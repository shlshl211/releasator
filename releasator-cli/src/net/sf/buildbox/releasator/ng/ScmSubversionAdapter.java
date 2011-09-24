package net.sf.buildbox.releasator.ng;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.buildbox.releasator.legacy.MyUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

public class ScmSubversionAdapter implements ScmAdapter {
    private final ScmSubversionAdapterFactory factory;
    private final String scmUrl;
    private final String svnUrl;
    private final File jobDir;
    private File codeDir;
    private final String tagBase;

    public ScmSubversionAdapter(ScmSubversionAdapterFactory factory, String scmUrl, File jobDir) {
        this.factory = factory;
        this.scmUrl = scmUrl;
        this.jobDir = jobDir;
        // compute specific url
        final int n = scmUrl.indexOf(':', 4);
        this.svnUrl = scmUrl.substring(n + 1);
        this.tagBase = factory.computeSvnTagBase(svnUrl);
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void checkout() {
        final File wc = new File(jobDir, "code");
        try {
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-checkout");
            wc.mkdirs();
            MyUtils.loggedCmd(logFile, wc, "svn", "checkout", "--non-interactive", "--ignore-externals", svnUrl, ".");
            codeDir = wc;
        } catch (IOException e) {
            throw new ScmException(svnUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    public void commit(String message) {
        try {
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-commit");
            final String commitFile = new File(logFile, ".commit.txt").getAbsolutePath();
            FileUtils.fileWrite(commitFile, message);
            MyUtils.loggedCmd(logFile, codeDir, "svn", "commit", "--non-interactive", "--no-unlock", "--file", commitFile);
        } catch (IOException e) {
            throw new ScmException(svnUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    public String getFullTagName(String groupId, String artifactId, String version) {
        return groupId + "-" + artifactId + "-" + version;
    }

    public String getTagCheckoutCommandHint(String fullTagName) {
        return scmUrl.substring(0, scmUrl.length() - svnUrl.length()) + tagBase + "/" + fullTagName;
    }

    public boolean isTagPresent(String fullTagName) {
        try {
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-info");
            MyUtils.loggedCmd(logFile, codeDir, "svn", "info", "--non-interactive");
            return true;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    public File getCodeDirectory() {
        return codeDir;
    }

    public String lock(String comment) {
        try {
            final String[] cmdline = lockCmdLine("lock");
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-lock");
            MyUtils.loggedCmd(logFile, codeDir, "svn", cmdline);
            return "LOCK{" + codeDir.getAbsolutePath() + "}";
        } catch (IOException e) {
            throw new ScmException(svnUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    public void unlock(String lockKey) {
        try {
            final String[] cmdline = lockCmdLine("unlock");
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-unlock");
            MyUtils.loggedCmd(logFile, codeDir, "svn", cmdline);
        } catch (IOException e) {
            throw new ScmException(svnUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    private String[] lockCmdLine(String command) {
        final String[] files = codeDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(codeDir, name).isFile();
            }
        });
        final List<String> args = new ArrayList<String>();
        args.add(command);
        args.add("--non-interactive");
        args.addAll(Arrays.asList(files));
        return args.toArray(new String[args.size()]);
    }

    public void tag(String fullTagName, String commitMessage) {
        try {
            final String svnTagUrl = tagBase + "/" + fullTagName;
            final File logFile = MyUtils.nextLogFile(jobDir, "svn-cp");
            final String messageFile = new File(logFile, ".commit.txt").getAbsolutePath();
            FileUtils.fileWrite(messageFile, commitMessage);
            MyUtils.loggedCmd(logFile, codeDir, "svn", "cp", "--non-interactive", "--no-unlock", "--file", messageFile, svnUrl, svnTagUrl);
        } catch (IOException e) {
            throw new ScmException(svnUrl, e);
        } catch (InterruptedException e) {
            throw new ScmException(svnUrl, e);
        } catch (CommandLineException e) {
            throw new ScmException(svnUrl, e);
        }
    }

    public void push() {
        // ignored - not used in svn
    }
}
