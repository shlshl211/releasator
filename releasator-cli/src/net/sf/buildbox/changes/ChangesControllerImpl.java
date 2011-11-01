package net.sf.buildbox.changes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.io.*;

import net.sf.buildbox.changes.bean.*;
import net.sf.buildbox.releasator.legacy.ReleasatorProperties;
import net.sf.buildbox.util.BbxStringUtils;
import net.sf.buildbox.util.StreamingMultiDigester;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ChangesControllerImpl implements ChangesController {
    public static final XmlOptions xmlOptions = new XmlOptions()
            .setUseDefaultNamespace()
            .setCharacterEncoding("UTF-8")
            .setSavePrettyPrint();

    private final ChangesDocumentBean doc;
    final ChangesDocumentBean.Changes changes;
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    private static final String COMMENTED_EXAMPLE = " delete this wrapping comment and write your change items here - at least one is required for release.\n" +
            "    <item action=\"fix/add/improve\" issue=\"XX-1234\" component=\"WhateverComponent\">some meaningful description</item>\n    ::";

    public ChangesControllerImpl(File xmlFile) throws IOException {
        try {
            this.doc = ChangesDocumentBean.Factory.parse(xmlFile, xmlOptions);
            this.changes = doc.getChanges();
        } catch (XmlException e) {
            e.printStackTrace();
            throw new IOException(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ChangesControllerImpl(ChangesDocumentBean doc) {
        this.doc = doc;
        this.changes = doc.getChanges();
    }

    private static ChangesDocumentBean newDoc() {
        final ChangesDocumentBean result = ChangesDocumentBean.Factory.newInstance(xmlOptions);
        result.addNewChanges();
        return result;
    }

    public ChangesControllerImpl(String groupId, String artifactId, String version, String initialItemText) {
        this(newDoc());
        final ChangesDocumentBean.Changes.Unreleased unreleased = changes.addNewUnreleased();
        unreleased.setGroupId(groupId);
        unreleased.setArtifactId(artifactId);
        unreleased.setVersion(version);
        if (initialItemText != null) {
            final ItemBean item = unreleased.insertNewItem(0);
            item.setAction(ItemBean.Action.ADD);
            item.setStringValue(initialItemText);
        }
    }

    private VersionNotesBean latest() {
        if (changes.getUnreleased() != null) return changes.getUnreleased();
        if (changes.getLocalbuild() != null) return changes.getLocalbuild();
        return changes.getReleaseArray(0);
    }

    public String getGroupId() {
        return latest().getGroupId();
    }

    public String getArtifactId() {
        return latest().getArtifactId();
    }

    public String getVersion() {
        return latest().getVersion();
    }

    public void snapshotToLocalBuild(long timestamp, String author) throws IOException {
        final BuiltVersionNotesBean localBuild = changes.addNewLocalbuild();
        final ChangesDocumentBean.Changes.Unreleased unreleased = changes.getUnreleased();
        if (unreleased == null) {
            throw new IllegalStateException("no unreleased part found");
        }
        // remove comments:
        final Node parentNode = unreleased.getDomNode();
        Node node = parentNode.getFirstChild();
        while (node != null) {
            final Node n1 = node;
            node = node.getNextSibling();
            if (n1.getNodeType() == Node.COMMENT_NODE) {
                parentNode.removeChild(n1);
            }
        }
        //
        try {
            localBuild.set(unreleased);
            localBuild.setVersion(unreleased.getVersion());
            final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(timestamp);
            localBuild.setTimestamp(cal);
            localBuild.setAuthor(author);
            // vcs, toolstack, os, cpu

            final BuiltVersionNotesBean.Os os = localBuild.addNewOs();
            os.addNewArch().setStringValue(System.getProperty("os.arch"));
            os.addNewName().setStringValue(System.getProperty("os.name"));
            os.addNewVersion().setStringValue(System.getProperty("os.version"));

        } finally {
            changes.unsetUnreleased();
        }
    }

    public boolean localBuildToRelease(String releaseVersion, String releaseTag) throws IOException {
        final BuiltVersionNotesBean localBuild = changes.getLocalbuild();
        if (localBuild == null) {
            throw new IllegalStateException("no localbuild part prepared");
        }
// TODO: check that all neccessary info is ready and correct
        // check that there is no release with such version yet
        final String releaseGroupId = localBuild.getGroupId();
        final String releaseArtifactId = localBuild.getArtifactId();
        for (ChangesDocumentBean.Changes.Release release : changes.getReleaseArray()) {
            if (!release.getGroupId().equals(releaseGroupId)) continue;
            if (!release.getArtifactId().equals(releaseArtifactId)) continue;
            if (!release.getVersion().equals(releaseVersion)) continue;
            throw new IllegalStateException("File changes.xml already contains version " + releaseVersion);
        }

        // check that major version specified in changes.xml prefixes the releaseVersion
        boolean shouldIncrementSnapshot = false;
        final String unreleasedVersion = localBuild.getVersion();
        final String releaseVersionPrefix = getReleaseConfigProperty(RLSCFG_RELEASE_VERSION_PREFIX);
        if (releaseVersionPrefix == null) {
            final String majorVersion;
            if (unreleasedVersion.endsWith(SNAPSHOT_SUFFIX)) {
                majorVersion = unreleasedVersion.substring(0, unreleasedVersion.length() - SNAPSHOT_SUFFIX.length());
            } else {
                majorVersion = unreleasedVersion;
            }
            if (releaseVersion.equals(majorVersion)) {
                // ok, we release exactly the same version as major
                shouldIncrementSnapshot = true;
            } else if (releaseVersion.startsWith(majorVersion + ".")) {
                // ok, we release minor of current version
            } else if (releaseVersion.startsWith(majorVersion + "-")) {
                // ok, we release alpha/beta/rc or patch of current version
            } else {
                throw new IllegalArgumentException(String.format("version mismatch - changes.xml/unreleased/@version(='%s') is not prefix for releaseVersion(='%s')",
                        unreleasedVersion, releaseVersion));
            }
        } else {
            if (!releaseVersion.startsWith(releaseVersionPrefix)) {
                throw new IllegalArgumentException(String.format("version mismatch - changes.xml/configuration/property[@name='%s'] " +
                        "requires the version to be prefixed with '%s' - not satisfied by your version: %s ",
                        RLSCFG_RELEASE_VERSION_PREFIX, releaseVersionPrefix, releaseVersion));
            }
        }

        final ItemBean[] items = localBuild.getItemArray();
        if (items.length == 0) {
            throw new IllegalStateException("There are no change items to release");
        }
        final String firstItemStr = items[0].getStringValue();
        if (firstItemStr.length() < 10) {
            throw new IllegalStateException("Item text does not look like a valuable description: " + firstItemStr);
        }
/*TODO:
        if (localBuild.getModuleArray().length == 0) {
            throw new IllegalStateException("Missing list of released artifacts");
        }
*/
        if (localBuild.getVcs() == null) {
            throw new IllegalStateException("Missing VCS information");
        }
        try {
            final BuiltVersionNotesBean release = changes.insertNewRelease(0);
            release.set(localBuild);
            release.setVersion(releaseVersion);
            release.getVcs().setTag(releaseTag);
            return shouldIncrementSnapshot;
        } finally {
            changes.unsetLocalbuild();
        }
    }

    public void releaseToSnapshot(String snapshotVersion) {
        if (changes.getLocalbuild() != null) {
            throw new IllegalStateException("cannot advance to snapshot from local distribution");
        }
        if (changes.getUnreleased() != null) {
            throw new IllegalStateException("not in released state - unreleased section found");
        }
        final ChangesDocumentBean.Changes.Release r = changes.getReleaseArray(0);
        final ChangesDocumentBean.Changes.Unreleased u = changes.addNewUnreleased();
        u.setGroupId(r.getGroupId());
        u.setArtifactId(r.getArtifactId());
        u.setVersion(snapshotVersion);
        u.getDomNode().appendChild(((Document) doc.getDomNode()).createComment(COMMENTED_EXAMPLE));
    }

    public void validate() {
        doc.validate();
        //TODO: add some more validations
    }

    public void addBuildTool(BuildToolRole role, String groupId, String artifactId, String version) {
        final BuiltVersionNotesBean localBuild = changes.getLocalbuild();
        final BuiltVersionNotesBean.Buildtool tool = localBuild.addNewBuildtool();
        tool.setRole(role.toString());
        tool.setGroupId(groupId);
        tool.setArtifactId(artifactId);
        tool.setVersion(version);
    }

    public void addFile(File file, String moduleGroupId, String moduleArtifactId, String moduleVersion, String classifier, String type) {
        final BuiltVersionNotesBean latest = (BuiltVersionNotesBean) latest(); //TODO: DIRTY - should be done on localbuild!
        final BuiltVersionNotesBean.Module module = findOrCreate(latest, moduleGroupId, moduleArtifactId, moduleVersion);
        final BuiltVersionNotesBean.Module.Artifact artifact = module.addNewArtifact();
        if (classifier != null) {
            artifact.addNewClassifier().setStringValue(classifier);
        }
        artifact.addNewType().setStringValue(type);
        if (file != null) {
            if ("true".equals(getReleaseConfigProperty(ReleasatorProperties.CFG_RECORD_FILE_SIZE))) {
                artifact.addNewLength().setStringValue(file.length() + "");
            }
            if ("true".equals(getReleaseConfigProperty(ReleasatorProperties.CFG_RECORD_FILE_CHECKSUM))) {
                try {
                    final MessageDigest md5 = MessageDigest.getInstance("MD5");
                    final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                    StreamingMultiDigester.compute(file, md5, sha1);
                    final byte[] md5sum = md5.digest();
                    final byte[] sha1sum = sha1.digest();
                    artifact.addNewMd5().setStringValue(BbxStringUtils.hexEncodeBytes(md5sum));
                    artifact.addNewSha1().setStringValue(BbxStringUtils.hexEncodeBytes(sha1sum));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addUnreleasedItem(ItemBean.Action.Enum action, String issueRef, String component, String text) {
        final ChangesDocumentBean.Changes.Unreleased unreleased = changes.getUnreleased();
        if (unreleased == null) {
            throw new IllegalStateException("No 'unreleased' element found in changes.xml");
        }
        final ItemBean item = unreleased.insertNewItem(0);
        if (action != null) {
            item.setAction(action);
        }
        if (issueRef != null) {
            item.setIssue(issueRef);
        }
        if (component != null) {
            item.setComponent(component);
        }
        item.setStringValue(text);
    }

    public void save(File file) throws IOException {
        ((Element) doc.getChanges().getDomNode()).setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://buildbox.sf.net/changes/2.0 http://buildbox.sourceforge.net/releasator/changes.xsd");
        doc.save(file, xmlOptions);
    }

    public void setCodename(String newCodename) {
        if (changes.getUnreleased() != null) {
            changes.getUnreleased().setCodename(newCodename);
        } else if (changes.getLocalbuild() != null) {
            changes.getLocalbuild().setCodename(newCodename);
        } else {
            throw new IllegalArgumentException("no unreleased or localbuild section found, cannot change codename");
        }
    }

    public void setVcsInfo(String vcsType, String vcsId, String develCodePath, String revision) {
        final BuiltVersionNotesBean localBuild = changes.getLocalbuild();
        final VcsInfoBean vcs = localBuild.addNewVcs();
        vcs.setType(vcsType);
        vcs.setId(vcsId);
        vcs.setLocation(develCodePath);
        if (revision != null) {
            vcs.setRevision(revision);
        }
    }

    public String getReleaseConfigProperty(String propertyName) {
        return getReleaseConfigProperties().get(propertyName);
    }

    public Map<String, String> getReleaseConfigProperties() {
        final Map<String, String> config = new HashMap<String, String>();
        final ChangesDocumentBean.Changes.Configuration configuration = changes.getConfiguration();
        if (configuration == null) {
            return Collections.emptyMap();
        }
        for (ConfigurationPropertyBean propertyBean : configuration.getPropertyArray()) {
            final String propertyName = propertyBean.getName();
            final String propertyValue = propertyBean.getStringValue();
            config.put(propertyName, propertyValue);
        }
        return config;
    }

    private BuiltVersionNotesBean.Module findOrCreate(BuiltVersionNotesBean builtVersion, String groupId, String artifactId, String version) {
        final BuiltVersionNotesBean.Module[] modules = builtVersion.getModuleArray();
        for (BuiltVersionNotesBean.Module module : modules) {
            if (!groupId.equals(module.getGroupId().getStringValue())) continue;
            if (!artifactId.equals(module.getArtifactId().getStringValue())) continue;
            if (!version.equals(module.getVersion().getStringValue())) continue;
            return module;
        }
        final BuiltVersionNotesBean.Module module = builtVersion.addNewModule();
        module.addNewGroupId().setStringValue(groupId);
        module.addNewArtifactId().setStringValue(artifactId);
        module.addNewVersion().setStringValue(version);
        return module;
    }
}
