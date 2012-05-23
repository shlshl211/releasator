package net.sf.buildbox.releasator.ng.model;

import java.io.File;
import java.util.Set;

/**
 * @author Petr Kozelka
 */
public class VcsFactoryConfig {
    File config;
    String vcsType;
    String webSoftware;
    String vcsIdMask;
    String webMask;
    String scmUrlMask;
    Set<String> otherScmUrlMasks;
    // publishing with maven:
    File releasatorSettingsXml;
//    String uploadUrl;
//    String publicDownloadUrlBase;
}
