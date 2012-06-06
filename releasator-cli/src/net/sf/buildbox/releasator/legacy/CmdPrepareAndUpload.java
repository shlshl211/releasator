package net.sf.buildbox.releasator.legacy;

import net.sf.buildbox.args.annotation.Param;
import net.sf.buildbox.args.annotation.SubCommand;

@SubCommand(name = "full", description = "= prepare + upload")
public class CmdPrepareAndUpload extends AbstractPrepareCommand {
    public CmdPrepareAndUpload(
            @Param("project-url") String projectUrl,
            @Param("version") String releaseVersion,
            @Param("codename") String... codename) {
        super(projectUrl, releaseVersion, codename);
    }

    public Integer call() throws Exception {
        final CmdPrepare cmdPrepare = new CmdPrepare(projectUrl, releaseVersion, codename == null ? "" : codename);
        //TODO: change codename to option
        cmdPrepare.copyOptionsFrom(this);
        //
        final Integer exitCode = cmdPrepare.call();
        if (exitCode == 0) {
            final CmdUpload cmdUpload = new CmdUpload(projectUrl, cmdPrepare.getReleaseTag());
            cmdUpload.copyOptionsFrom(this);
            return cmdUpload.call();
        }
        return exitCode;
    }

}
