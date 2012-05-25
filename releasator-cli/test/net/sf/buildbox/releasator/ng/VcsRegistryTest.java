package net.sf.buildbox.releasator.ng;

import com.thoughtworks.xstream.XStream;
import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.impl.VcsRegistryImpl;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class VcsRegistryTest {

    private final ScmManager scmManager = new BasicScmManager();

    @Before
    public void setUp() throws Exception {
        scmManager.setScmProvider("svn", new SvnExeScmProvider());
        scmManager.setScmProvider("git", new GitExeScmProvider());
    }

    @Test
    public void testParse() throws Exception {
        final String resourceName = "git.bitbucket.xml";
        final VcsFactoryConfig config = loadVfc(resourceName);
        System.out.println("config = " + config);
    }

    @Test
    public void testShow() throws Exception {
        final VcsFactoryConfig config = createSourceforgeSvn();

        XStream xstream = vcsXstream();
        System.out.println(xstream.toXML(config));
    }

    @Test
    public void testFindByVcsId() throws Exception {
        final VcsRegistry reg = createDemoRegistry();
        final VcsRepositoryMatch match = reg.findByVcsId("bitbucket.org/pkozelka/buildbox");
        System.out.println("match = " + match);
        Assert.assertEquals("bitbucket.org/{REPOURI}", match.getMatchedMask());
        final VcsFactoryConfig config = match.getVcsFactoryConfig();
        final Collection<String> scmUrlMasks = config.getScmUrlMasks();
        System.out.println("otherScmUrlMasks = " + scmUrlMasks);
        Assert.assertEquals(5, scmUrlMasks.size());
    }

    @Test
    public void testFindByScmUrl() throws Exception {
        final VcsRegistry reg = createDemoRegistry();
        final VcsRepositoryMatch match = reg.findByScmUrl("scm:git:git@bitbucket.org:pkozelka/buildbox.git");
        System.out.println("match = " + match);
        Assert.assertEquals("scm:git:git@bitbucket.org:{REPOURI}.git", match.getMatchedMask());
    }

    @Test
    public void testCheckout() throws Exception {

        final VcsRegistry reg = createDemoRegistry();
//        final VcsRepositoryMatch match = reg.findByScmUrl("scm:git:git@bitbucket.org:pkozelka/buildbox.git");
        final VcsRepositoryMatch match = reg.findByScmUrl("scm:svn:http://releasator.svn.sourceforge.net/svnroot/releasator/trunk/releasator-cli");

        System.out.println("match = " + match);
        Assert.assertNotNull(match);
        final ScmRepository scmRepository = match.getScmRepository();
        final File tempFile = File.createTempFile("scm-", "-git");
        final File basedir = new File(tempFile.getAbsolutePath() + ".d");
        System.out.println("basedir = " + basedir);

        final CheckOutScmResult result = scmManager.checkOut(scmRepository, new ScmFileSet(basedir));

        System.out.println("result.getCommandLine() = " + result.getCommandLine());
        System.out.println("result.getProviderMessage() = " + result.getProviderMessage());
        System.out.println("result.getCommandOutput() = " + result.getCommandOutput());
        final List<ScmFile> checkedOutFiles = result.getCheckedOutFiles();
        if (checkedOutFiles != null) {
            System.out.println("result.getCheckedOutFiles() = " + checkedOutFiles.size());
            for (ScmFile scmFile : checkedOutFiles) {
                System.out.println(scmFile.getStatus() + " " + scmFile.getPath());
            }
        }
    }

    private VcsRegistry createDemoRegistry() {
        final VcsRegistry reg = new VcsRegistryImpl(scmManager);
        reg.register(createSourceforgeSvn());
        reg.register(loadVfc("git.bitbucket.xml"));
        return reg;
    }

    private static VcsFactoryConfig loadVfc(String resourceName) {
        final XStream xstream = vcsXstream();
        return (VcsFactoryConfig) xstream.fromXML(VcsRegistryTest.class.getResourceAsStream(resourceName));
    }

    private static VcsFactoryConfig createSourceforgeSvn() {
        final VcsFactoryConfig config = new VcsFactoryConfig();
        config.setVcsIdMask("sf.{REPOURI}");
        config.setVcsType("svn");
        // scm:svn:https://releasator.svn.sourceforge.net/svnroot/releasator/trunk/releasator-cli
        config.setScmUrlMasks(Arrays.asList(
                "scm:svn:http://{REPOURI}.svn.sourceforge.net/svnroot/{REPOURI::([^/]*)}/{PATH}",
                "scm:svn:https://{REPOURI}.svn.sourceforge.net/svnroot/{REPOURI::([^/]*)}/{PATH}"));
        return config;
    }

    private static XStream vcsXstream() {
        final XStream xstream = new XStream();
        xstream.alias("VcsFactoryConfig", VcsFactoryConfig.class);
        xstream.aliasField("type", VcsFactoryConfig.class, "vcsType");
        xstream.aliasField("idMask", VcsFactoryConfig.class, "vcsIdMask");
        xstream.addImplicitCollection(VcsFactoryConfig.class, "scmUrlMasks", "scmUrlMask", String.class);
//        xstream.omitField(VcsFactoryConfig.class, "file");
        return xstream;
    }
}
