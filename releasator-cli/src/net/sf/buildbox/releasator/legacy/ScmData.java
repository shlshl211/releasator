package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.IOException;

public abstract class ScmData {
    public static final String SCM_SVN_PREFIX = "scm:svn:";
    protected final String scm;

    public static ScmData valueOf(String scm) {
        if (!scm.startsWith("scm:")) {
            scm = SCM_SVN_PREFIX + scm;
        }
        if (scm.startsWith(SCM_SVN_PREFIX)) {
            return new ScmSvnData(scm);
        }
        throw new UnsupportedOperationException("unsupported version control system: " + scm);
    }

    ScmData(String scm) {
        this.scm = scm;
    }

    @Override
    public String toString() {
        return scm;
    }

    public abstract String checkout(File dest, File log) throws IOException, InterruptedException;

    public abstract ScmData getTagScm(String scmTag) throws IOException, InterruptedException;

    public abstract String getVcsId();

    public abstract String getVcsPath();
}
