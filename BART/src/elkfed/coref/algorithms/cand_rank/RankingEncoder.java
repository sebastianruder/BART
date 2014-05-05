/*
 * RankingEncoder.java
 *
 * Created on August 4, 2007, 3:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.AnaphoricityInstance;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.TuningParameters;
import elkfed.coref.features.anaphoricity.AFE_NPType;
import elkfed.coref.features.anaphoricity.AFE_PronounString;
import elkfed.coref.features.anaphoricity.AFE_SynPos;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.Instance;
import elkfed.ml.RankerSink;
import elkfed.nlp.util.Gender;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class RankingEncoder implements CorefTrainer {

    List<RankerSink> _sinks;
    List<PairFeatureExtractor> _fes;
    List<FeatureExtractor<AnaphoricityInstance>> _afes;
    int[] limits;

    public static List<FeatureExtractor<AnaphoricityInstance>> getAnaphoricityFeatures() {
        List<FeatureExtractor<AnaphoricityInstance>> result =
                new ArrayList<FeatureExtractor<AnaphoricityInstance>>();
        result.add(new AFE_NPType());
        result.add(new AFE_PronounString());
        result.add(new AFE_SynPos());
        return result;
    }

    public static int classifyAnaphor(Mention m) {
        if (m.getPronoun()) {
            if (m.getIsFirstSecondPerson()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (m.getProperName()) {
                return 3;
            } else {
                return 2;
            }
        }
    }

    /** Creates a new instance of RankingEncoder */
    public RankingEncoder(List<List<PairFeatureExtractor>> fess,
            List<RankerSink> rankers,
            TuningParameters params)
            throws FileNotFoundException, IOException {
        //TODO: use more than one feature combination
        _sinks = rankers;
        _fes = fess.get(0);
        _afes = getAnaphoricityFeatures();
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        for (FeatureExtractor<AnaphoricityInstance> fe : _afes) {
            fe.describeFeatures(fds);
        }
        fds.add(AnaphoricityInstance.FD_BIAS_NONE);
        fds.add(CandRankEncoder.FD_IS_NEAREST);
        fds.add(PairInstance.FD_POSITIVE);
        for (RankerSink rs : _sinks) {
            rs.setHeader(fds);
        }
        limits=new int[4];
        limits[0]=params.getInt("pron_limit", 3);
        limits[1]=params.getInt("fpron_limit", 3);
        limits[2]=params.getInt("nom_limit", 8);
        limits[3]=50;
    }

    //TODO: perform more filtering for non-pronouns
    // (i.e., only non-pronominal antecedents that either have
    //  matching heads or are less than 4 sentences away)
    public void encodeDocument(List<Mention> mentions) throws IOException {

        List<Instance> normalPairs = new ArrayList<Instance>();
        for (int i = 1; i < mentions.size(); i++) {
            normalPairs.clear();
            Mention m_i = mentions.get(i);
            int ana_cls = classifyAnaphor(m_i);
            boolean any_found = false;
            boolean antecedent_found=false;
            for (int j = i - 1; j >= 0; j--) {
                Mention m_j = mentions.get(j);
                if (m_i.overlapsWith(m_j)) {
                    continue;
                }
                if (m_j.getNumber() == m_i.getNumber() &&
                        (m_j.getGender() == Gender.UNKNOWN ||
                        m_i.getGender() == Gender.UNKNOWN ||
                        m_j.getGender() == m_i.getGender())) {
                    PairInstance inst = new PairInstance(m_i, m_j);
                    //if (m_i.getPronoun())
                    for (PairFeatureExtractor fe : _fes) {
                        fe.extractFeatures(inst);
                    }
                    boolean posInst = m_i.isCoreferent(m_j);
                    inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                    inst.setFeature(CandRankEncoder.FD_IS_NEAREST, !any_found);
                    any_found = true;
                    antecedent_found |= posInst;
                    normalPairs.add(inst);
                }
            }
            if (!normalPairs.isEmpty()) {
                AnaphoricityInstance instA = new AnaphoricityInstance(m_i);
                for (FeatureExtractor<AnaphoricityInstance> fe : _afes) {
                    fe.extractFeatures(instA);
                }
                instA.setFeature(AnaphoricityInstance.FD_BIAS_NONE, true);
                instA.setFeature(PairInstance.FD_POSITIVE, !antecedent_found);
                normalPairs.add(instA);
                _sinks.get(ana_cls).write(normalPairs);
            }
        }
        for (RankerSink rs: _sinks) {
            rs.flush();
        }
    }

    public void close() throws FileNotFoundException, IOException {
        for (RankerSink rs: _sinks) {
            rs.close();
        }
    }
}
