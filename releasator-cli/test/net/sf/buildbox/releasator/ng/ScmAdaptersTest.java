package net.sf.buildbox.releasator.ng;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ScmAdaptersTest {
    ScmAdapterManager scmManager;

    @Before
    public void setUp() {
        // this will be done by spring
        scmManager = new ScmAdapterManager();
        final Map<String, ScmAdapterFactory> scmAdapters = new HashMap<String, ScmAdapterFactory>();
        final ScmSubversionAdapterFactory svn = new ScmSubversionAdapterFactory();
        scmAdapters.put("svn", svn);
        final ScmGitAdapterFactory git = new ScmGitAdapterFactory();
        scmAdapters.put("git", git);
        scmManager.setAdapters(scmAdapters);
        //
    }

    @Test
    public void testSvn() {
        final ScmAdapter scm = scmManager.create("scm:svn:https://buildbox.svn.sourceforge.net/svnroot/buildbox/trunk/tools/releasator");
//        scm.checkout();
        // TODO
    }

    @Test
    @Ignore("git not yet available")
    public void testGit() throws org.apache.maven.scm.ScmException {
        final ScmAdapter scm = scmManager.create("scm:git:git://github.com/buildbox/contentcheck-maven-plugin.git");
        scm.checkout();
        // TODO
    }
}
