package net.sf.buildbox.releasator.ng;

import java.io.File;

/**
 * Interface to scm repository operations on given scm url.
 * Needs a space on disk, where it creates a working copy.
 * Instances should be created by specific implementations of {@link net.sf.buildbox.releasator.ng.ScmAdapterFactory}
 */
public interface ScmAdapter {
    /**
     * @return the original scm url
     */
    String getScmUrl();

    /**
     * computes full tag name, given all module coordinates
     *
     * @param groupId    -
     * @param artifactId -
     * @param version    -
     * @return computed tag name
     */
    String getFullTagName(String groupId, String artifactId, String version);

    /**
     * @param fullTagName
     * @return
     */
    String getTagCheckoutCommandHint(String fullTagName);

    boolean isTagPresent(String fullTagName);

    /**
     * @return the directory where working copy was created
     */
    File getCodeDirectory();

    /**
     * Creates working copy of a remote repository specified when creating this instance
     */
    void checkout();

    /**
     * Locks the previously checked-out working copy.
     * Fails if working copy, it's part, or it's container is already locked.
     *
     * @return an opaque lock key that must be passed to unlock later.
     */
    String lock(String comment);

    /**
     * Unlocks the previously locked working copy
     *
     * @param lock the key obtained by a lock
     */
    void unlock(String lock);

    /**
     * Commits modifications in working copy to the repository.
     *
     * @param message
     */
    void commit(String message);

    /**
     * Tags the code directory
     *
     * @param fullTagName
     */
    void tag(String fullTagName, String commitMessage);

    /**
     * For distributed vcs only - pushes the changes upstream.
     */
    void push();
}
