package net.sf.buildbox.releasator.legacy;

import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.impl.DefaultVcsRegistry;
import org.apache.maven.scm.manager.ScmManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Petr Kozelka
 */
public class Globals {
    private static final String USER_HOME = System.getProperty("user.home");

    public static final Globals INSTANCE = new Globals();

    private File preloadRepository = new File(USER_HOME, ".m2/releasator-preload.zip");
    private File tmp;
    private File conf;
    private ReleasatorProperties releasatorProperties;
    private VcsRegistry vcsRegistry;
    private AntHookSupport antHookSupport;


    public void preloadRepository(File repo) throws ArchiverException {
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

    public File getPreloadRepository() {
        return preloadRepository;
    }

    public void setPreloadRepository(File preloadRepository) {
        this.preloadRepository = preloadRepository;
    }

    public File getTmp() throws IOException {
        if (tmp == null) {
            setTmp(FileUtils.createTempFile("releasator.", "." + System.getProperty("user.name"), null));
        }
        return tmp;
    }

    public void setTmp(File tmp) throws IOException {
        if (! tmp.exists()) {
            tmp.mkdirs();
        }
        if (! tmp.isDirectory()) {
            throw new IOException("Failed to create temporary directory: " + tmp);
        }
        final String[] existingFiles =  tmp.list();
        if (existingFiles.length > 0) {
            throw new IOException(tmp + ": temporary dir is not empty - " + Arrays.asList(existingFiles));
        }
        this.tmp = tmp;
    }

    public File getConf() {
        if (conf == null) {
            conf = new File(USER_HOME, ".m2/releasator");
        }
        return conf;
    }

    public void setConf(File conf) throws FileNotFoundException {
        this.conf = conf;
        ReleasatorProperties.validateConf(conf);
    }

    public ReleasatorProperties getReleasatorProperties() throws IOException {
        if (releasatorProperties == null) {
            releasatorProperties = new ReleasatorProperties(getConf());
        }
        return releasatorProperties;
    }

    public String getReleasatorProperty(String propertyName, boolean required) throws IOException {
        return getReleasatorProperties().getReleasatorProperty(propertyName, required);
    }


    public VcsRegistry getVcsRegistry() throws IOException {
        if (vcsRegistry == null) {
            vcsRegistry = new DefaultVcsRegistry();
            vcsRegistry.loadConf(getConf());
        }
        return vcsRegistry;
    }

    public void setVcsRegistry(VcsRegistry vcsRegistry) {
        this.vcsRegistry = vcsRegistry;
    }

    public ScmManager getScmManager() {
        return vcsRegistry.getScmManager();
    }

    public AntHookSupport getAntHookSupport() throws IOException {
        if (antHookSupport == null) {
            antHookSupport = AntHookSupport.configure(getConf(), getReleasatorProperties());
        }
        return antHookSupport;
    }

}
