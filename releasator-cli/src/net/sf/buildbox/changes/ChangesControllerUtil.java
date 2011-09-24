package net.sf.buildbox.changes;

import java.io.File;
import java.io.FileFilter;

public class ChangesControllerUtil {
    public static void addMavenModule(final ChangesController chg, File localRepository, final String groupId, final String artifactId, final String version) {
        addMavenModuleInternal(chg, localRepository, groupId, artifactId, version, version);
    }

    /**
     * @deprecated try to avoid using this variant - use {@link #addMavenModule(ChangesController, java.io.File, String, String, String)} instead.
     * This one exists in order to workarround stupidity of maven's release:prepare in dry mode where the snapshot poms are used, not the versioned ones.
     */
    @Deprecated
    public static void addMavenModule(final ChangesController chg, File localRepository, final String groupId, final String artifactId, final String version, final String releaseVersion) {
        addMavenModuleInternal(chg, localRepository, groupId, artifactId, version, releaseVersion);
    }

    private static void addMavenModuleInternal(final ChangesController chg, File localRepository, final String groupId, final String artifactId, final String version, final String releaseVersion) {
        final File dir = new File(localRepository, String.format("%s/%s/%s",
                groupId.replace('.', '/'),
                artifactId,
                version));
        final String prefixDash = artifactId + "-" + version + "-";
        final String prefixDot = artifactId + "-" + version + ".";
        dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                final String fn = file.getName();
                if (fn.endsWith(".md5")) return false;
                if (fn.endsWith(".sha1")) return false;
                if (fn.endsWith(".asc")) return false;
                if (fn.startsWith(prefixDash)) {
                    final String suffix = fn.substring(prefixDot.length());
                    int n = suffix.indexOf('.');
                    if (n < 0) return false;
                    // TODO: if there is multiple dots, they become part of type - is that correct ?
                    final String classifier = suffix.substring(0, n);
                    final String type = suffix.substring(n + 1);
                    chg.addFile(groupId, artifactId, releaseVersion, classifier, type);

                } else if (fn.startsWith(prefixDot)) {
                    final String type = fn.substring(prefixDot.length());
                    chg.addFile(groupId, artifactId, releaseVersion, null, type);
                }
                return false;
            }
        });
    }
}
