package net.sf.buildbox.releasator.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Params {
    static final String RELEASE_PLUGIN_GROUPID = "org.apache.maven.plugins";
    static final String RELEASE_PLUGIN_ARTIFACTID = "maven-release-plugin";

    static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    public static final String releasatorVersion;

    static {
        final InputStream is = JReleasator.class.getResourceAsStream("/META-INF/build.properties");
        try {
            final Properties p = new Properties();
            p.load(is);
            String v = p.getProperty("version");
            if (v.endsWith(Params.SNAPSHOT_SUFFIX)) {
                v += "-" + p.getProperty("build.time");
            }
            releasatorVersion = v;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static final String RELEASATOR_PREFIX = "[releasator]";

    private Params() {
        // not instantiable
    }


}
