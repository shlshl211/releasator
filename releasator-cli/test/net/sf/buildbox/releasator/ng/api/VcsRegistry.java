package net.sf.buildbox.releasator.ng.api;

import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;

/**
 * @author Petr Kozelka
 */
public interface VcsRegistry {
    void register(VcsFactoryConfig vcsFactoryConfig);
    VcsRepositoryMatch findByVcsId(String vcsId);
    VcsRepositoryMatch findByScmUrl(String scmUrl);
}
