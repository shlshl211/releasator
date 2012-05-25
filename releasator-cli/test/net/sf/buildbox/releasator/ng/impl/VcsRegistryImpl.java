package net.sf.buildbox.releasator.ng.impl;

import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepository;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;
import net.sf.buildbox.util.BbxStringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import java.util.*;
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
        for (String scmUrlMask : config.getScmUrlMasks()) {
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
            if (!m.matches()) continue;
            final VcsRepositoryMatch vrm = new VcsRepositoryMatch();
            vrm.setMatchedMask(candidate.mask);

            // combined list of refs with matcher groups, make key-value pairs
            final Map<String, String> params = new LinkedHashMap<String, String>();
            int groupIndex = 1;
            for (String ref : candidate.refs) {
                final String value = m.group(groupIndex);
                final String oldValue = params.get(ref);
                if (oldValue != null) {
                    if (!oldValue.equals(value)) {
                        // more conflicting values for the same param => not matching in fact!
                        return null;
                    }
                } else {
                    params.put(ref, value);
                }
                groupIndex++;
            }
            vrm.setMatchedParams(params);

            vrm.setVcsFactoryConfig(candidate.config);
            vrm.setVcsRepository(createVcsRepository(vrm));
            try {
                vrm.setScmRepository(createScmRepository(vrm));
            } catch (ScmException e) {
                throw new IllegalArgumentException(e);
            }
            return vrm;
        }
        return null;
    }

    private VcsRepository createVcsRepository(VcsRepositoryMatch match) {
        final VcsRepository vcsRepository = new VcsRepository();
        final VcsFactoryConfig config = match.getVcsFactoryConfig();
        vcsRepository.setVcsType(config.getVcsType());
        final Map<String, String> params = match.getMatchedParams();
        vcsRepository.setVcsId(fill(config.getVcsIdMask(), params));
        vcsRepository.setScmUrl(fill(config.getFirstScmUrlMask(), params));
        final VcsFactoryConfig.ScmWeb scmweb = config.getScmweb();
        if (scmweb != null) {
            vcsRepository.setWebSoftware(scmweb.getLayout());
            vcsRepository.setWebUrl(fill(scmweb.getUrlMask(), params));
        }
        return vcsRepository;
    }

    private ScmRepository createScmRepository(VcsRepositoryMatch vrm) throws ScmException {
        final VcsFactoryConfig config = vrm.getVcsFactoryConfig();
        final VcsRepository vcsRepository = vrm.getVcsRepository();
        final ScmProviderRepository scmProviderRepository;
        final String vcsType = config.getVcsType();
        final String scmurl = vcsRepository.getScmUrl();
        final String url;
        final String prefix = "scm:" + vcsType + ":";
        if (scmurl.startsWith(prefix)) {
            url = scmurl.substring(prefix.length());
        } else if (scmurl.startsWith("scm:")) {
            throw new ScmException("Unknown scm url: " + scmurl);
        } else {
            url = scmurl;
        }
        System.out.println("url = " + url);
        if (vcsType.equals("svn")) {
            scmProviderRepository = new SvnScmProviderRepository(url);
        } else if (vcsType.equals("git")) {
            scmProviderRepository = new GitScmProviderRepository(url);
        } else {
            throw new ScmException("Unknown vcsType: " + vcsType);
        }
//TODO:        scmProviderRepository.setUser();
//        scmProviderRepository.setPassword();
//        scmProviderRepository.setPersistCheckout();
//        scmProviderRepository.setPushChanges();
        return new ScmRepository(vcsType, scmProviderRepository);
    }

    public VcsRepositoryMatch findByVcsId(String vcsId) {
        return find(vcsId, vcsIdCandidates);
    }

    public VcsRepositoryMatch findByScmUrl(String scmUrl) {
        return find(scmUrl, scmUrlCandidates);
    }

    private static class CandidateComparator implements Comparator<Candidate> {
        public int compare(Candidate o1, Candidate o2) {
            final int maskLength1 = o1.normalizedSize;
            final int maskLength2 = o2.normalizedSize;
            if (maskLength1 != maskLength2) {
                return maskLength1 - maskLength2;
            }
            final String configFileName1 = o1.config.getFile().getName();
            final String configFileName2 = o1.config.getFile().getName();
            return configFileName1.compareTo(configFileName2);
        }

    }

    private static String fill(String mask, final Map<String, String> params) {
        return BbxStringUtils.resolveReferences(mask, new BbxStringUtils.ReferenceResolver() {
            public String resolve(String ref) {
                final int n = ref.indexOf("::");
                final String key = n < 0 ? ref : ref.substring(0, n);
                return params.containsKey(key) ? params.get(key) : "{!ERROR!" + ref + "}";
            }
        }, "{", "}");

    }

    private static class Candidate {
        VcsFactoryConfig config;
        String mask;
        Pattern pattern;
        int normalizedSize;
        final List<String> refs = new ArrayList<String>();

        public static Candidate create(VcsFactoryConfig config, String mask) {
            final Candidate c = new Candidate();
            c.config = config;
            c.mask = mask;
            c.normalizedSize = mask.length();
            String regex = BbxStringUtils.resolveReferences(mask, new BbxStringUtils.ReferenceResolver() {
                public String resolve(String ref) {
                    c.normalizedSize -= ref.length();
                    c.normalizedSize--; // = minus [] delimiters plus one for "*" to make placeholders somehow participate on size
                    final int n = ref.indexOf("::");
                    if (n < 0) {
                        c.refs.add(ref);
                        return "\\E(.*)\\Q";
                    } else {
                        c.refs.add(ref.substring(0, n));
                        return "\\E" + ref.substring(n + 2) + "\\Q";
                    }
                }
            }, "{", "}");
            regex = "\\Q" + regex + "\\E";
            regex = regex.replace("\\Q\\E", "");
            c.pattern = Pattern.compile(regex);
            return c;
        }
    }
}
