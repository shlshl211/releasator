package net.sf.buildbox.releasator;

import net.sf.buildbox.args.BasicArgsParser;
import net.sf.buildbox.args.DefaultHelpCommand;
import net.sf.buildbox.args.annotation.AnnottationAwareSetup;
import net.sf.buildbox.releasator.legacy.CmdPrepare;
import net.sf.buildbox.releasator.legacy.CmdPrepareAndUpload;
import net.sf.buildbox.releasator.legacy.CmdUpload;
import net.sf.buildbox.releasator.legacy.Params;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;

//@Singleton
public class Main {

    /**
     * Highest-possible level of invocation usable both from {@link #main} and from tests.
     *
     * @param args commandline arguments
     * @return true if successful
     * @throws Exception -
     */
    public static int run(String... args) throws Exception {
        System.err.println("Releasator " + Params.releasatorVersion + " (C) 2006-2012 Petr Kozelka <pkozelka@gmail.com>");
        System.getProperties().store(System.out, "JVM SYSTEM PROPERTIES FOR BUILDBOX JDK DISCOVERY " + Arrays.asList(args));
        File homedir = new File(System.getProperty("java.class.path")).getParentFile().getAbsoluteFile();
        System.out.println("homedir = " + homedir); //works in production, but not in tests
//        System.err.println("http://releasator.sourceforge.net");
        final AnnottationAwareSetup setup = argsSetup();
        return BasicArgsParser.process(setup, args);
    }

/*
    public void runInWeld(@Observes ContainerInitialized event, @Parameters String[] args) throws Exception {
        System.err.println("Weld-based Releasator " + Params.releasatorVersion + " (C) 2006-2011 Petr Kozelka <pkozelka@gmail.com>");
        final AnnottationAwareSetup setup = argsSetup();
        BasicArgsParser.process(setup, args);
    }

*/
    static AnnottationAwareSetup argsSetup() throws ParseException {
        final AnnottationAwareSetup setup = new AnnottationAwareSetup("releasator");
        setup.addSubCommand(DefaultHelpCommand.class);
        setup.addSubCommand(CmdPrepare.class);
        setup.addSubCommand(CmdPrepareAndUpload.class);
        setup.addSubCommand(CmdUpload.class);
        return setup;
    }

    public static void main(String... args) throws Exception {
        final int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
