package net.sf.buildbox.releasator.ng.impl;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;

/**
 * @author Petr Kozelka
 */
public class ReleasatorScmManager extends AbstractScmManager {
    @Override
    protected ScmLogger getScmLogger() {
        return new ReleasatorScmLogger();
    }
}
