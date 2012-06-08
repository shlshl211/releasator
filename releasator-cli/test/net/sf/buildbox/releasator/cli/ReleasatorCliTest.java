package net.sf.buildbox.releasator.cli;

import net.sf.buildbox.releasator.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

public class ReleasatorCliTest {
    @Ignore("manual")
    @Test
    public void testPrepare() throws Exception {
        final int exitCode = Main.run("--author", "pkozelka@gmail.com",
                "prepare", "http://example.com/svn/repo/trunk/myproject", "1.0.1-alpha-12", "mycodename");
        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testGlobalHelp() throws Exception {
        final int exitCode = Main.run("--help");
        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testHelpPrepare() throws Exception {
        final int exitCode = Main.run("--help", "prepare");
        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testHelpUpload() throws Exception {
        final int exitCode = Main.run("--help", "upload");
        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testHelpFull() throws Exception {
        final int exitCode = Main.run("--help", "full");
        Assert.assertEquals(0, exitCode);
    }
}
