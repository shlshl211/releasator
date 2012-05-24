package net.sf.buildbox.releasator.ng.model;

import java.io.File;
import java.util.Set;

/**
 * @author Petr Kozelka
 */
public class VcsFactoryConfig {
    private File config;
    private String vcsType;
    private String webSoftware;
    private String vcsIdMask;
    private String webMask;
    private String scmUrlMask;
    private Set<String> otherScmUrlMasks;
    // publishing with maven:
    private File releasatorSettingsXml;
//    String uploadUrl;
//    String publicDownloadUrlBase;


    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public String getVcsType() {
        return vcsType;
    }

    public void setVcsType(String vcsType) {
        this.vcsType = vcsType;
    }

    public String getWebSoftware() {
        return webSoftware;
    }

    public void setWebSoftware(String webSoftware) {
        this.webSoftware = webSoftware;
    }

    public String getVcsIdMask() {
        return vcsIdMask;
    }

    public void setVcsIdMask(String vcsIdMask) {
        this.vcsIdMask = vcsIdMask;
    }

    public String getWebMask() {
        return webMask;
    }

    public void setWebMask(String webMask) {
        this.webMask = webMask;
    }

    public String getScmUrlMask() {
        return scmUrlMask;
    }

    public void setScmUrlMask(String scmUrlMask) {
        this.scmUrlMask = scmUrlMask;
    }

    public Set<String> getOtherScmUrlMasks() {
        return otherScmUrlMasks;
    }

    public void setOtherScmUrlMasks(Set<String> otherScmUrlMasks) {
        this.otherScmUrlMasks = otherScmUrlMasks;
    }

    public File getReleasatorSettingsXml() {
        return releasatorSettingsXml;
    }

    public void setReleasatorSettingsXml(File releasatorSettingsXml) {
        this.releasatorSettingsXml = releasatorSettingsXml;
    }
}
