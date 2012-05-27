package net.sf.buildbox.releasator.ng.model;

import java.io.File;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class VcsFactoryConfig {
    private File file;
    private String vcsType;
    private String vcsIdMask;
    private List<String> scmUrlMasks;

    private ScmWeb scmweb;
    private DistroUrls releaseArtifactDistribution;
    private DistroUrls releaseSiteDistribution;

    // publishing with maven:
    private File releasatorSettingsXml;
//    String uploadUrl;
//    String publicDownloadUrlBase;


    public ScmWeb getScmweb() {
        return scmweb;
    }

    public void setScmweb(ScmWeb scmweb) {
        this.scmweb = scmweb;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getVcsType() {
        return vcsType;
    }

    public void setVcsType(String vcsType) {
        this.vcsType = vcsType;
    }

    public String getVcsIdMask() {
        return vcsIdMask;
    }

    public void setVcsIdMask(String vcsIdMask) {
        this.vcsIdMask = vcsIdMask;
    }

    public String getFirstScmUrlMask() {
        return scmUrlMasks.get(0);
    }

    public List<String> getScmUrlMasks() {
        return scmUrlMasks;
    }

    public void setScmUrlMasks(List<String> otherScmUrlMasks) {
        this.scmUrlMasks = otherScmUrlMasks;
    }

    public File getReleasatorSettingsXml() {
        return releasatorSettingsXml;
    }

    public void setReleasatorSettingsXml(File releasatorSettingsXml) {
        this.releasatorSettingsXml = releasatorSettingsXml;
    }


    public static class ScmWeb {
        private String layout;
        private String urlMask;

        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }

        public String getUrlMask() {
            return urlMask;
        }

        public void setUrlMask(String urlMask) {
            this.urlMask = urlMask;
        }
    }

    public static class DistroUrls {
        private String publicUrl;
        private String downloadUrl;
        private String uploadUrl;

        public String getPublicUrl() {
            return publicUrl;
        }

        public void setPublicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getUploadUrl() {
            return uploadUrl;
        }

        public void setUploadUrl(String uploadUrl) {
            this.uploadUrl = uploadUrl;
        }
    }
}
