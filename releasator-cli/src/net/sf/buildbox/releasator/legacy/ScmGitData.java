package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.IOException;

public class ScmGitData extends ScmData {
    public ScmGitData(String scm) {
        super(scm);
    }

    public String checkout(File dest, File log) throws IOException, InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
        return null;
    }

    public ScmData getTagScm(String scmTag) throws IOException, InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getVcsType() {
        return "git";
    }

    public String getVcsId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getVcsPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
