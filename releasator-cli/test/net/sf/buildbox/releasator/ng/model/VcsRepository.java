package net.sf.buildbox.releasator.ng.model;

import java.io.File;

/**
 * @author Petr Kozelka
 */
public class VcsRepository {
    String vcsType;
    String vcsId;
    String connection;
    String webSoftware;
    String webUrl;

    File releasatorSettingsXml;
}
