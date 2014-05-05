/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.LocalDecoder;
import elkfed.coref.features.pairs.crash.FE_Expletive;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.Ranker;
import elkfed.nlp.util.Gender;
import java.util.ArrayList;
import java.util.List;

import static elkfed.coref.algorithms.cand_rank.MixedRankEncoder.FD_IS_NEAREST;
import static elkfed.coref.algorithms.cand_rank.MixedRankEncoder.FD_NEAREST_LR;

/** 
 * hybrid approach that does a maxent-based ranking for pronouns
 * with candidate selection as in Yang et al (2003)
 * and using plain Soon-style decoding for other mentions
 * @author versley
 */
public class MixedRankDecoder extends LocalDecoder {

    private static final boolean FILTER_EXPLETIVES = false;
    private static final int CHUNK_SIZE = 10;
    protected final List<PairFeatureExtractor> _fes_np;
    protected final List<PairFeatureExtractor> _fes_pro;
    protected final OfflineClassifier _oc_np;
    protected final Ranker _r_pro;
    protected final FE_Expletive fee;

    public MixedRankDecoder(List<List<PairFeatureExtractor>> fes,
            OfflineClassifier oc_np,
            Ranker r_pro) {
        if (FILTER_EXPLETIVES) {
            fee = new FE_Expletive();
        } else {
            fee = null;
        }
        _fes_pro = fes.get(0);
        _fes_np = fes.get(1);
        _r_pro = r_pro;
        _oc_np = oc_np;
        ArrayList<FeatureDescription> fds;
        // describe NP classifier header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_np) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        oc_np.setHeader(fds);
        // describe pronoun ranker header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_pro) {
            fe.describeFeatures(fds);
        }
        fds.add(FD_IS_NEAREST);
        fds.add(FD_NEAREST_LR);
        fds.add(PairInstance.FD_POSITIVE);
        r_pro.setHeader(fds);

    }
    private boolean[] expl_it;

    @Override
    protected void setupDocument(List<Mention> mentions) {
        if (FILTER_EXPLETIVES) {
            expl_it = new boolean[mentions.size()];
            for (int i = 0; i < mentions.size(); i++) {
                Mention m_i = mentions.get(i);
                if (fee.is_nonref_it(m_i)) {
                    expl_it[i] = true;
                }
            }
        }
    }

    @Override
    public int resolveSingle(List<Mention> mentions, int ana) {
        if (FILTER_EXPLETIVES && expl_it[ana]) {
            return -1;
        }
        Mention m_ana = mentions.get(ana);
        Mention m_last = null;
        PairInstance inst_last = null;
        if (m_ana.getPronoun()) {
            // pronoun: look for morphologically compatible candidates
            // in the last 2 sentences
            // keep searching if no morph. compatible candidates can be found
            int last_sent = m_ana.getSentId() - 2;
            boolean found = false;
            List<PairInstance> cands = new ArrayList<PairInstance>();
            for (int i = ana - 1; i >= 0; i--) {
                if (FILTER_EXPLETIVES && expl_it[i]) {
                    continue;
                }
                Mention m_i = mentions.get(i);
                if (m_i.getSentId() < last_sent) {
                    if (found) {
                        break;
                    }
                    last_sent--;
                }
                if (m_ana.overlapsWith(m_i) || m_ana.embeds(m_i)) {
                    continue;
                }
                // check morphological compatibility
                if (m_i.getNumber() == m_ana.getNumber() &&
                        (m_i.getGender() == Gender.UNKNOWN ||
                        m_ana.getGender() == Gender.UNKNOWN ||
                        m_i.getGender() == m_ana.getGender()) &&
                        MixedRankEncoder.compatiblePerson(m_i, m_ana)
                        ) {
                    PairInstance inst = new PairInstance(m_ana, m_i);
                    for (PairFeatureExtractor fe : _fes_pro) {
                        fe.extractFeatures(inst);
                    }
                    boolean posInst = m_ana.isCoreferent(m_i);
                    inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                    inst.setFeature(FD_IS_NEAREST, !found);
                    inst.setFeature(FD_NEAREST_LR, false);
                    cands.add(inst);
                    if (!found) {
                        m_last = m_i;
                        inst_last = inst;
                    } else {
                        if (m_i.getSentId() == m_last.getSentId() &&
                                m_last.getSentId()!=m_ana.getSentId()) {
                            m_last = m_i;
                            inst_last = inst;
                        }
                    }
                    found = true;
                }
            }
            if (cands.isEmpty()) {
                return -1;
            }
            inst_last.setFeature(FD_NEAREST_LR, true);
            Mention ante = _r_pro.getHighestRanked(cands).getAntecedent();
            return mentions.indexOf(ante);
        } else {
            List<PairInstance> candLinks = new ArrayList<PairInstance>();
            List<Boolean> result = new ArrayList<Boolean>();
            // non-pronouns: get all candidates from the Soon algorithm
            // and then do some ranking
            base_j_loop:
            for (int base_j = ana - 1; base_j >= 0; base_j -= CHUNK_SIZE) {
                candLinks.clear();
                result.clear();
                final int low_j;
                if (base_j < CHUNK_SIZE) {
                    low_j = 0;
                } else {
                    low_j = base_j - CHUNK_SIZE + 1;
                }
                for (int j = base_j; j >= low_j; j--) {
                    Mention m_j = mentions.get(j);
                    if (m_ana.overlapsWith(m_j) || m_ana.embeds(m_j)) {
                        continue;
                    }
                    PairInstance inst = new PairInstance(m_ana, m_j);
                    for (PairFeatureExtractor fe : _fes_np) {
                        fe.extractFeatures(inst);
                    }
                    // for debugging purposes, add the isCoreferent value to
                    // the learning instances
                    inst.setFeature(PairInstance.FD_POSITIVE,
                            inst.getAnaphor().isCoreferent(inst.getAntecedent()));
                    candLinks.add(inst);
                }
                _oc_np.classify(candLinks, result);
                for (int j = 0; j < candLinks.size(); j++) {
                    if (result.get(j)) {
                        Mention ante = candLinks.get(j).getAntecedent();
                        return mentions.indexOf(ante);
                    }
                }
            }
            return -1;
        }
    }
}
