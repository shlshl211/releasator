package net.sf.buildbox.releasator.ng;

import com.thoughtworks.xstream.XStream;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Petr Kozelka
 */
public class VcsRegistryTest {

    @Test
    public void testParse() throws Exception {
        final XStream xstream = vcsXstream();

        final VcsFactoryConfig config = (VcsFactoryConfig) xstream.fromXML(getClass().getResourceAsStream("git.bitbucket.xml"));
        System.out.println("config = " + config);
    }

    @Test
    public void testShow() throws Exception {
        final VcsFactoryConfig config = new VcsFactoryConfig();
        config.setVcsIdMask("sf.[REPOURI]");
        config.setVcsType("svn");
        config.setScmUrlMask("scm:svn:https://[REPOURI].svn.sourceforge.net/svnroot/[REPOURI][PATH]");
        config.setOtherScmUrlMasks(Arrays.asList("scm:svn:http://[REPOURI].svn.sourceforge.net/svnroot/[REPOURI][PATH]"));

        XStream xstream = vcsXstream();
        System.out.println(xstream.toXML(config));
    }

    private static XStream vcsXstream() {
        final XStream xstream = new XStream();
        xstream.alias("VcsFactoryConfig", VcsFactoryConfig.class);
        xstream.aliasField("type", VcsFactoryConfig.class, "vcsType");
        xstream.aliasField("idMask", VcsFactoryConfig.class, "vcsIdMask");
        xstream.addImplicitCollection(VcsFactoryConfig.class, "otherScmUrlMasks", "otherScmUrlMask", String.class);
        xstream.omitField(VcsFactoryConfig.class, "file");
        return xstream;
    }
}
