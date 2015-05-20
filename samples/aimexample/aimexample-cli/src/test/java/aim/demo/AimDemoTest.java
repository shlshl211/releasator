package aim.demo;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Petr Kozelka
 */
public class AimDemoTest {
    @Test
    public void testMain() throws Exception {
        AimDemo.main();
    }

    @Ignore("this represents a fix")
    @Test
    public void breakingTest() {
        Assert.fail("intentional failure");
    }
}
