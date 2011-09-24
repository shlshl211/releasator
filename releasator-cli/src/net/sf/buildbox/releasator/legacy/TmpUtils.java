package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import net.sf.buildbox.changes.ChangesController;

public class TmpUtils {
    /**
     * @deprecated try to avoid using this variant - use {@link net.sf.buildbox.changes.ChangesControllerUtil#addMavenModule(net.sf.buildbox.changes.ChangesController , java.io.File, String, String, String)} instead.
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
        final File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                final String fn = file.getName();
                if (fn.endsWith(".md5")) return false;
                if (fn.endsWith(".sha1")) return false;
                if (fn.endsWith(".asc")) return false;
                //
                return true;
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
        });
        for (File file : files) {
            final String fn = file.getName();
            if (fn.startsWith(prefixDash)) {
                final String suffix = fn.substring(prefixDot.length());
                int n = suffix.indexOf('.');
                if (n < 0) continue;
                // TODO: if there is multiple dots, they become part of type - is that correct ?
                final String classifier = suffix.substring(0, n);
                final String type = suffix.substring(n + 1);
                chg.addFile(groupId, artifactId, releaseVersion, classifier, type);

            } else if (fn.startsWith(prefixDot)) {
                final String type = fn.substring(prefixDot.length());
                chg.addFile(groupId, artifactId, releaseVersion, null, type);
            }
        }
    }
}
