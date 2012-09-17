package net.sf.buildbox.releasator.ng.impl;

import com.thoughtworks.xstream.XStream;
import net.sf.buildbox.releasator.ng.api.VcsRegistry;
import net.sf.buildbox.releasator.ng.model.VcsFactoryConfig;
import net.sf.buildbox.releasator.ng.model.VcsRepository;
import net.sf.buildbox.releasator.ng.model.VcsRepositoryMatch;
import net.sf.buildbox.util.BbxStringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.manager.ScmManager;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petr Kozelka
 */
public class DefaultVcsRegistry implements VcsRegistry {
    private final List<VcsFactoryConfig> vcsFactoryConfigs = new ArrayList<VcsFactoryConfig>();
    private final List<Candidate> vcsIdCandidates = new ArrayList<Candidate>();
    private final List<Candidate> scmUrlCandidates = new ArrayList<Candidate>();

    private boolean candidatesSorted;
    private ScmManager scmManager;

    public DefaultVcsRegistry() {
        this(new ReleasatorScmManager());
    }

    public DefaultVcsRegistry(ScmManager scmManager) {
        this.scmManager = scmManager;
    }

    public void loadConf(File confDir) throws IOException {
        @SuppressWarnings("unchecked")
        final List<File> files = FileUtils.getFiles(confDir, "*/**/*.xml", null, true);
        final XStream xstream = vcsXstream();
        for (File file : files) {
            System.out.println("Loading vcs configuration from " + file);
            final InputStream is = new FileInputStream(file);
            try {
                final VcsFactoryConfig config = (VcsFactoryConfig) xstream.fromXML(is);
                final String releasatorSettingsXml = config.getReleasatorSettingsXml();
                if (releasatorSettingsXml != null) {
                    final File releasatorSettingsXmlFile = FileUtils.resolveFile(confDir, releasatorSettingsXml);
                    config.setReleasatorSettingsXmlFile(releasatorSettingsXmlFile);
                }
                register(config);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                is.close();
            }
        }
    }

    public static XStream vcsXstream() {
        final XStream xstream = new XStream();
        xstream.alias("VcsFactoryConfig", VcsFactoryConfig.class);
        xstream.aliasField("type", VcsFactoryConfig.class, "vcsType");
        xstream.aliasField("idMask", VcsFactoryConfig.class, "vcsIdMask");
        xstream.addImplicitCollection(VcsFactoryConfig.class, "scmUrlMasks", "scmUrlMask", String.class);
//        xstream.omitField(VcsFactoryConfig.class, "file");
        return xstream;
    }


    public ScmManager getScmManager() {
        return scmManager;
    }

    public List<VcsFactoryConfig> list() {
        return vcsFactoryConfigs;
    }

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
            final String scmUrl = fill(candidate.config.getFirstScmUrlMask(), params);
            vrm.setVcsRepository(createVcsRepository(vrm, scmUrl));
            try {
                //TODO:        scmProviderRepository.setUser();
                //        scmProviderRepository.setPassword();
                //        scmProviderRepository.setPersistCheckout();
                //        scmProviderRepository.setPushChanges();
                vrm.setScmRepository(scmManager.makeScmRepository(scmUrl));
            } catch (ScmException e) {
                throw new IllegalArgumentException(e);
            }
            return vrm;
        }
        return null;
    }

    private VcsRepository createVcsRepository(VcsRepositoryMatch match, String scmUrl) {
        final VcsRepository vcsRepository = new VcsRepository();
        final VcsFactoryConfig config = match.getVcsFactoryConfig();
        vcsRepository.setVcsType(config.getVcsType());
        final Map<String, String> params = match.getMatchedParams();
        vcsRepository.setVcsId(fill(config.getVcsIdMask(), params));
        vcsRepository.setScmUrl(scmUrl);
        final VcsFactoryConfig.ScmWeb scmweb = config.getScmweb();
        if (scmweb != null) {
            vcsRepository.setWebSoftware(scmweb.getLayout());
            vcsRepository.setWebUrl(fill(scmweb.getUrlMask(), params));
        }
        return vcsRepository;
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
            if (o1.config == o2.config) {
                // avoid NPE for unfilled config file
                return 0;
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
