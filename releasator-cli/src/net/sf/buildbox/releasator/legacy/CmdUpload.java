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
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepository;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

@SubCommand(name = "upload", description = "uploads the release from a tag")
public class CmdUpload extends JReleasator {
    private final String projectUrl;
    private final String tag;

    public CmdUpload(@Param("scm-url") String projectUrl, @Param("tag") String tag) {
        this.projectUrl = projectUrl;
        this.tag = tag;
    }

    public void release_upload(VcsRepositoryMatch match) throws IOException, InterruptedException, CommandLineException, ArchiverException, ScmException {
        final File wc = new File(tmp, "code-tag");
        wc.getParentFile().mkdirs();
        final CheckOutScmResult checkOutScmResult = scm(scmManager.checkOut(match.getScmRepository(), new ScmFileSet(wc), new ScmTag(tag)));
        System.out.println("checkOutScmResult.getCheckedOutFiles().size() = " + checkOutScmResult.getCheckedOutFiles().size());

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
        init(true);
        final VcsRepositoryMatch match = vcsRegistry.findByScmUrl(projectUrl);
        if (match == null) {
            final List<VcsFactoryConfig> vcsFactoryConfigs = vcsRegistry.list();
            System.err.println(String.format("Available VCS configurations (%d):", vcsFactoryConfigs.size()));
            for (VcsFactoryConfig vcsFactoryConfig : vcsFactoryConfigs) {
                System.err.println("* " + vcsFactoryConfig.toString());
            }
            throw new RuntimeException("No matching VCS found for " + projectUrl);
        }

        try {
            release_upload(match);
            return 0;
        } finally {
        }
    }
}
