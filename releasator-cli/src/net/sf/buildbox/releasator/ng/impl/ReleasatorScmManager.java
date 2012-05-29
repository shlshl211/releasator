package net.sf.buildbox.releasator.ng.impl;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;

/**
 * @author Petr Kozelka
 */
public class ReleasatorScmManager extends AbstractScmManager {

    public ReleasatorScmManager() {
        setScmProvider("svn", new SvnExeScmProvider());
        setScmProvider("git", new GitExeScmProvider());
    }

    @Override
    protected ScmLogger getScmLogger() {
        return new ReleasatorScmLogger();
    }
}
