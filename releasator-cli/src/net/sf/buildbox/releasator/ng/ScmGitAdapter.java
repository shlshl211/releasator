package net.sf.buildbox.releasator.ng;

import java.io.File;
import java.io.IOException;
import net.sf.buildbox.releasator.legacy.MyUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

public class ScmGitAdapter implements ScmAdapter {
    private final ScmGitAdapterFactory factory;
    private final String scmUrl;
    private final String gitCloneUrl;
    private final String gitPushUrl;
    private final File jobDir;
    private File codeDir;

    public ScmGitAdapter(ScmGitAdapterFactory scmGitAdapterFactory, String scmUrl, File jobDir) {
        factory = scmGitAdapterFactory;
        this.scmUrl = scmUrl;
        this.jobDir = jobDir;
        // compute specific urls
        final int n = scmUrl.indexOf(':', 4);
        this.gitCloneUrl = scmUrl.substring(n + 1); //todo: support full syntax
        this.gitPushUrl = scmUrl.substring(n + 1); //todo: support full syntax
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void checkout() {
        final File wc = new File(jobDir, "code");
        try {
            final File logFile = MyUtils.nextLogFile(jobDir, "git-clone");
            wc.mkdirs();
            MyUtils.loggedCmd(logFile, wc, "git", "clone", gitCloneUrl, ".");
            codeDir = wc;
        } catch (IOException e) {
            throw new ScmException(gitCloneUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(gitCloneUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(gitCloneUrl, e);
        }
    }

    public void commit(String message) {
    }

    public String getFullTagName(String groupId, String artifactId, String version) {
        return String.format(factory.getTagFormatString(), groupId, artifactId, version);
    }

    public String getTagCheckoutCommandHint(String fullTagName) {
        //todo: needs to be redefined to allow implementation for git
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTagPresent(String fullTagName) {
        return false;
    }

    public File getCodeDirectory() {
        return codeDir;
    }

    public String lock(String comment) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unlock(String lock) {

    }

    public void tag(String fullTagName, String commitMessage) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void push() {
        final File wc = new File(jobDir, "code");
        try {
            final File logFile = MyUtils.nextLogFile(jobDir, "git-push");
            wc.mkdirs();
            MyUtils.loggedCmd(logFile, wc, "git", "push", gitPushUrl, ".");
            codeDir = wc;
        } catch (IOException e) {
            throw new ScmException(gitCloneUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(gitCloneUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(gitCloneUrl, e);
        }
    }
}
