package net.sf.buildbox.releasator.ng.api;

import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;
import org.apache.maven.scm.manager.ScmManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public interface VcsRegistry {
    void register(VcsFactoryConfig vcsFactoryConfig);
    VcsRepositoryMatch findByVcsId(String vcsId);
    VcsRepositoryMatch findByScmUrl(String scmUrl);

    void loadConf(File confDir) throws IOException;

    ScmManager getScmManager();

    List<VcsFactoryConfig> list();
}
