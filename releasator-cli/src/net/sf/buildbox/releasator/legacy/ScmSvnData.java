package net.sf.buildbox.releasator.legacy;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

class ScmSvnData extends ScmData {
    private static final Pattern SVN_SOURCEFORGE = Pattern.compile("^https?://(.*)\\.svn\\.sourceforge\\.net/svnroot/([^/]+)(/.*)$");
    private static final Pattern SVN_GOOGLECODE = Pattern.compile("^https?://(.*)\\.googlecode\\.com/svn(/.*)$");
    private static final Pattern SVN_CUSTOM = Pattern.compile("https://svn\\.(\\w+).*/rg\\d\\d\\d\\d/([\\w-]+)(.*)$");
    private static final Pattern SVN_MANY = Pattern.compile("^[\\w-]+:.*\\.([\\w-]+)\\.[\\w-]+/svn/([\\w-]+)(/.*)$");
    private static final Pattern CHECKED_OUT_REVISION = Pattern.compile("^Checked out revision (\\d+).$");
    private String repoRoot;
    private String vcsId;


    ScmSvnData(String scm) {
        super(scm);
    }

    /**
     * fills vcsId and repoRoot
     */
    private void analyzeValidUrl() {
        if (repoRoot == null) {
            final String svnUrl = scm.substring(SCM_SVN_PREFIX.length());
            try {
                repoRoot = SCM_SVN_PREFIX + svnInfoGet(svnUrl, "Repository Root");
            } catch (CommandLineException e) {
                throw new IllegalArgumentException(svnUrl);
            } catch (IOException e) {
                throw new IllegalArgumentException(svnUrl);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            }
        }

        if (vcsId == null) {
System.out.println("ANALYZING " + scm);
            final String svnUrl = scm.substring(SCM_SVN_PREFIX.length());
            if (svnUrl.startsWith("file://")) {
                vcsId = "local.svn";
                return;
            }
            final Matcher sfMatcher = SVN_SOURCEFORGE.matcher(svnUrl);
            if (sfMatcher.matches()) {
                vcsId = "sf." + sfMatcher.group(1);
                return;
            }
            final Matcher gcMatcher = SVN_GOOGLECODE.matcher(svnUrl);
            if (gcMatcher.matches()) {
                vcsId = "googlecode." + sfMatcher.group(1);
                return;
            }
            final Matcher manyMatcher = SVN_MANY.matcher(svnUrl);
            if (manyMatcher.matches()) {
                vcsId = manyMatcher.group(1) + "." + manyMatcher.group(2);
                return;
            }
            final Matcher customMatcher = SVN_CUSTOM.matcher(svnUrl);
            if (customMatcher.matches()) {
                vcsId = customMatcher.group(1) + "." + customMatcher.group(2);
                return;
            }
            throw new IllegalArgumentException("xx Unknown SVN repository: " + svnUrl);
        }
    }

    public ScmData getTagScm(String scmTag) throws IOException, InterruptedException {

        int n = scm.indexOf("/trunk/");
        if (n < 0) {
            if (scm.endsWith("/trunk")) {
                n = scm.length() - 6;
            } else {
                n = scm.indexOf("/branches/");
            }
        }
        if (n < 0) {
            n = scm.indexOf("/tags/");
        }
        if (n < 0) {
            throw new UnsupportedOperationException("releasing from path in a non-standard directory layout is not implemented yet");
        }
        final String scmBaseUrl = scm.substring(0, n);
        final ScmSvnData tag = (ScmSvnData) ScmData.valueOf(scmBaseUrl + "/tags/" + scmTag);
        analyzeValidUrl();
        tag.repoRoot = repoRoot;
        tag.vcsId = vcsId;
        return tag;
    }

    private String getRepoUrl() {
        analyzeValidUrl();
        return repoRoot;
    }

    public String getVcsPath() {
        return scm.substring(getRepoUrl().length());
    }

    public String getVcsId() {
        analyzeValidUrl();
        return vcsId;
    }

    private static String svnInfoGet(String svnUrl, String info) throws IOException, InterruptedException, CommandLineException {
        final String prefix = info + ": ";
        final AtomicReference<String> result = new AtomicReference<String>();
        final Commandline cl = new Commandline("svn info "+ svnUrl);
        System.err.println("# " + cl);
        final StreamConsumer mycon = new StreamConsumer() {
            public void consumeLine(String s) {
                if (s.startsWith(prefix)) {
                    result.set(s.substring(prefix.length()));
                }
            }
        };
        final int exitCode = CommandLineUtils.executeCommandLine(cl, mycon, MyUtils.STDERR_CONSUMER);
        if (exitCode != 0) {
            throw new IOException("exitCode = " + exitCode);
        }
        if (result.get() == null) {
            throw new IOException("No such svn info found: " + info);
        }
        return result.get();
//        return "http://tools.assembla.com/svn/dejavu";
    }


    public String checkout(File dest, File log) throws IOException, InterruptedException {
        final String svnUrl = scm.substring(SCM_SVN_PREFIX.length());
        dest.mkdirs();
        try {
            MyUtils.loggedCmd(log, dest, "svn", "checkout", "--ignore-externals", svnUrl, ".");
            final String lastLine = readLastLine(log);
            final Matcher m = CHECKED_OUT_REVISION.matcher(lastLine);
            if (! m.matches()) {
                throw new IllegalStateException("Unable to determine revision");
            }
            return m.group(1);

        } catch (CommandLineException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static String readLastLine(File file) throws IOException {
        final BufferedReader r = new BufferedReader(new FileReader(file));
        String lastLine = r.readLine();
        String line = lastLine == null ? null : r.readLine();
        while (line != null) {
            lastLine = line;
            line = r.readLine();
        }
        return lastLine;
    }
}
