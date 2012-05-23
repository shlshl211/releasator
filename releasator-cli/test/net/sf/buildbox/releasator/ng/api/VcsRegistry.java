package net.sf.buildbox.releasator.ng.api;

import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepository;

import java.io.File;

/**
 * @author Petr Kozelka
 */
public interface VcsRegistry {
    void register(VcsFactoryConfig vcsFactoryConfig);
    void register(File vcsFactoryConfigFile);
    VcsRepository findByVcsId(String vcsId);
    VcsRepository findByConnection(String connection);
}
