package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.buildbox.args.annotation.Param;
import net.sf.buildbox.args.annotation.SubCommand;
import net.sf.buildbox.changes.ChangesController;
import net.sf.buildbox.changes.ChangesControllerImpl;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

@SubCommand(name = "upload", description = "uploads the release from a tag")
public class CmdUpload extends JReleasator {
    private final String projectUrl;

    public CmdUpload(@Param("scm-url") String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public void release_upload(ScmData scm) throws IOException, InterruptedException, CommandLineException, ArchiverException {
        final File wc = checkoutFiles(scm, "code", "upload-checkout-log.txt").getAbsoluteFile();
        final File changesXmlFile = new File(wc, "changes.xml");
        final ChangesController chg = new ChangesControllerImpl(changesXmlFile);
        final File localRepository = new File(tmp, "repository");
        preloadRepository(localRepository);
        {
            final List<String> mavenArgs = new ArrayList<String>();
            mavenArgs.add("-Dreleasator=" + Params.releasatorVersion);
            mavenArgs.add("-DperformRelease=true");
            mavenArgs.addAll(Arrays.asList(
                    "net.sf.buildbox.maven:buildbox-bbx-plugin:1.0.0:AttachChanges",
                    "-Denforcer.skip",
                    "clean",
                    "deploy"));
            final String moreMavenArgs = chg.getReleaseConfigProperty(ChangesController.RLSCFG_CMDLINE_MAVEN_ARGUMENTS);
            if (moreMavenArgs != null) {
                mavenArgs.addAll(Arrays.asList(moreMavenArgs.split(" ")));
            }
            final Commandline cl = prepareMavenCommandline(chg, wc, localRepository, mavenArgs);
            runHook(AntHookSupport.ON_BEFORE_DEPLOY_BUILD);
            MyUtils.loggedCmd(new File(tmp, "release-upload-log.txt"), cl);
            runHook(AntHookSupport.ON_AFTER_DEPLOY_BUILD);
        }
        System.out.println(String.format("SUCCESSFULY UPLOADED - module %s:%s:%s has been released",
                chg.getGroupId(),
                chg.getArtifactId(),
                chg.getVersion()));
    }

    public Integer call() throws Exception {
        final ScmData scm = ScmData.valueOf(projectUrl);
        init();
        try {
            lock(scm.getVcsId() + ":" + scm.getVcsPath());
            release_upload(scm);
            return 0;
        } finally {
            unlock();
        }
    }
}
