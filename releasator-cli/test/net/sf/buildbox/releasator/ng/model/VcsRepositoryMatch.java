package net.sf.buildbox.releasator.ng.model;

/**
 * @author Petr Kozelka
 */
public class VcsRepositoryMatch {
    private VcsFactoryConfig vcsFactoryConfig;
    private VcsRepository vcsRepository;
    private String matchedMask;
    private String path;

    public VcsFactoryConfig getVcsFactoryConfig() {
        return vcsFactoryConfig;
    }

    public void setVcsFactoryConfig(VcsFactoryConfig vcsFactoryConfig) {
        this.vcsFactoryConfig = vcsFactoryConfig;
    }

    public VcsRepository getVcsRepository() {
        return vcsRepository;
    }

    public void setVcsRepository(VcsRepository vcsRepository) {
        this.vcsRepository = vcsRepository;
    }

    public String getMatchedMask() {
        return matchedMask;
    }

    public void setMatchedMask(String matchedMask) {
        this.matchedMask = matchedMask;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
