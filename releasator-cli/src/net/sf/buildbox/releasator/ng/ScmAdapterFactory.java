package net.sf.buildbox.releasator.ng;

public interface ScmAdapterFactory {
    public ScmAdapter create(String scmUrl);
}
