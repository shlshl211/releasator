package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.buildbox.args.annotation.Param;
import net.sf.buildbox.args.annotation.SubCommand;
import net.sf.buildbox.changes.BuildToolRole;
import net.sf.buildbox.changes.ChangesController;
import net.sf.buildbox.changes.ChangesControllerImpl;
import net.sf.buildbox.releasator.model.PomChange;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

@SubCommand(name = "prepare", description = "prepares the release tag")
public class CmdPrepare extends AbstractPrepareCommand {

    private ScmData releaseTag;

    public CmdPrepare(
            @Param("snapshot-url") String projectUrl,
            @Param("version") String releaseVersion,
            @Param("codename") String... codename) {
        super(projectUrl, releaseVersion, codename);
    }

    private boolean preReleaseModifyChangesXml(File wc, ChangesController chg, ScmData scm, ScmData tag) throws TransformerException, IOException, InterruptedException {
        final File origChangesXml = new File(tmp, "changes-0.xml");
        final File changesXml = new File(wc, "changes.xml");
        FileUtils.rename(changesXml, origChangesXml);
        //
        // prepare changes to be released
        // codename
        if (codename != null) {
            chg.setCodename(codename);
        }
        // items
        for (String itemText : changeItems) {
            final String text;
            if (itemText.startsWith("@")) {
                text = FileUtils.fileRead(itemText.substring(1));
            } else {
                text = itemText;
            }
            System.out.println("ADDING: " + text);
            chg.addUnreleasedItem(null, null, null, text);
        }
        //
        chg.snapshotToLocalBuild(System.currentTimeMillis(), author);
        // VCS
        chg.setVcsInfo(tag.getVcsType(), scm.getVcsId(), scm.getVcsPath(), revision);
        {
            // buildTools
//TODO            chg.addBuildTool(BuildToolRole.COMPILER, "com.sun.jdk", "java", System.getProperty("java.vm.version"));
//TODO            chg.addBuildTool(BuildToolRole.BUILD, "org.apache", "maven", MyUtils.getMavenVersion());
            chg.addBuildTool(BuildToolRole.RELEASE, Params.RELEASE_PLUGIN_GROUPID, Params.RELEASE_PLUGIN_ARTIFACTID, mavenReleasePluginVersion(chg));
            chg.addBuildTool(BuildToolRole.RELEASE, "net.sf.buildbox", "releasator", Params.releasatorVersion);
        }
        final boolean shouldAdvanceSnapshotVersion = chg.localBuildToRelease(releaseVersion, tag.getVcsPath());
        chg.save(changesXml);
        return shouldAdvanceSnapshotVersion;
    }

    private void preReleaseModifyPoms(File wc, ParsedPom top, Map<File, ParsedPom> allPoms, String publicArtifactId, boolean keepName) throws TransformerException, IOException {
        // remove tag "scm" from all involved pom.xml files + expand name + reindent them all
        for (Map.Entry<File, ParsedPom> entry : allPoms.entrySet()) {
            final File pomFile = entry.getKey();
            final File pomFileNew = new File(pomFile.getAbsolutePath() + ".new");
            final Transformer pomSetName = MyUtils.xsltPomTransformer();
            if (!keepName) {
                pomSetName.setParameter("name-action", "static");
            }
            pomSetName.setParameter("version", releaseVersion);
            final String subPath = MyUtils.subpath(wc, pomFile.getParentFile());
            pomSetName.setParameter("scm", projectUrl + subPath); //??
            pomSetName.setParameter("release-root", top.groupId + ":" + top.artifactId + ":" + releaseVersion);
            pomSetName.transform(new StreamSource(pomFile), new StreamResult(pomFileNew));
            FileUtils.rename(pomFileNew, pomFile);
        }
        // add scm info to the topmost pom
        final File topFile = new File(wc, "pom.xml");
        final File topFileNew = new File(topFile.getAbsolutePath() + ".new");
        final Transformer fixTopPom = MyUtils.xsltPomTransformer();
        if (!keepName) {
            fixTopPom.setParameter("name-action", "static");
        }
        fixTopPom.setParameter("newArtifactId", publicArtifactId);
        fixTopPom.setParameter("version", releaseVersion);
        fixTopPom.setParameter("scm", projectUrl); //??
        //NOT on topmost pom!!! fixTopPom.setParameter("release-root", top.gav());
        for (PomChange pomChange : pomChanges) {
            final String location = pomChange.getLocation();
            if (location.equals("/project/url")) {
                fixTopPom.setParameter("url", pomChange.getValue());
            } else {
                throw new IllegalArgumentException(location + ": unsupported location. Only '/project/url' is currently supported.");
            }
        }
        fixTopPom.transform(new StreamSource(topFile), new StreamResult(topFileNew));
        FileUtils.rename(topFileNew, topFile);
    }

    private void postReleaseModifyPoms(File wc, Map<File, ParsedPom> allPoms, String develArtifactId, boolean keepName) throws TransformerException, IOException {
        // remove tag "scm" from all involved pom.xml files + expand name
        for (Map.Entry<File, ParsedPom> entry : allPoms.entrySet()) {
            final Transformer pomSetName = MyUtils.xsltPomTransformer();
            final File pomFile = entry.getKey();
            final File pomFileNew = new File(pomFile.getAbsolutePath() + ".new");
            if (!keepName) {
                pomSetName.setParameter("name-action", "dynamic");
            }
            pomSetName.transform(new StreamSource(pomFile), new StreamResult(pomFileNew));
            FileUtils.rename(pomFileNew, pomFile);
        }
        final Transformer fixTopPom = MyUtils.xsltPomTransformer();
        final File topFile = new File(wc, "pom.xml");
        final File topFileNew = new File(topFile.getAbsolutePath() + ".new");
        fixTopPom.setParameter("newArtifactId", develArtifactId);
        fixTopPom.transform(new StreamSource(topFile), new StreamResult(topFileNew));
        FileUtils.rename(topFileNew, topFile);
    }

    private void preReleaseListAllModules(ChangesController chg, File localRepository, Map<File, ParsedPom> allPoms, File file) {
        // modules
        for (Map.Entry<File, ParsedPom> entry : allPoms.entrySet()) {
            final ParsedPom pp = entry.getValue();
            final String artifactId = file.equals(pp.file) ? chg.getArtifactId() : pp.artifactId;
            TmpUtils.addMavenModule(chg, localRepository, pp.groupId, artifactId, pp.version, releaseVersion);
        }
    }

    private ScmData doReleaseActions(ScmData scm) throws Exception {
        final File wc = checkoutFiles(scm, "code", "checkout-log.txt").getAbsoluteFile();
        final File topPomFile = new File(wc, "pom.xml");
        final Map<File, ParsedPom> allPoms = MyUtils.parseAllPoms(topPomFile);
        final ParsedPom top = allPoms.get(topPomFile);
        final File changesXmlFile = new File(wc, "changes.xml");
        final ChangesController chg = new ChangesControllerImpl(changesXmlFile);
        final String nextSnapshotVersion = chg.getVersion();
        MyUtils.checkChangesXml(chg, top);

        final String releaseVersionPrefix = chg.getReleaseConfigProperty(ChangesController.RLSCFG_RELEASE_VERSION_PREFIX);
        if (releaseVersionPrefix == null) {
            MyUtils.checkVersionFormat(releaseVersion);
        }

        final String publicArtifactId = chg.getArtifactId();
        final File localRepository = new File(tmp, "repository");
        final String scmTag = String.format("%s-%s-%s", top.groupId, publicArtifactId, releaseVersion);
        final Properties releaseProps = MyUtils.prepareReleaseProps(projectUrl, chg);
        final ScmData releaseTag = scm.getTagScm(scmTag);
        // changes.xml
        final boolean shouldAdvanceSnapshotVersion = preReleaseModifyChangesXml(wc, chg, scm, releaseTag);
        final boolean skipDryBuild = Boolean.TRUE.toString().equals(chg.getReleaseConfigProperty(ChangesController.RLSCFG_SKIP_DRY_BUILD));
        // pom.xml
        final boolean pomKeepName = Boolean.TRUE.toString().equals(chg.getReleaseConfigProperty(ChangesController.RLSCFG_POM_KEEP_NAME));
        preReleaseModifyPoms(wc, top, allPoms, publicArtifactId, pomKeepName);
        //TODO: fail if the tag already exists

        //save releaseProps to $wc/release.properties
        System.out.println("shouldAdvanceSnapshotVersion = " + shouldAdvanceSnapshotVersion);
        releaseProps.store(new FileOutputStream(new File(wc, "release.properties")), "Releasator");

        // in order to fail fast, we prepare the second maven commandline right now
        final List<String> mavenArgs = new ArrayList<String>();
        final Commandline mvnReleasePrepareCmd;
        {
            if (!skipDryBuild) {
                // clean is useless for the first build
                mavenArgs.add("clean");
            }
            mavenArgs.add("-DpreparationGoals=install");
            mavenArgs.addAll(Arrays.asList(
                    "-DuseEditMode=true",
                    "-DreleaseVersion=" + releaseVersion,
                    "-DdevelopmentVersion=" + top.version,
                    "-Dtag=" + scmTag,
                    mavenReleasePrepareGoal(chg)
                    ));
            mavenArgs.addAll(MyUtils.getConfiguredArgs(chg, ChangesController.RLSCFG_CMDLINE_MAVEN_ARGUMENTS));
            mvnReleasePrepareCmd = prepareMavenCommandline(chg, wc, localRepository, mavenArgs);
        }

        try {
            preloadRepository(localRepository);
            System.out.println("==== DRY RUN ====");
            if (dryOnly || ! skipDryBuild) {
                // DRY RUN - right before we commit anything, let's perform process as similar as possible to the release
                final List<String> mavenDryArgs = new ArrayList<String>(mavenArgs);
                mavenDryArgs.add("-DdryRun=true");
                final Commandline mvnDryReleasePrepareCmd = prepareMavenCommandline(chg, wc, localRepository, mavenDryArgs);
                runHook(AntHookSupport.ON_BEFORE_DRY_BUILD);
                MyUtils.loggedCmd(new File(tmp, "release-dry-log.txt"), mvnDryReleasePrepareCmd);
                runHook(AntHookSupport.ON_AFTER_DRY_BUILD);
                preReleaseListAllModules(chg, localRepository, allPoms, top.file);
                chg.save(changesXmlFile);
            } else {
                System.out.println("WARNING: skipping dry build, artifacts will not be listed!");
            }
            if (dryOnly) return null;

            System.out.println("==== PRE-RELEASE ====");
            {
                // THIS is the first change that needs to be reverted (currently manually) if release preparation fails
                runHook(AntHookSupport.ON_BEFORE_FIRST_COMMIT);
                MyUtils.loggedCmd(new File(tmp, "svn-precommit.txt"), wc,
                        "svn", "commit", "--non-interactive", "--no-unlock", "--message",
                        String.format("%s Pre-release changes for version %s:%s:%s by %s",
                                Params.RELEASATOR_PREFIX, top.groupId, publicArtifactId, releaseVersion, author));
                MyUtils.doCmd(wc, "svn", "update");
            }


            System.out.println("==== PREPARE RELEASE ====");
            //save releaseProps to $wc/release.properties
            releaseProps.store(new FileOutputStream(new File(wc, "release.properties")), "Releasator");
            runHook(AntHookSupport.ON_BEFORE_MRP_PREPARE);
            MyUtils.loggedCmd(new File(tmp, "release-prepare-log.txt"), mvnReleasePrepareCmd);
            MyUtils.doCmd(wc, "svn", "update");
            runHook(AntHookSupport.ON_AFTER_MRP_PREPARE);

            System.out.println("==== POST-RELEASE ====");
            // post-release modifications
            if (shouldAdvanceSnapshotVersion) {
                //TODO compute the next snapshot; check if policy allows this
                System.err.println("You should change the snapshot version to avoid conflicts in local repository and confusion in dependencies");
            }
            chg.releaseToSnapshot(nextSnapshotVersion);
            chg.save(changesXmlFile);
            postReleaseModifyPoms(wc, allPoms, top.artifactId, pomKeepName);


            MyUtils.loggedCmd(new File(tmp, "svn-postcommit.txt"), wc,
                    "svn", "commit", "--non-interactive", "--no-unlock", "--message",
                    String.format("%s Post-release changes for version %s:%s:%s by %s",
                            Params.RELEASATOR_PREFIX, top.groupId, publicArtifactId, releaseVersion, author));
            runHook(AntHookSupport.ON_AFTER_LAST_COMMIT);
        } catch (Exception e) {
            System.out.println("-------- >>> ERROR : " + e.getMessage() + " <<< --------");
            throw e;
        }
        MyUtils.doCmd(wc, "svn", "update");
        return releaseTag;
    }

    public Integer call() throws Exception {
        final ScmData scm = ScmData.valueOf(projectUrl);
        init();
        try {
            lock(scm.getVcsId() + ":" + scm.getVcsPath());
            runHook(AntHookSupport.ON_VCS_LOCK);
            MyUtils.assertValidAuthor(author);
            releaseTag = doReleaseActions(scm);
            if (dryOnly) {
                System.out.println("DRY RELEASE completed successfully. See more in " + tmp);
            } else {
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println("RELEASE_TAG_URL=" + releaseTag);
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println("Release was successfuly prepared. Use the following command to upload it for public use:");
                System.out.println("releasator upload " + releaseTag);
            }
            return 0;
        } finally {
            unlock();
            runHook(AntHookSupport.ON_VCS_UNLOCK);
        }
    }


    /**
     * @return after execution, the tag that marks the release in version control
     */
    public ScmData getReleaseTag() {
        return releaseTag;
    }

    private String mavenReleasePluginVersion(ChangesController chg) throws IOException {
        String mrpVersion = chg.getReleaseConfigProperty(ChangesController.RLSCFG_MRP_VERSION);
        if (mrpVersion == null) {
            mrpVersion = getReleasatorProperty(ReleasatorProperties.CFG_MRP_VERSION, false);
        }
        return mrpVersion == null ? "2.1" : mrpVersion;
    }

    private String mavenReleasePrepareGoal(ChangesController chg) throws IOException {
        return String.format("%s:%s:%s:prepare",
                Params.RELEASE_PLUGIN_GROUPID,
                Params.RELEASE_PLUGIN_ARTIFACTID,
                mavenReleasePluginVersion(chg));
    }

}
