package net.sf.buildbox.releasator.ng.model;

/**
 * @author Petr Kozelka
 */
public class VcsRepository {
    private String vcsType;
    private String vcsId;
    private String scmUrl;
    private String webSoftware;
    private String webUrl;

    public String getVcsType() {
        return vcsType;
    }

    public void setVcsType(String vcsType) {
        this.vcsType = vcsType;
    }

    public String getVcsId() {
        return vcsId;
    }

    public void setVcsId(String vcsId) {
        this.vcsId = vcsId;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public String getWebSoftware() {
        return webSoftware;
    }

    public void setWebSoftware(String webSoftware) {
        this.webSoftware = webSoftware;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("VcsRepository");
        sb.append("{vcsType='").append(vcsType).append('\'');
        sb.append(", vcsId='").append(vcsId).append('\'');
        sb.append(", scmUrl='").append(scmUrl).append('\'');
        sb.append(", webSoftware='").append(webSoftware).append('\'');
        sb.append(", webUrl='").append(webUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
