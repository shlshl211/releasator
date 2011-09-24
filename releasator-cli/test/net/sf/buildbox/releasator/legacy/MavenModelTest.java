package net.sf.buildbox.releasator.legacy;

import org.junit.Test;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import java.io.File;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import testutil.Tst;

public class MavenModelTest {

    @Test
    public void pomApiTest() throws Exception {
        final File projectBuildDirectory = Tst.getTestDataDir().getParentFile();
        final File pomFile = new File(projectBuildDirectory.getParentFile(), "pom.xml");
        final MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        final Model model = xpp3Reader.read(new FileReader(pomFile));
        Scm scm = new Scm();
        scm.setConnection("conn");
        scm.setDeveloperConnection("dconn");
        scm.setTag("tg");
        scm.setUrl("url");
        model.setScm(scm);
        //
        final Writer os = new FileWriter(new File(projectBuildDirectory, "newpom.xml"));
        new MavenXpp3Writer().write(os, model);
        os.close();
    }

}
