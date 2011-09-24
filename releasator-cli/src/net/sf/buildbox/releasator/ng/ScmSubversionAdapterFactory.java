package net.sf.buildbox.releasator.ng;

import java.io.File;

public class ScmSubversionAdapterFactory implements ScmAdapterFactory {

    public ScmAdapter create(String scmUrl) {
        final File jobDir = new File(ScmUtils.workDir(), ScmUtils.scmUrlAsFileName(scmUrl));
        return new ScmSubversionAdapter(this, scmUrl, jobDir);
    }

    String computeSvnTagBase(String svnUrl) {
        final String tagBase = svnUrl.replaceFirst("(/trunk|/branches)/.*$", "/tags");
        if (svnUrl.equals(tagBase)) {
            //TODO: allow some configuration to cope with this case (no-default-svn-layout)
            throw new ScmException("Failed to compute tagbase for " + svnUrl);
        }
        return tagBase;
    }

}
