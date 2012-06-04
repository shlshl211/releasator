package net.sf.buildbox.releasator.legacy;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;
import java.util.regex.Pattern;

public class ChangesAndPomDifferenceListener implements DifferenceListener {
    private final Pattern BUILDTOOL_VERSION_PATTERN = Pattern.compile("^" + Pattern.quote("/changes[1]/release[") + "\\d+" + Pattern.quote("]/buildtool[") + "\\d+" + Pattern.quote("]/@version"));
    private final Pattern OS_PATTERN = Pattern.compile("^" + Pattern.quote("/changes[1]/release[") + "\\d+" + Pattern.quote("]/os[") + "\\d+" + Pattern.quote("]/@") + "\\w+");
    private final String testDataPath;

    public ChangesAndPomDifferenceListener(String testDataPath) {
        this.testDataPath = testDataPath;
        System.out.println("testDataPath = " + testDataPath);
    }

    public int differenceFound(Difference difference) {
        final String cxPath = difference.getControlNodeDetail().getXpathLocation();
        if (difference.getId() == DifferenceConstants.ATTR_VALUE_ID) {
            // tolerate timestamps in changes.xml
            final String localName = difference.getControlNodeDetail().getNode().getNodeName();
//            if ("revision".equals(localName)) {
//                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
//            }
            if ("timestamp".equals(localName)) {
                //TODO: we could at least check time format
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            if (BUILDTOOL_VERSION_PATTERN.matcher(cxPath).matches()) {
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR; 
            }
            if (OS_PATTERN.matcher(cxPath).matches()) {
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
        } else if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
            final String controlValue = difference.getControlNodeDetail().getValue();
            final String testValue = difference.getTestNodeDetail().getValue();
            if (controlValue.trim().equals(testValue.trim())) {
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            // tolerate location of adhoc scm which depends on where the test is running
            if (cxPath.startsWith("/project[1]/scm[1]/")) {
                final boolean matching = controlValue.equals(testValue.replace(testDataPath, "@TESTDATA@"));
                return matching ? RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR : RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
            }
        } else if (difference.getId() == DifferenceConstants.PROCESSING_INSTRUCTION_DATA_ID) {
            return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
        } else if (difference.getId() == DifferenceConstants.PROCESSING_INSTRUCTION_TARGET_ID) {
            return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
        }
        return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
    }

    public void skippedComparison(Node node1, Node node2) {
        System.out.println("--- skipped comparison: node1=" + node1 + ", node2=" + node2 + " ---");
    }
}
