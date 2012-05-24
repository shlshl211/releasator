package net.sf.buildbox.releasator.ng.impl;

import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petr Kozelka
 */
public class VcsRegistryImpl implements VcsRegistry {
    private final List<VcsFactoryConfig> vcsFactoryConfigs = new ArrayList<VcsFactoryConfig>();

    public void register(VcsFactoryConfig config) {
        vcsFactoryConfigs.add(config);
    }

    public VcsRepositoryMatch findByVcsId(String vcsId) {
        final List<VcsRepositoryMatch> matches = new ArrayList<VcsRepositoryMatch>();
        for (VcsFactoryConfig config : vcsFactoryConfigs) {
            final String mask = config.getVcsIdMask();
            final Pattern pattern = Pattern.compile("\\Q" + mask.replaceAll("REPOURI","\\E.*\\Q") + "\\E");
            final Matcher m = pattern.matcher(vcsId);
            if (! m.matches()) continue;
            final VcsRepositoryMatch vrm = new VcsRepositoryMatch();
            vrm.setMatchedMask(mask);
            vrm.setVcsFactoryConfig(config);
        }
        if (matches.isEmpty()) {
            return null;
        }
        Collections.sort(matches, new VcsRepositoryMatchComparator());
        return matches.get(0);
    }

    public VcsRepositoryMatch findByScmUrl(String scmUrl) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class VcsRepositoryMatchComparator implements Comparator<VcsRepositoryMatch> {
        public int compare(VcsRepositoryMatch o1, VcsRepositoryMatch o2) {
            final int maskLength1 = o1.getMatchedMask().length();
            final int maskLength2 = o2.getMatchedMask().length();
            if (maskLength1 != maskLength2) {
                return maskLength1 - maskLength2;
            }
            final String configFileName1 = o1.getVcsFactoryConfig().getConfig().getName();
            final String configFileName2 = o1.getVcsFactoryConfig().getConfig().getName();
            return configFileName1.compareTo(configFileName2);
        }
    }
}
