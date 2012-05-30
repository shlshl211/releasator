package net.sf.buildbox.releasator.legacy;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.thoughtworks.xstream.XStream;
import net.sf.buildbox.releasator.Main;
import net.sf.buildbox.releasator.ng.impl.DefaultVcsRegistry;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class Helper {
    static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public static String svnRepoPrepareAndCheckout(File repoDir) throws IOException, InterruptedException, CommandLineException {
        final String repoPath = repoDir.getAbsolutePath().replace('\\', '/');
        repoDir.getParentFile().mkdirs();
        doCmd(null, "svnadmin", "create", repoPath);
        final boolean iswindows = System.getProperty("os.name").toLowerCase().startsWith("win");
        return (iswindows ? "file:///" : "file://") + repoPath;
    }


    public static void doCmd(final File basedir, String command, String... args) throws IOException, InterruptedException, CommandLineException {
        final Commandline cl = new Commandline();
        cl.setWorkingDirectory(basedir);
        cl.setExecutable(command);
        cl.addArguments(args);
        System.out.println("# Executing shell: " + cl.getShell().getShellCommand());
        if (cl.getWorkingDirectory() != null) {
            System.out.println("# cd " + cl.getWorkingDirectory());
        }
        System.out.println("# " + CommandLineUtils.toString(cl.getCommandline()));
        final int exitCode = CommandLineUtils.executeCommandLine(cl, MyUtils.STDERR_CONSUMER, MyUtils.STDERR_CONSUMER);
        if (exitCode != 0) {
            throw new IOException("exitCode = " + exitCode);
        }
    }

    static void unzipUrl(URL url, File targetDir) throws IOException {
        final ZipInputStream zis = new ZipInputStream(url.openStream());
        System.out.println(url + " --> " + targetDir);
        try {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                final String name = ze.getName();
                final File targetFile = new File(targetDir, name);
                targetFile.getParentFile().mkdirs();
                if (!ze.isDirectory()) {
                    System.out.println("  unpacking " + name);
                    copyStreamToFileNC(zis, targetFile);
                }
                //
                ze = zis.getNextEntry();
            }
        } finally {
            zis.close();
        }

    }

    static void copyStreamToFileNC(InputStream inputStream, File outputFile) throws IOException {
        final OutputStream os = new FileOutputStream(outputFile);
        try {
            copyStream(inputStream, os);
        } finally {
            os.close();
        }
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code>.
     *
     * @param is stream to copy from
     * @param os stream to copy to
     * @return the number of bytes copied
     * @throws java.io.IOException if an I/O error occurs (may result in partially done work)
     */
    public static long copyStream(InputStream is, OutputStream os) throws IOException {
        final byte[] copyBuffer = new byte[8192]; //buffer used for copying
        long bytesCopied = 0;
        int cnt = is.read(copyBuffer);
        while (cnt != -1) {
            os.write(copyBuffer, 0, cnt);
            bytesCopied += cnt;
            cnt = is.read(copyBuffer);
        }
        return bytesCopied;
    }

    static Collection<URL> listResources(String resName, ClassLoader classLoader) throws IOException {
        final Collection<URL> result = new ArrayList<URL>();
        final Enumeration<URL> urls = classLoader.getResources(resName);
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            final String protocol = url.getProtocol();
            if ("jar".equals(protocol)) {
                final String s = url.getFile();
                final String path = s.substring(5, s.length() - 2 - resName.length());
                final File jar = new File(path);
                final ZipInputStream zis = new ZipInputStream(new FileInputStream(jar));
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    final String entryName = ze.getName();
                    if (entryName.endsWith(".zip")) {
                        final URL testsuiteUrl = new URL("jar:file:" + path + "!/" + entryName);
                        result.add(testsuiteUrl);
                    }
                    ze = zis.getNextEntry();
                }
                zis.close();
            } else if ("file".equals(protocol)) {
                // flat directory
                final String path = url.getPath();
                @SuppressWarnings("unchecked")
                final List<File> lst = FileUtils.getFiles(new File(path.substring(0, path.length() - resName.length())), "**/*.zip", null);
                for (File file : lst) {
                    result.add(file.toURL());
                }
                //TODO: traverse and pass all zip files [containing testsuite.properties]
            } else {
                System.out.println("WARNING - ignored pathelement: " + url);
            }
        }
        return result;
    }

    static int releasator(String... args) throws Exception {
        System.err.println("--- executing ---");
        System.err.print("releasator");
        for (String arg : args) {
            System.err.print(" " + arg);
        }
        System.err.println();
        final int exitCode = Main.run(args);
        System.err.println("--- terminated with exitCode=" + exitCode + " ---");
        return exitCode;
    }

    /**
     * Generates settings.xml equipped for releasator. That is:
     * - it copies mirrors and servers from the currently active settings.xml
     * - configures properties for releasator output repositories
     *
     * @param newSettingsFile the settings file to be generated
     * @throws TransformerException -
     * @throws IOException          -
     */
    static void generateSettings(File newSettingsFile) throws TransformerException, IOException {
        final File currentSettings = new File(System.getProperty("user.home"), ".m2/settings.xml"); // isn't there a better way ?
        generateSettings(newSettingsFile, currentSettings);
        // generate properties
        final File testData = newSettingsFile.getParentFile();
        final File confDir = new File(testData, "testconf");
        confDir.mkdirs();
        final File newPropertiesFile = new File(confDir, "releasator.properties");
        final Properties props = new Properties();
        props.setProperty("settings.id.byGav-test", "*:*:*");
        final OutputStream os = new FileOutputStream(newPropertiesFile);
        try {
            props.store(os, "test configuration for releasator");
        } finally {
            os.close();
        }

        // generate descriptor
        final File svnrepo = new File(testData, "svnrepo");
        final VcsFactoryConfig localtest = new VcsFactoryConfig();
        localtest.setVcsType("svn");
        localtest.setVcsIdMask("test.{REPOURI}");
        localtest.setScmUrlMasks(Arrays.asList("scm:svn:file://" + svnrepo.getAbsolutePath() + "/{PATH}"));
//        localtest.setScmweb(TODO);// TODO
//        localtest.setReleasatorSettingsXml(); //TODO
        final XStream xstream = DefaultVcsRegistry.vcsXstream();
        final File localtestFile = new File(testData, "local/local.svn.xml");
        localtestFile.getParentFile().mkdirs();
        FileUtils.fileWrite(localtestFile.getAbsolutePath(), xstream.toXML(localtest));
    }

    /**
     * Generates settings.xml equipped for releasator. That is:
     * - it copies mirrors and servers from the currently active settings.xml
     * - configures properties for releasator output repositories
     *
     * @param newSettingsFile the settings file to be generated
     * @param currentSettings currently active settings
     * @throws TransformerException -
     * @throws IOException          -
     */
    private static void generateSettings(File newSettingsFile, File currentSettings) throws TransformerException, IOException {
        final URL url = Helper.class.getResource("settings-AddMirrors.xsl");
        final InputStream stream = url.openStream();
        try {
            final File settingsDirectory = newSettingsFile.getParentFile();
            settingsDirectory.mkdirs();
            final Transformer t = TRANSFORMER_FACTORY.newTransformer(new StreamSource(stream));
            t.setParameter("current.settings.xml", currentSettings.getAbsolutePath());
            t.setParameter("tmp.repo.base", settingsDirectory);
            System.out.println(currentSettings + " *--> " + newSettingsFile);
            t.transform(new StreamSource(Helper.class.getResourceAsStream("minimal-settings.xml")), new StreamResult(newSettingsFile));
        } finally {
            stream.close();
        }
    }
}
