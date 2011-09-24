package net.sf.buildbox.releasator.legacy;

import net.sf.buildbox.util.BbxStringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AntHookSupport {
    public static final String ON_VCS_LOCK = "on-vcs-lock";
    public static final String ON_VCS_UNLOCK = "on-vcs-unlock";
    public static final String ON_BEFORE_DRY_BUILD = "on-before-dry-build";
    public static final String ON_AFTER_DRY_BUILD = "on-after-dry-build";
    public static final String ON_BEFORE_FIRST_COMMIT = "on-before-first-commit";
    public static final String ON_BEFORE_MRP_PREPARE = "on-before-mrp-prepare";
    public static final String ON_AFTER_MRP_PREPARE = "on-after-mrp-prepare";
    public static final String ON_AFTER_LAST_COMMIT = "on-after-last-commit";
    public static final String ON_BEFORE_DEPLOY_BUILD = "on-before-deploy-build";
    public static final String ON_AFTER_DEPLOY_BUILD = "on-after-deploy-build";

    private static final String HOOK_PREFIX = "on-";
    private static final String ANT_NAMESPACE = "antlib:org.apache.tools.ant";

    private final File antExecutable;
    private final File antBuildFile;
    private final ReleasatorProperties releasatorProperties;
    private Arg antArgs = new Commandline.Argument();

    private Map<String,String> availableTargets = new HashMap<String, String>();

    private AntHookSupport(File antExecutable, File antBuildFile, ReleasatorProperties releasatorProperties) throws IOException {
        this.antExecutable = antExecutable;
        this.antBuildFile = antBuildFile;
        this.releasatorProperties = releasatorProperties;
        final String antArgsStr = releasatorProperties.getReleasatorProperty(ReleasatorProperties.CFG_ANT_ARGS, false);
        if (antArgsStr != null) {
            antArgs.setLine(BbxStringUtils.expandSysProps(antArgsStr));
        }
        try {
            readTargets();
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void readTargets() throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
        DBF.setNamespaceAware(true);
        final DocumentBuilder DB = DBF.newDocumentBuilder();
        final Document doc = DB.parse(antBuildFile);
        final NodeList targetElements = doc.getElementsByTagNameNS(ANT_NAMESPACE, "target");
        final int targetCount = targetElements.getLength();
        if (targetCount == 0) {
            System.err.println("WARNING: No targets found in " +antBuildFile+ " - are you using correct namespace " + ANT_NAMESPACE + " ?");
            return;
        }
        for (int i=0; i < targetCount; i++) {
            final Element targetElem = (Element) targetElements.item(i);
            final String targetName = targetElem.getAttribute("name");
            if (targetName == null) continue;
            if (!targetName.startsWith(HOOK_PREFIX)) continue;
            final String depends = targetElem.getAttribute("depends");
            if (depends == null && !targetElem.hasChildNodes()) continue;
            availableTargets.put(targetName, targetName); // the second argument will be used to store list of expected properties
        }
        System.out.println("INFO: File " + antBuildFile + " defines " + availableTargets.size() + " active hooks");
    }

    public static AntHookSupport configure(File conf, ReleasatorProperties releasatorProperties) throws IOException {
        // if ant is configured and does not exist, fail
        // if ant is configured and no buildfile, create it from a skeleton
        // if buildfile exists and ant is not configured, warn
        // read list of targets
        final File antBuildFile = new File(conf, "releasator.build.xml");
        final String antExecutableStr = releasatorProperties.getReleasatorProperty(ReleasatorProperties.CFG_ANT_EXECUTABLE, false);
        if (antExecutableStr == null) {
            if (antBuildFile.exists()) {
                System.err.println("WARNING: Ant is not configured in release.properties - no property named '"
                        + ReleasatorProperties.CFG_ANT_EXECUTABLE + "' exists");
            }
            return null;
        }
        final File antExecutable = new File(antExecutableStr);
        if (! antExecutable.isFile()) {
            throw new FileNotFoundException(antExecutableStr + " - check property '" + ReleasatorProperties.CFG_ANT_EXECUTABLE + "' in release.properties");
        }
        if (! antBuildFile.isFile()) {
            System.err.println("INFO: Creating ant file from skeleton: " + antBuildFile);
            FileUtils.copyURLToFile(AntHookSupport.class.getResource("/etc/default.releasator.build.xml"), antBuildFile);
        }
        return new AntHookSupport(antExecutable, antBuildFile, releasatorProperties);
    }

    public void executeHook(File workdir, String target) throws IOException, CommandLineException {
        if (! availableTargets.containsKey(target)) return;
        System.out.println();
        System.out.println("/-------- hook target " + target + " executes: --------\\");
        final Commandline antCommandline = new Commandline();
        final String jdkVersionForAnt = releasatorProperties.getReleasatorProperty(ReleasatorProperties.CFG_ANT_JDK_VERSION, false);
        releasatorProperties.configureJava(antCommandline, jdkVersionForAnt);
        antCommandline.setExecutable(antExecutable.getAbsolutePath());
        antCommandline.addArg(antArgs);
        antCommandline.addArguments(new String[]{"-f", antBuildFile.getAbsolutePath(),
                "-Dworkdir=" + workdir,
                target});
        antCommandline.setWorkingDirectory(antBuildFile.getParentFile());
        MyUtils.doCmd(antCommandline);
        System.out.println("\\-------- hook target " + target + " finished. --------/");
        System.out.println();
    }
}
