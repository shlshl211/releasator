package net.sf.buildbox.releasator.legacy;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JReleasatorTest {

    @Test
    public void showCurrentTimeAsUTC() throws Exception {
        System.out.println("JReleasator.formatCurrentTime() = " + MyUtils.formatCurrentTime());
    }

    @Test
    public void mavenVersionTest() throws Exception {
        String v = MyUtils.getMavenVersion();
        System.out.println("v = " + v);
    }

    @Test
    public void getVcsPathSvn() throws Exception {
        final ScmData scm = ScmData.valueOf("scm:svn:https://buildbox.svn.sourceforge.net/svnroot/buildbox/releasator/trunk/releasator-cli");
        Assert.assertEquals("vcsId", "sf.buildbox", scm.getVcsId());
        Assert.assertEquals("vcsPath", "/releasator/trunk/releasator-cli", scm.getVcsPath());
        final ScmData tag = scm.getTagScm("MY-TAG");
        Assert.assertEquals("tag", "scm:svn:https://buildbox.svn.sourceforge.net/svnroot/buildbox/releasator/tags/MY-TAG", tag.toString());
        Assert.assertEquals("tag.vcsPath", "/releasator/tags/MY-TAG", tag.getVcsPath());
    }

    @Ignore("NOT WORKING YET")
    @Test
    public void getVcsPathGit() throws Exception {
        final ScmData scm = ScmData.valueOf("scm:git:git@github.com:buildbox/contentcheck-maven-plugin.git");
        Assert.assertEquals("vcsId", "github.buildbox_contentcheck-maven-plugin", scm.getVcsId());
        Assert.assertEquals("vcsPath", "/", scm.getVcsPath());
        final ScmData tag = scm.getTagScm("MY-TAG");
        Assert.assertEquals("tag", "scm:svn:https://buildbox.svn.sourceforge.net/svnroot/buildbox/tags/MY-TAG", tag.toString());
        Assert.assertEquals("tag.vcsPath", "/tags/MY-TAG", tag.getVcsPath());
    }
}
