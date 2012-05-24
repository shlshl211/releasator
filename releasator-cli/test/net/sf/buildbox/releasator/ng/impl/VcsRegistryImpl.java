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
    private final List<Candidate> vcsIdCandidates = new ArrayList<Candidate>();
    private final List<Candidate> scmUrlCandidates = new ArrayList<Candidate>();

    private boolean candidatesSorted;

    public void register(VcsFactoryConfig config) {
        vcsFactoryConfigs.add(config);
        // vcsId
        vcsIdCandidates.add(Candidate.create(config, config.getVcsIdMask()));

        // scmUrls
        scmUrlCandidates.add(Candidate.create(config, config.getScmUrlMask()));
        for (String scmUrlMask : config.getOtherScmUrlMasks()) {
            scmUrlCandidates.add(Candidate.create(config, scmUrlMask));
        }

        // set dirty flag
        candidatesSorted = false;
    }

    private void sort() {
        if (!candidatesSorted) {
            final CandidateComparator cc = new CandidateComparator();
            Collections.sort(vcsIdCandidates, cc);
            Collections.sort(scmUrlCandidates, cc);
            candidatesSorted = true;
        }
    }

    private VcsRepositoryMatch find(String vcsId, List<Candidate> candidates) {
        sort();
        for (Candidate candidate : candidates) {
            final Matcher m = candidate.pattern.matcher(vcsId);
            if (! m.matches()) continue;
            final VcsRepositoryMatch vrm = new VcsRepositoryMatch();
            vrm.setMatchedMask(candidate.mask);
            vrm.setVcsFactoryConfig(candidate.config);
            return vrm;
        }
        return null;
    }

    public VcsRepositoryMatch findByVcsId(String vcsId) {
        return find(vcsId, vcsIdCandidates);
    }

    public VcsRepositoryMatch findByScmUrl(String scmUrl) {
        return find(scmUrl, scmUrlCandidates);
    }

    private static class CandidateComparator implements Comparator<Candidate> {
        public int compare(Candidate o1, Candidate o2) {
            final int maskLength1 = o1.mask.length();
            final int maskLength2 = o2.mask.length();
            if (maskLength1 != maskLength2) {
                return maskLength1 - maskLength2;
            }
            final String configFileName1 = o1.config.getFile().getName();
            final String configFileName2 = o1.config.getFile().getName();
            return configFileName1.compareTo(configFileName2);
        }
    }

    private static class Candidate {
        VcsFactoryConfig config;
        String mask;
        Pattern pattern;

        public static Candidate create(VcsFactoryConfig config, String mask) {
            final Candidate c = new Candidate();
            c.config = config;
            c.mask = mask;
            String regex = mask.replaceAll("\\[REPOURI\\]", "\\E.*\\Q");
            regex = regex.replaceAll("\\[PATH\\]", "\\E.*\\Q");
            c.pattern = Pattern.compile("\\Q" + regex + "\\E");
            return c;
        }
    }
}
