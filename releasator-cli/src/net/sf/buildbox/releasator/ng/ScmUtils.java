package net.sf.buildbox.releasator.ng;

import java.io.File;

public class ScmUtils {
    /**
     * @param scmUrl -
     * @return helper string for creating temporary directory; it should be deterministically derived from scm url, nice enough to remind the location
     */
    public static String scmUrlAsFileName(String scmUrl) {
        final int n = scmUrl.indexOf(':', 4);
        String s = scmUrl.substring(n + 1);
        s = s.replace(':', '_');
        s = s.replace('/', '_');
        return s;
    }

    public static File workDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
