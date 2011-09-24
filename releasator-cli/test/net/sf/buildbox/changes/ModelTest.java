package net.sf.buildbox.changes;

import java.io.File;
import java.io.IOException;
import net.sf.buildbox.changes.bean.ChangesDocumentBean;
import net.sf.buildbox.changes.bean.ItemBean;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.codehaus.plexus.util.FileUtils;
import testutil.Tst;

public class ModelTest {
    private File testDataDir;
    private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home"), ".m2/repository");

    @Before
    public void setUp() {
        testDataDir = Tst.getTestDataDir();
    }

    private void save(ChangesController chg, String nm) throws IOException {
        chg.validate();
        final File file = new File(testDataDir, nm);
        chg.save(file);
        System.out.println("=== " + nm + " ===");
        final String xml = FileUtils.fileRead(file);
        System.out.println(xml);
        System.out.println();
    }

    @Test
    public void testEvolution() throws IOException {
        final String groupId = "net.kozelka.demo";
        final String artifactId = "test";
        final String version = "1-SNAPSHOT";
        String releaseVersion = "1.0.0-alpha-1";
        final ChangesController chg = new ChangesControllerImpl(groupId, artifactId, version, "initial release");
        save(chg, "00-new-changes.xml");
        chg.snapshotToLocalBuild(System.currentTimeMillis(), "pkozelka@gmail.com");
        chg.setVcsInfo("svn", "net.kozelka.koss", "/trunk/maven/ko-maven-plugin", null);
        chg.setCodename("Uragan");
        chg.addBuildTool(BuildToolRole.BUILD, "org.apache", "maven", "2.0.9");
        chg.addBuildTool(BuildToolRole.RELEASE, "net.sf.buildbox.tools", "releasator", "1-SNAPSHOT");
        chg.addFile(groupId, artifactId, version, null, "pom");
        chg.addFile(groupId, artifactId, version, null, "jar");
        chg.addFile(groupId, artifactId, version, "changes", "xml");
        ChangesControllerUtil.addMavenModule(chg, LOCAL_REPOSITORY, "org.codehaus.mojo", "xmlbeans-maven-plugin", "2.3.2");
        ChangesControllerUtil.addMavenModule(chg, LOCAL_REPOSITORY, "org.apache.xmlbeans", "xmlbeans", "2.4.0");
        save(chg, "01-localbuild.xml");
        chg.localBuildToRelease(releaseVersion, String.format("/tags/%s-%s-%s",
                groupId, artifactId, releaseVersion));
        save(chg, "02-release.xml");
        chg.releaseToSnapshot("1-SNAPSHOT");
        save(chg, "03-devel-ready.xml");

        // 2nd release:
        addItem(chg, "some another change");
        chg.snapshotToLocalBuild(System.currentTimeMillis(), "pkozelka@gmail.com");
        chg.setVcsInfo("svn", "net.kozelka.koss", "/trunk/maven/ko-maven-plugin", null);
        chg.setCodename("Hurican");
        chg.addBuildTool(BuildToolRole.BUILD, "org.apache", "maven", "2.0.9");
        chg.addBuildTool(BuildToolRole.RELEASE, "net.sf.buildbox.tools", "releasator", "1-SNAPSHOT");
        chg.addFile(groupId, artifactId, version, null, "pom");
        chg.addFile(groupId, artifactId, version, null, "jar");
        chg.addFile(groupId, artifactId, version, "changes", "xml");
        save(chg, "04-localbuild.xml");

        releaseVersion = "1.0.0-alpha-2";
        chg.localBuildToRelease(releaseVersion, String.format("/tags/%s-%s-%s",
                groupId, artifactId, releaseVersion));
        save(chg, "05-release.xml");

        chg.releaseToSnapshot("1-SNAPSHOT");
        save(chg, "06-devel-ready.xml");
    }

    private void addItem(ChangesController chg, String text) {
        ((ChangesControllerImpl) chg).changes.getUnreleased().addNewItem().setStringValue(text);
    }

    @Test
    public void testModel() throws IOException, XmlException {
        final ChangesDocumentBean doc = ChangesDocumentBean.Factory.parse(new File(testDataDir.getParentFile().getParentFile(), "changes.xml"));
        final ChangesDocumentBean.Changes changes = doc.getChanges();
        System.out.println("--- List of previous releases ---");
        for (ChangesDocumentBean.Changes.Release release : changes.getReleaseArray()) {
            System.out.println(release.getTimestamp() + " " + release.getGroupId() + ":" + release.getArtifactId() + ":" + release.getVersion() + " by " + release.getAuthor());
            for (ItemBean item : release.getItemArray()) {
                System.out.println("   [" + item.getAction() + "] " + item.getStringValue().trim());
            }
        }
        System.out.println("--- ---");
    }
}
