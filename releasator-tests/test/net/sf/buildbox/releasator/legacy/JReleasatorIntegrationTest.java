package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.buildbox.changes.ChangesController;
import net.sf.buildbox.changes.ChangesControllerImpl;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * target/test-data
 * target/test-data/testset/
 * target/test-data/svnrepo/
 * target/test-data/wc/
 * target/test-data/tmp/
 * target/test-data/releasator-settings.xml
 * target/test-data/repository/
 * target/test-data/siterepo/
 */
public class JReleasatorIntegrationTest extends TestCase {
    private static File TESTDATA_DIR;

    private static File TESTSET;

    private static File SETTINGS_XML;
    private static File CONF;

    private static String LOCAL_SVN_URL;

    private static File WC;
    private static File TMP;
    private final URL suiteUrl;

    public JReleasatorIntegrationTest(URL suiteUrl) {
        this.suiteUrl = suiteUrl;
    }

    @BeforeClass
    public static void setupClass() throws TransformerException, IOException, CommandLineException, InterruptedException {
        final File testClassesDir = new File(Class.class.getResource("/REF.txt").getFile()).getParentFile();
        TESTDATA_DIR = new File(testClassesDir.getParentFile(), "test-data");
        FileUtils.deleteDirectory(TESTDATA_DIR);
        TESTSET = new File(TESTDATA_DIR, "testset");

        // prepare settings.xml
        CONF = new File(TESTDATA_DIR, "testconf");
        SETTINGS_XML = new File(CONF, "test-releasator-settings.xml");
        Helper.generateSettings(SETTINGS_XML);

        // prepare svn repository
        final File svnRepo = new File(TESTDATA_DIR, "svnrepo");
        WC = new File(TESTDATA_DIR, "wc");
        LOCAL_SVN_URL = Helper.svnRepoPrepareAndCheckout(svnRepo);
        Helper.doCmd(TESTDATA_DIR, "svn", "mkdir", "--parents", LOCAL_SVN_URL + "/tags", LOCAL_SVN_URL + "/underworld/tags", "--message", "prepare tag dirs to allow tagging the release");
        Helper.doCmd(TESTDATA_DIR, "svn", "checkout", LOCAL_SVN_URL, WC.getAbsolutePath());

        //
        TMP = new File(TESTDATA_DIR, "tmp");
        TMP.mkdirs();
    }

    @Override
    protected void runTest() throws Throwable {
        System.out.println(String.format("----- Test suite '%s' = %s -----", getName(), suiteUrl));
        // setup
        final File destDir = new File(TESTSET, getName());
        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }
        Helper.unzipUrl(suiteUrl, destDir);

        // here we call the testing functionality, just like in any normal test, except that here we can use arguments of method createTest()
        performTest(destDir);
        //
    }

    private void performTest(File suiteDir) throws Exception {
        final Properties properties = new Properties();
        final FileInputStream is = new FileInputStream(new File(suiteDir, "testsuite.properties"));
        try {
            properties.load(is);
            // import input into svn repo
            final String moduleVcsPath = properties.getProperty("module.vcs.path");
            Helper.doCmd(suiteDir, "svn", "import", "-m", "importing module", "input/wc/" + moduleVcsPath, LOCAL_SVN_URL + "/" + moduleVcsPath);
            final ChangesController inputChanges = new ChangesControllerImpl(new File(suiteDir, "input/wc/" + moduleVcsPath + "/changes.xml"));
            final String inputArtifactId = inputChanges.getArtifactId();
            final String inputGroupId = inputChanges.getGroupId();
            // run the test
            final String releaseVersion = properties.getProperty("release.version");
            final String codename = properties.getProperty("release.codename");
            final int exitCode = Helper.releasator("--tmpbase", TMP.getAbsolutePath(),
                    "--preload-repository", TMP.getParentFile().getParentFile().getParentFile() + "/preload.zip",
                    "--author", "releasator@gmail.com",
                    "--conf", CONF.getAbsolutePath(),
                    "full", "scm:svn:" + LOCAL_SVN_URL + "/" + moduleVcsPath, releaseVersion, codename == null ? "" : codename);
            Assert.assertEquals("release failed", 0, exitCode);
            // compare control dir against results
            Helper.doCmd(WC, "svn", "update");
            final String tagsBase = properties.getProperty("module.vcs.tagbase"); //TODO: compute using releasator's method
            int controlFileCount = Integer.parseInt(properties.getProperty("expected.controlfile.count"));
            FileTreeAssert.assertResultFilesMatchDesired(
                    new ChangesAndPomDifferenceListener(TESTDATA_DIR.getAbsolutePath()),
                    new File(suiteDir, "expected/wc"),
                    WC,
                    tagsBase + "/" + inputGroupId + "-" + inputArtifactId + "-" + releaseVersion, controlFileCount);
            //TODO: check number of xml differences
            // second release if configured
            final String secondReleaseVersion = properties.getProperty("second.release.version");
            if (secondReleaseVersion != null) {
                System.out.println("secondReleaseVersion = " + secondReleaseVersion);
                final String secondReleaseItem = properties.getProperty("second.release.item");
                final String secondReleaseUrl = properties.getProperty("second.release.url");
                final int exitCode2 = Helper.releasator("--tmpbase", TMP.getAbsolutePath(),
                        "--preload-repository", TMP.getParentFile().getParentFile().getParentFile() + "/preload.zip",
                        "--author", "releasator@gmail.com",
                        "--conf", CONF.getAbsolutePath(),
                        "--changes-item-simple", secondReleaseItem,
                        "--pom-change", "/project/url", secondReleaseUrl,
                        "full", "scm:svn:" + LOCAL_SVN_URL + "/" + moduleVcsPath, secondReleaseVersion);
                Assert.assertEquals("second release failed", 0, exitCode2);
                //todo check for expected content of files
            }
        } finally {
            is.close();
        }
    }

    /**
     * Entry point to the whole magic; junit invokes this method to create the suite
     *
     * @return the suite with all tests in it
     */
    public static junit.framework.Test suite() throws TransformerException, CommandLineException, IOException, InterruptedException {
        final TestSuite suite = new TestSuite(JReleasatorIntegrationTest.class.getName());
        setupClass();
        // here we enlist all testcases, in our case distinguished by url
        final String resName = "TESTSUITES";
        final ClassLoader classLoader = JReleasatorIntegrationTest.class.getClassLoader();
        final Collection<URL> urls = Helper.listResources(resName, classLoader);
        for (URL suiteUrl : urls) {
            // we derive the display name from method arguments
            final String s = suiteUrl.toString();
            final int n = s.lastIndexOf('/');
            final String testDisplayName = s.substring(n + 1, s.length() - 4);
            final JReleasatorIntegrationTest test = new JReleasatorIntegrationTest(suiteUrl);
            test.setName(testDisplayName);
            suite.addTest(test);
        }
        //
        return suite;
    }
}
