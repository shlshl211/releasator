package testutil;

import java.io.File;

public final class Tst {
    public static File getTestDataDir() {
        /// ATTENTION: this code references itself as a classpath resource !
        final File f = new File(Tst.class.getResource("/testutil/Tst.class").getFile());
        ///
        final File resources = f.getParentFile().getParentFile();
        final File testDataDir = resources.getName().equals("test-classes") ? resources.getParentFile() : resources;
        final File rv = new File(testDataDir, "test-data");
        rv.mkdirs();
        return rv;
    }
}
