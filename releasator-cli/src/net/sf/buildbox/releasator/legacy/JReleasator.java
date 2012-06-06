package net.sf.buildbox.releasator.legacy;

import net.sf.buildbox.args.annotation.Option;
import net.sf.buildbox.args.annotation.Param;
import net.sf.buildbox.args.api.ArgsCommand;
import net.sf.buildbox.changes.ChangesController;
import net.sf.buildbox.releasator.ng.ScmException;
import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.impl.DefaultVcsRegistry;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This started as a stupid rewrite of the releasator.sh script to java.
 * It's primary purpose is to provide seamless transfer to java-based code, as it is far much easier to develop and maintain.
 * All kinds of ugliness are allowed here for the initial phase; these will be continually resolved and later the whole thing will be  completely refactored to other packages.
 */
public abstract class JReleasator implements ArgsCommand {

    private static final String USER_HOME = System.getProperty("user.home");
    protected File tmpbase = new File(System.getProperty("java.io.tmpdir"), "releasator-" + System.getProperty("user.name"));
    protected File preloadRepository = new File(USER_HOME, ".m2/releasator-preload.zip");

    protected File tmp;
    protected File conf;
    protected ReleasatorProperties releasatorProperties;
    protected VcsRegistry vcsRegistry;
    protected ScmManager scmManager;
    private AntHookSupport antHookSupport;

    protected static <T extends ScmResult> T scm(T scmResult) {
        if (! scmResult.isSuccess()) {
            System.err.println("ERROR: " + scmResult.getCommandOutput());
            throw new ScmException(scmResult.getProviderMessage());
        }
        return scmResult;
    }

    protected void preloadRepository(File repo) throws ArchiverException {
        if (preloadRepository != null && preloadRepository.exists()) {
            System.err.println("INFO: preloading repository from " + preloadRepository);
            repo.mkdirs();
            final ZipUnArchiver ua = new ZipUnArchiver();
            ua.enableLogging(new ConsoleLogger(0, "UNZIP"));
            ua.setSourceFile(preloadRepository);
            ua.setDestDirectory(repo);
            ua.extract();
        }
    }

    protected void lock(String vcsLocation) throws IOException {
        String s = vcsLocation;
//        s = s.replace("/tags/", "");
//        s = s.replace("/trunk/", "");
        s = s.replace('/', '_');
        s = s.replace(':', '/');
        tmp = new File(tmpbase, s);
        final File lockFile = new File(tmp, "lock");
        if (lockFile.exists()) {
            throw new IOException("Release for this module is already running - lockfile is " + lockFile);
        }
        FileUtils.deleteDirectory(tmp);
        tmp.mkdirs();
        FileUtils.fileWrite(lockFile.getAbsolutePath(), vcsLocation.toString());
        lockFile.deleteOnExit();
    }

    protected void unlock() {
        final File lockFile = new File(tmp, "lock");
        lockFile.delete();
    }

    @Option(longName = "--conf", description = "directory with releasator configuration files")
    public void setConf(@Param("conf") File conf) throws IOException {
        this.conf = conf;
        ReleasatorProperties.validateConf(conf);
    }

    protected final void init() throws IOException {
        if (conf == null) {
            conf = new File(USER_HOME, ".m2/releasator");
        }
        if (releasatorProperties == null) {
            releasatorProperties = new ReleasatorProperties(conf);
        }
        vcsRegistry = new DefaultVcsRegistry();
        vcsRegistry.loadConf(conf);
        scmManager = vcsRegistry.getScmManager();

        if (antHookSupport == null) {
            antHookSupport = AntHookSupport.configure(conf, releasatorProperties);
        }
    }

    public String getReleasatorProperty(String propertyName, boolean required) throws IOException {
        return releasatorProperties.getReleasatorProperty(propertyName, required);
    }

    @Option(longName = "--tmpbase", description = "where to store locks and temporary files")
    public void setTmpBase(@Param("dir") File tmpbase) {
        this.tmpbase = tmpbase;
    }

    @Option(longName = "--preload-repository", description = "zip file with preloaded artifacts (performance tuning - use with care!)")
    public void setPreloadRepository(@Param("zipfile") File preloadRepository) {
        this.preloadRepository = preloadRepository;
    }

    protected Commandline prepareMavenCommandline(ChangesController chg, File wc, File localRepository, List<String> mavenArgs) throws IOException {
        final Commandline cl = new Commandline();
        cl.setWorkingDirectory(wc);
        // maven version
        final String mavenVersion = chg.getReleaseConfigProperty(ChangesController.RLSCFG_MAVEN_VERSION);
        if (mavenVersion == null) {
            cl.setExecutable("mvn");
            System.out.println("WARNING: using default (unspecified) maven, the one on PATH");
        } else {
            final File mavenHome = new File(getReleasatorProperty(ReleasatorProperties.CFG_MAVEN_VERSION_PREFIX + mavenVersion, true));
            final File mvn = new File(mavenHome, "bin/mvn");
            if (!mvn.isFile()) {
                throw new IllegalArgumentException(mavenHome + " : not a valid maven home directory");
            }
            cl.setExecutable(mvn.getAbsolutePath());
        }
        // settings.xml
        final File settingsXml = lookupSettingsXml(chg);

        // jdk version
        final String jdkVersion = chg.getReleaseConfigProperty(ChangesController.RLSCFG_JDK_VERSION);
        releasatorProperties.configureJava(cl, jdkVersion);

        // generic arguments
        cl.addArguments(new String[]{
                "--show-version",
                "--batch-mode",
                "--settings", settingsXml.getAbsolutePath(),
                "-Dmaven.repo.local=" + localRepository,
                "-X"
        });
        cl.addArguments(mavenArgs.toArray(new String[mavenArgs.size()]));
        for (Map.Entry<String, String> entry : chg.getReleaseConfigProperties().entrySet()) {
            if (entry.getKey().startsWith(ChangesController.RLSCFG_ENV_PREFIX)) {
                final String envName = entry.getKey().substring(ChangesController.RLSCFG_ENV_PREFIX.length());
                final String envValue = entry.getValue();
                System.out.println("adding env: " + envName + "=" + envValue);
                cl.addEnvironment(envName, envValue);
            }
        }
        return cl;
    }

    private File lookupSettingsXml(ChangesController chg) throws IOException {
        getReleasatorProperty("dummy", false);
        String settingsId = chg.getReleaseConfigProperty(ChangesController.RLSCFG_SETTINGS_ID);
        if (settingsId == null) {
            // try to autoguess settings.id by patterns specified in releasator.properties
            final String gavStr = chg.getGroupId() + ":" + chg.getArtifactId() + ":" + chg.getVersion(); //TODO: this should be releaseVersion!!!
            for (Object propNameObj : releasatorProperties.keys()) {
                final String propName = propNameObj.toString();
                if (propName.startsWith(ReleasatorProperties.CFG_SETTINGS_ID_BYGAV_PREFIX)) {
                    final String candidateId = propName.substring(ReleasatorProperties.CFG_SETTINGS_ID_BYGAV_PREFIX.length());
                    if (matchesPatterns(gavStr, getReleasatorProperty(propName, true))) {
                        System.out.println("INFO: autoconfiguration found settings.id=" + candidateId);
                        settingsId = candidateId;
                        break;
                    }
                }
            }
        }
        final String rsPrefix = settingsId == null ? "" : settingsId + "-";
        final File settingsXml = new File(conf, rsPrefix + "releasator-settings.xml");
        validateSettingsXml(settingsXml);
        return settingsXml;
    }

    private boolean matchesPatterns(String gavStr, String patternSpec) {
        final String[] patterns = patternSpec.split("\\|");
        for (String patternStr : patterns) {
            patternStr = patternStr.replace(".", "\\.");
            patternStr = patternStr.replace("*", ".*");
            if (Pattern.matches(patternStr, gavStr)) {
                return true;
            }
        }
        return false;
    }

    private void validateSettingsXml(File settingsXml) throws FileNotFoundException {
        if (! settingsXml.exists()) {
            throw new FileNotFoundException(settingsXml.getAbsolutePath());
        }
        // TODO: must be well-formed
        // TODO: make sure that it defines releasator-specific server
        // TODO: make sure that it defines releasator-specific properties
    }

    protected void runHook(String name) throws CommandLineException, IOException {
        if (antHookSupport != null) {
            antHookSupport.executeHook(tmp, name);
        }
    }

    public void copyOptionsFrom(JReleasator other) {
        setTmpBase(other.tmpbase);
        setPreloadRepository(other.preloadRepository);
        releasatorProperties = other.releasatorProperties;
        antHookSupport = other.antHookSupport;
        conf = other.conf;
        preloadRepository = other.preloadRepository;
    }
}
