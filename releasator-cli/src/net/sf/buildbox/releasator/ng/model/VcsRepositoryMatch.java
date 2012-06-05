package net.sf.buildbox.releasator.ng.model;

import org.apache.maven.scm.repository.ScmRepository;

import java.util.Map;

/**
 * @author Petr Kozelka
 */
public class VcsRepositoryMatch {
    /**
     * parameter BRANCH - cannot contain slash
     */
    public static final String PARAM_BRANCH = "BRANCH";
    /**
     * parameter PATH - without leading slash
     */
    public static final String PARAM_PATH = "PATH";

    private VcsFactoryConfig vcsFactoryConfig;
    private VcsRepository vcsRepository;
    private ScmRepository scmRepository;
    private String matchedMask;
    private Map<String,String> matchedParams;

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

    public ScmRepository getScmRepository() {
        return scmRepository;
    }

    public void setScmRepository(ScmRepository scmRepository) {
        this.scmRepository = scmRepository;
    }

    public String getMatchedMask() {
        return matchedMask;
    }

    public void setMatchedMask(String matchedMask) {
        this.matchedMask = matchedMask;
    }

    public Map<String, String> getMatchedParams() {
        return matchedParams;
    }

    public void setMatchedParams(Map<String, String> matchedParams) {
        this.matchedParams = matchedParams;
    }

    public String getBranchAndPath() {
        final String branch = matchedParams.get(PARAM_BRANCH);
        final String path = matchedParams.get(PARAM_PATH);
        final StringBuilder sb = new StringBuilder();
        if (branch != null) {
            sb.append(branch);
        }
        sb.append("/");
        if (path != null) {
            sb.append(path);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("VcsRepositoryMatch");
        sb.append("{matchedMask='").append(matchedMask).append('\'');
        sb.append(", matchedParams='").append(matchedParams).append('\'');
        sb.append(", vcsRepository=").append(vcsRepository);
        sb.append(", vcsFactoryConfig=").append(vcsFactoryConfig);
        sb.append('}');
        return sb.toString();
    }
}
