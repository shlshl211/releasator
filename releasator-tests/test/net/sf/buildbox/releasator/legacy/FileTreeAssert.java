package net.sf.buildbox.releasator.legacy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.codehaus.plexus.util.FileUtils;
import org.custommonkey.xmlunit.*;
import org.junit.Assert;
import org.xml.sax.SAXException;

public class FileTreeAssert {
    /**
     * Compares file trees.
     * @param baseUri  which part of the "desired" repository to check (root)
     * @param expectedFileCount  how many "desired" files we expect to be comparing
     */
    public static void assertResultFilesMatchDesired(DifferenceListener differenceListener,
                                              File controlBase, File testBase,
                                              String baseUri,
                                              int expectedFileCount) throws IOException, SAXException {
        final File controlRoot = new File(controlBase, baseUri);
        final File testRoot = new File(testBase, baseUri);
        System.out.println("controlRoot = " + controlRoot);
        System.out.println("testRoot = " + testRoot);
        @SuppressWarnings("unchecked")
        final List<String> uris = FileUtils.getFileNames(controlRoot, null, null, false);
        Assert.assertEquals("number of checked files", expectedFileCount, uris.size());
        System.out.println(String.format("Going to check %d files under %s", expectedFileCount, baseUri));
        int errors = 0;
        for (String uri : uris) {
            final File controlFile = new File(controlRoot, uri);
            final File testFile = new File(testRoot, uri);
            errors += countXmlFilesMismatches(differenceListener, controlFile, testFile);
        }
        Assert.assertEquals("Unrecoverable XML differences found", 0, errors);
    }

    public static int countXmlFilesMismatches(DifferenceListener differenceListener, File controlFile, File testFile) throws SAXException, IOException {
        System.out.println("Checking XML differences for" + testFile);
        System.out.println("C: " + controlFile);
        System.out.println("T: " + testFile);
        final Reader controlInput = new FileReader(controlFile);
        final Reader testInput = new FileReader(testFile);
        final Diff diff = new Diff(controlInput, testInput);
        diff.overrideDifferenceListener(differenceListener);
        final DetailedDiff ddiff = new DetailedDiff(diff);
        @SuppressWarnings("unchecked")
        final List<Difference> differences = ddiff.getAllDifferences();
        System.out.println("differences.size() = " + differences.size());
        int errors = 0;
        for (Difference difference : differences) {
            final String loc = difference.getControlNodeDetail().getXpathLocation();
            System.out.println("==== " + loc + " ====");

            System.out.println(String.format("%s (%02d) %s\n     Expected: '%s'\n     Found:    '%s'",
                    difference.isRecoverable() ? "INFO": "ERROR",
                    difference.getId(),
                    difference.getDescription(),
                    difference.getControlNodeDetail().getValue(),
                    difference.getTestNodeDetail().getValue()
                    ));
            if (!difference.isRecoverable()) {
                errors++;
            }
        }
        return errors;
    }

}
