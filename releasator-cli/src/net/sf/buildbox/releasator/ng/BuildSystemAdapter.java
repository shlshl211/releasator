package net.sf.buildbox.releasator.ng;

import java.io.File;
import java.util.List;

public interface BuildSystemAdapter {
    /**
     * Changes all references to project version with the release version in current working copy.
     * Also prepares build files to strictly reproducible build mode.
     * @param releaseVersion the release version to set
     * @return list of explored (and potentially affected) files
     */
    List<File> switchToRelease(String releaseVersion);

    /**
     * Changes all references to project version with the snapshot version in current working copy.
     * Also prepares build files to normal working mode.
     * @param snapshotVersion the version to set
     * @return list of explored (and potentially affected) files
     */
    List<File> switchToSnapshot(String snapshotVersion);

    /**
     * Builds the project in current working copy; produces:
     * - logs
     * - list of output artifacts
     * - list of dependencies
     */
    void build();

    /**
     * @return list of module code roots
     */
    List<File> listModules();
}
