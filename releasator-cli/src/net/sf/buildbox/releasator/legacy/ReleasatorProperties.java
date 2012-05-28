package net.sf.buildbox.releasator.legacy;

import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.*;
import java.util.Properties;

public class ReleasatorProperties {

    public static final String CFG_MRP_VERSION = "maven-release-plugin.version";
    public static final String CFG_SETTINGS_ID_BYGAV_PREFIX = "settings.id.byGav-";
    public static final String CFG_MAVEN_VERSION_PREFIX = "maven-";
    public static final String CFG_JDK_VERSION_PREFIX = "jdk-";
    public static final String CFG_ANT_EXECUTABLE = "ant";
    public static final String CFG_ANT_ARGS = "ant.args";
    public static final String CFG_ANT_JDK_VERSION = "ant.jdk.version";
    //Note: these two must be used with care; deploy build can easily create new artifacts with slightly different values (due to timestamping)
    public static final String CFG_RECORD_FILE_SIZE = "record.file.size";
    public static final String CFG_RECORD_FILE_CHECKSUM = "record.file.checksum";
    public static final String CONFIGURATION_FILENAME = "releasator.properties";

    private final Properties properties = new Properties();
    private final File file;

    public ReleasatorProperties(File conf) throws IOException {
        file = new File(conf, CONFIGURATION_FILENAME);
        if (!file.exists()) {
            initializeConfiguration(conf);
        }
        final InputStream is = new FileInputStream(file);
        try {
            properties.load(is);
        } finally {
            is.close();
        }
    }

    private void initializeConfiguration(File conf) throws IOException {
        // legacy
        conf.mkdirs();
        FileUtils.copyURLToFile(getClass().getResource("/etc/default.releasator.properties"), file);

        // ng
        final File confDir = new File(conf.getParent(), "releasator");

        if (! confDir.exists()) {
        }
    }

    public static void validateConf(File conf) throws FileNotFoundException {
        final File file = new File(conf, CONFIGURATION_FILENAME);
        if (! file.isFile()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }


    public String getReleasatorProperty(String propertyName, boolean required) throws IOException {
        final String result = properties.getProperty(propertyName);
        if (required && result == null) {
            throw new IllegalArgumentException(String.format("Required property %s is not defined in %s",
                    propertyName, file));
        }
        return result;
    }


    public Iterable<Object> keys() {
        return properties.keySet();
    }

    public void configureJava(Commandline cl, String jdkVersion) throws IOException {
        if (jdkVersion != null) {
            final String jdkHome = getReleasatorProperty(CFG_JDK_VERSION_PREFIX + jdkVersion, true);
            final File javac = new File(jdkHome, "bin/javac");
            if (!javac.isFile()) {
                throw new IllegalArgumentException(jdkHome + " : not a valid JDK home directory");
            }
            cl.addEnvironment("JAVA_HOME", jdkHome);
            cl.addEnvironment("PATH", javac.getParent() + File.pathSeparatorChar + System.getenv("PATH"));
        } else {
            System.out.println("WARNING: using default (unspecified) java, the one on PATH");
        }
    }
}
