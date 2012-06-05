package net.sf.buildbox.releasator.legacy;

import org.junit.Test;

public class JReleasatorTest {

    @Test
    public void showCurrentTimeAsUTC() throws Exception {
        System.out.println("JReleasator.formatCurrentTime() = " + MyUtils.formatCurrentTime());
    }

    @Test
    public void mavenVersionTest() throws Exception {
        String v = MyUtils.getMavenVersion();
        System.out.println("v = " + v);
    }
}
