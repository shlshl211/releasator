package net.sf.buildbox.releasator.ng;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScmAdapterManager {
    private final Map<String,ScmAdapterFactory> adaptersByType = new LinkedHashMap<String, ScmAdapterFactory>();

    public void setAdapters(Map<String, ScmAdapterFactory> adaptersByPrefix) {
        this.adaptersByType.putAll(adaptersByPrefix);
    }

    public ScmAdapter create(String scmUrl) {
        if (! scmUrl.startsWith("scm:")) {
            throw new IllegalArgumentException("SCM URL must start with prefix 'scm:'; your url is: " + scmUrl);
        }
        final int colon1 = scmUrl.indexOf(':');
        final int colon2 = scmUrl.indexOf(':', colon1 + 1);
        final String scmType = scmUrl.substring(colon1 + 1, colon2);
        final ScmAdapterFactory scmAdapterFactory = adaptersByType.get(scmType);
        if (scmAdapterFactory == null) {
            throw new IllegalArgumentException(scmType + ": unsupported prefix - " + scmUrl);
        }
        return scmAdapterFactory.create(scmUrl);
    }
}
