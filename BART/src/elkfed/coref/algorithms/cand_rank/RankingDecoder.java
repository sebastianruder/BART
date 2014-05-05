/*
 * RankingDecoder.java
 *
 * Created on August 5, 2007, 8:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.algorithms.soon.*;
import elkfed.coref.AnaphoricityInstance;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.TuningParameters;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.Instance;
import elkfed.ml.Ranker;
import elkfed.ml.maxent.MaxentRanker;
import elkfed.nlp.util.Gender;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class RankingDecoder extends LocalDecoder {

    List<PairFeatureExtractor> _fes;
    List<FeatureExtractor<AnaphoricityInstance>> _afes;
    List<Ranker> _rankers;
    int limits[];
    double resolv_tune[];

    /** Creates a new instance of RankingDecoder */
    public RankingDecoder(List<List<PairFeatureExtractor>> fes,
            List<Ranker> rankers,
            TuningParameters params) {
        _fes = fes.get(0);
        _afes = RankingEncoder.getAnaphoricityFeatures();
        _rankers=rankers;
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
        for (Ranker r : _rankers) {
            r.setHeader(fds);
        }
        limits=new int[4];
        limits[0]=params.getInt("pron_limit", 3);
        limits[1]=params.getInt("fpron_limit", 3);
        limits[2]=params.getInt("nom_limit", 8);
        limits[3]=50;
        resolv_tune=new double[4];
        resolv_tune[0]=params.getFloat("pron_tune", 0.0);
        resolv_tune[1]=params.getFloat("fpron_tune", 0.0);
        resolv_tune[2]=params.getFloat("nom_tune", 0.0);
        resolv_tune[3]=params.getFloat("nam_tune", 0.0);
        String ana_param_name=String.format("%s+^%s+^",
                AnaphoricityInstance.FD_BIAS_NONE.name,
                AnaphoricityInstance.FD_BIAS_NONE.name);
        for (int i=0; i<4; i++) {
            ((MaxentRanker)_rankers.get(i)).adjustWeight(ana_param_name,
                    resolv_tune[i]);
        }
    }

    public int resolveSingle(List<Mention> mentions, int ana) {
        List<Instance> normalPairs = new ArrayList<Instance>();
        Mention m_i = mentions.get(ana);
        boolean antecedent_found = false;
        boolean any_found = false;
        int ana_cls = RankingEncoder.classifyAnaphor(m_i);
        for (int j = ana - 1; j >= 0; j--) {
            Mention m_j = mentions.get(j);
            if (m_i.overlapsWith(m_j)) {
                continue;
            }
            if (m_i.getSentId()-m_j.getSentId()>limits[ana_cls]) {
                break;
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
                any_found=true;
                normalPairs.add(inst);
                antecedent_found = antecedent_found || posInst;
            }
        }
        Instance result = null;
        AnaphoricityInstance instA = new AnaphoricityInstance(m_i);
        for (FeatureExtractor<AnaphoricityInstance> fe : _afes) {
            fe.extractFeatures(instA);
        }
        instA.setFeature(AnaphoricityInstance.FD_BIAS_NONE, true);
        instA.setFeature(PairInstance.FD_POSITIVE, !antecedent_found);
        normalPairs.add(instA);
        result = _rankers.get(ana_cls).getHighestRanked(normalPairs);
        if (!(result instanceof PairInstance)) {
            result = null;
        }
        if (result == null) {
            return -1;
        } else {
            PairInstance inst = (PairInstance) result;
            return mentions.indexOf(inst.getAntecedent());
        }
    }
}