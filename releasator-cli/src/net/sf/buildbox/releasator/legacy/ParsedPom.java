package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

class ParsedPom {
    final File file;
    String groupId;
    String artifactId;
    String version;
    final Set<String> modules = new LinkedHashSet<String>();

    public ParsedPom(File file) {
        this.file = file;
    }

    public static ParsedPom parse(File pomFile) throws IOException {
        if (! pomFile.exists()) {
            throw new FileNotFoundException(pomFile.getAbsolutePath());
        }
        try {
            final ParsedPom pom = new ParsedPom(pomFile);
            final Model mavenModel = new MavenXpp3Reader().read(new FileReader(pomFile));
            pom.groupId = mavenModel.getGroupId();
            pom.artifactId = mavenModel.getArtifactId();
            pom.version = mavenModel.getVersion();
            final Parent parent = mavenModel.getParent();
            if (parent != null) {
                if (pom.groupId == null) {
                    pom.groupId = parent.getGroupId();
                }
                if (pom.artifactId == null) {
                    pom.artifactId = parent.getArtifactId();
                }
                if (pom.version == null) {
                    pom.version = parent.getVersion();
                }
            }
            @SuppressWarnings("unchecked")
            final List<String> mmodules = mavenModel.getModules();
            if (mmodules != null) {
                pom.modules.addAll(mmodules);
            }
            return pom;
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    public String gav() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public String toString() {
        return gav();
    }
}
