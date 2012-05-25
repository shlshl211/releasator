package net.sf.buildbox.releasator.ng.api;

import net.sf.buildbox.releasator.ng.model.VcsRepository;

import java.io.File;

/**
 * @author Petr Kozelka
 */
public interface VcsProvider {
    void checkout(VcsRepository vcs, File wc, String uri);
    void commit(VcsRepository vcs, File wc, String message);
    void addfiles(VcsRepository vcs, File wc, String ... uris);
    void tag(VcsRepository vcs, String revision, String tagName);
    void lock(VcsRepository vcs, File wc, String message);
    void unlock(VcsRepository vcs, File wc);
}
