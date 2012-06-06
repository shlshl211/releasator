package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import net.sf.buildbox.changes.ChangesController;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.*;

public class MyUtils {
    private static final Pattern MAVEN_22 = Pattern.compile("^Apache Maven ([\\.\\d]+) .*$");
    static final String MAVEN_VERSION_PREFIX = "Maven version: ";
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    static Transformer xsltPomTransformer() throws TransformerConfigurationException {
        return TRANSFORMER_FACTORY.newTransformer(new StreamSource(MyUtils.class.getResourceAsStream("/etc/set-scm.xsl")));
    }


    public static final StreamConsumer STDOUT_CONSUMER = new StreamConsumer() {
        public void consumeLine(String s) {
            System.out.println(s);
        }
    };

    public static final StreamConsumer STDERR_CONSUMER = new StreamConsumer() {
        public void consumeLine(String s) {
            System.err.println(s);
        }
    };


    static String getMavenVersion() throws IOException, InterruptedException {
        final AtomicReference<String> result = new AtomicReference<String>();
        final Commandline cle = new Commandline("mvn --version");
        final StreamConsumer stdout = new StreamConsumer() {
            public void consumeLine(String s) {
                final Matcher m22 = MAVEN_22.matcher(s);
                if (m22.matches()) {
                    result.set(m22.group(1));
                } else if (s.startsWith(MAVEN_VERSION_PREFIX)) {
                    result.set(s.substring(MAVEN_VERSION_PREFIX.length()));
                }
            }
        };
        try {
            if (CommandLineUtils.executeCommandLine(cle, stdout, stdout, 10) != 0) {
                throw new IOException(cle + " execution failed");
            }
            if (result.get() == null) {
                throw new IOException("Unable to detect version of Maven");
            }
            return result.get();

        } catch (CommandLineException e) {
            throw new IOException(e.getMessage());
        }
    }

    static void doCmd(File dir, String command, String... args) throws IOException, InterruptedException, CommandLineException {
        final Commandline cl = new Commandline();
        cl.setWorkingDirectory(dir);
        cl.setExecutable(command);
        cl.addArguments(args);
        doCmd(cl);
    }

    static void doCmd(Commandline cl) throws CommandLineException, IOException {
        System.out.println("# Executing shell: " + cl.getShell().getShellCommand());
        if (cl.getWorkingDirectory() != null) {
            System.out.println("# cd " + cl.getWorkingDirectory());
        }
        System.out.println("# " + CommandLineUtils.toString(cl.getCommandline()));
        final int exitCode = CommandLineUtils.executeCommandLine(cl, STDOUT_CONSUMER, STDERR_CONSUMER);
        if (exitCode != 0) {
            throw new IOException("exitCode = " + exitCode);
        }
    }

    public static void loggedCmd(File logFile, File dir, String command, String... args) throws IOException, InterruptedException, CommandLineException {
        final Commandline cl = new Commandline();
        cl.setWorkingDirectory(dir);
        cl.setExecutable(command);
        cl.addArguments(args);
        loggedCmd(logFile, cl);
    }

    public static void loggedCmd(File logFile, Commandline cl) throws IOException, InterruptedException, CommandLineException {
        System.err.println("logging to " + logFile);
        final PrintWriter pw = new PrintWriter(logFile);
        try {

            System.out.println("# Executing shell: " + cl.getShell().getShellCommand());
            if (cl.getWorkingDirectory() != null) {
                System.out.println("# cd " + cl.getWorkingDirectory());
                pw.println("# cd " + cl.getWorkingDirectory());
            }
            System.out.println("# " + CommandLineUtils.toString(cl.getCommandline()));
            pw.println("# " + CommandLineUtils.toString(cl.getCommandline()));

            final StreamConsumer myOut = new WriterStreamConsumer(pw);
            final StreamConsumer myErr = new StreamConsumer() {
                public void consumeLine(String s) {
                    System.err.println("ERR: " + s);
                    pw.println("ERR: " + s);
                    pw.flush();
                }
            };

            final int exitCode = CommandLineUtils.executeCommandLine(cl, myOut, myErr);
            if (exitCode != 0) {
                throw new IOException("exitCode = " + exitCode);
            }
        } finally {
            pw.close();
        }
    }

    /**
     * Formats current time as UTC in ISO.
     *
     * @return ISO formated UTC time.
     */
    static String formatCurrentTime() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    static void checkVersionFormat(String releaseVersion) {
        if (!releaseVersion.matches("^\\w+\\.\\w+\\.\\w+(-(alpha|beta|rc|patch\\.\\w+))?(-\\w+)?$")) {
            throw new IllegalArgumentException("Invalid version syntax: " + releaseVersion);
        }
    }

    static void assertValidAuthor(String author) {
        if (author == null || author.trim().length() == 0) {
            throw new IllegalArgumentException("No author specified");
        }
    }

    static void checkChangesXml(ChangesController chg, ParsedPom top) throws IOException {
        final String pomArtifactId = top.artifactId;
        final String pomVersion = top.version;
        final String releaseArtifactId = chg.getArtifactId();
        final String unreleasedVersion = chg.getVersion();
        if (!unreleasedVersion.endsWith(Params.SNAPSHOT_SUFFIX)) {
            throw new IOException("changes.xml: unreleased version must end with '-SNAPSHOT'");
        }

        // check that version specified in changes.xml equals to pom.xml's version
        if (!pomVersion.equals(unreleasedVersion)) {
            throw new IOException(String.format("version mismatch - changes.xml/unreleased/@version(='%s') differs from version of pom.xml (='%s')",
                    unreleasedVersion, pomVersion));
        }
        // check that artifactId specified in changes.xml complies to pom.xml's artifactId
        if (pomArtifactId.equals(releaseArtifactId)) {
            // ok, exactly the same
        } else if (pomArtifactId.startsWith(releaseArtifactId + "-")) {
            // ok, suffixed
        } else {
            throw new IOException(String.format("naming mismatch - changes.xml/unreleased/@artifactId<>pom.xml/artifactId: '%s'<>'%s'",
                    releaseArtifactId, pomArtifactId));
        }
        // check that top pom's groupId matches changes.xml
        if (!top.groupId.equals(chg.getGroupId())) {
            throw new IOException(String.format("naming mismatch - changes.xml/unreleased/@groupId<>pom.xml/groupId: '%s'<>'%s'",
                    chg.getGroupId(), top.groupId));
        }

    }

    static Properties prepareReleaseProps(String scmUrl, ChangesController chg) throws IOException, InterruptedException {
        final Properties props = new Properties();
        props.setProperty("completedPhase", "check-dependency-snapshots");
        props.setProperty("scm.url", scmUrl);
        props.setProperty("releasator", Params.releasatorVersion); //TODO: check that it works!
        final Map<String, String> rlsprops = chg.getReleaseConfigProperties();
        for (Map.Entry<String, String> entry : rlsprops.entrySet()) {
            if (entry.getKey().startsWith(ChangesController.RLSCFG_MAVEN_PREFIX)) {
                props.setProperty(entry.getKey().substring(ChangesController.RLSCFG_MAVEN_PREFIX.length()), entry.getValue());
            }
        }
        return props;
    }

    static Map<File, ParsedPom> parseAllPoms(File topPomFile) throws IOException {
        final Map<File, ParsedPom> allPoms = new LinkedHashMap<File, ParsedPom>();
        final LinkedList<File> unparsedPoms = new LinkedList<File>();
        unparsedPoms.add(topPomFile);
        while (!unparsedPoms.isEmpty()) {
            final File pomFile = unparsedPoms.removeFirst();
            final File dir = pomFile.getParentFile();
            final ParsedPom pom = ParsedPom.parse(pomFile);
            for (String module : pom.modules) {
                unparsedPoms.add(new File(dir, module + "/pom.xml").getCanonicalFile());
            }
            allPoms.put(pomFile, pom);
        }
        return allPoms;
    }

    static String subpath(File root, File child) {
        return child.getAbsolutePath().substring(root.getAbsolutePath().length());
    }

    private static int logCnt = 0;

    public static File nextLogFile(File dir, String command) {
        logCnt++;
        return new File(dir, String.format("%02d-%s.log", logCnt, command));
    }

    public static List<String> getConfiguredArgs(ChangesController chg, String propertyName) {
        final List<String> result = new ArrayList<String>();
        final String propertyValue = chg.getReleaseConfigProperty(propertyName);
        if (propertyValue != null) {
            result.add("-Darguments=" + propertyValue);
        }
        return result;
    }
}
