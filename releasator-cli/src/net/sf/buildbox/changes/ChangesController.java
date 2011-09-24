package net.sf.buildbox.changes;

import java.io.IOException;
import java.io.File;
import java.util.Map;

import net.sf.buildbox.changes.bean.ItemBean;

/**
 * This interface is currently just some functionality preview, and will change considerably.
 */
public interface ChangesController {
    String RLSCFG_CMDLINE_MAVEN_ARGUMENTS = "cmdline.maven.arguments";
    String RLSCFG_SKIP_DRY_BUILD = "skip.dry.build";
    String RLSCFG_POM_KEEP_NAME = "pom.keep.name";
    String RLSCFG_RELEASE_VERSION_PREFIX = "release.version.prefix";
    String RLSCFG_MAVEN_PREFIX = "maven.";
    String RLSCFG_MRP_VERSION = "maven-release-plugin.version";
    String RLSCFG_MAVEN_VERSION = "maven.version";
    String RLSCFG_JDK_VERSION = "jdk.version";
    String RLSCFG_SETTINGS_ID = "settings.id";
    String RLSCFG_ENV_PREFIX = "env.";

    public String getGroupId();
    public String getArtifactId();
    public String getVersion();
    public void snapshotToLocalBuild(long timestamp, String author) throws IOException;

    /**
     * Changes current "localbuild" block to a "release" block, and adds neccessary metadata.
     * Fails if there is no change yet or if any other important data is missing.
     *
     * @param releaseVersion under which version to release it
     * @param releaseTag full tag path within the repository - i.e. "/tags/net.sf.buildbox.changes-changes-2.0.0"
     * @throws java.io.IOException -
     * @return true if the snapshot version for next development iteration should be increased;
     * this happens when release version equals to current snapshot's major part (the one without suffix "-SNAPSHOT" if present)
     */
    public boolean localBuildToRelease(String releaseVersion, String releaseTag) throws IOException;


    /**
     * Creates the "unreleased" block that is capable of accepting new content.
     *
     * @param snapshotVersion the version in development phase, for instance "1-SNAPSHOT"
     */
    public void releaseToSnapshot(String snapshotVersion);

    /**
     * Performs various checks:
     * - unreleased must contain at least 1 item
     * - version sequence must grow except if groupId/artifactId changes
     * - timestamp sequence must grow
     * - tag must not exist (before release)
     * - tag must exist (after release)
     */
    public void validate();

    public void addBuildTool(BuildToolRole role, String groupId, String artifactId, String version);

    public void addFile(String moduleGroupId, String moduleArtifactId, String moduleVersion, String classifier, String type);

    public void addUnreleasedItem(ItemBean.Action.Enum action, String issueRef, String component, String text);

    void save(File file) throws IOException;

    void setCodename(String newCodename);

    void setVcsInfo(String vcsType, String vcsId, String develCodePath, String revision);

    String getReleaseConfigProperty(String propertyName);

    Map<String,String> getReleaseConfigProperties();
}
