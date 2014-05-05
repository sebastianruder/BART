/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.LocalDecoder;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.Ranker;
import elkfed.nlp.util.Gender;
import java.util.ArrayList;
import java.util.List;

import static elkfed.coref.algorithms.cand_rank.CandRankEncoder.FD_IS_NEAREST;

/** 
 * implements candidate ranking as in Yang et al(2003)
 * candidates are identified by either
 * - selecting compatible antecedents in the last 2 sentences (pronouns) or
 * - taking the candidates returned by a Soon et al-style classifier (testing),
 *   substituted by the antecedent + candidates from sentences around it (training)
 * @author versley
 */
public class CandRankDecoder extends LocalDecoder {
    protected final List<PairFeatureExtractor> _fes_np;
    protected final List<PairFeatureExtractor> _fes_pro;
    protected final OfflineClassifier _oc_np;
    protected final Ranker _r_pro;
    protected final Ranker _r_np;
    
    public CandRankDecoder(List<PairFeatureExtractor> fes,
            OfflineClassifier oc_np,
            Ranker r_pro, Ranker r_np) {
        _fes_np = fes;
        _fes_pro = fes;
        _oc_np=oc_np;
        _r_pro=r_pro; _r_np=r_np;
        ArrayList<FeatureDescription> fds;
        // describe NP classifier header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_np) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        oc_np.setHeader(fds);
        // describe NP ranker header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_np) {
            fe.describeFeatures(fds);
        }
        fds.add(FD_IS_NEAREST);
        fds.add(PairInstance.FD_POSITIVE);
        r_np.setHeader(fds);
        // describe pronoun ranker header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_pro) {
            fe.describeFeatures(fds);
        }
        fds.add(FD_IS_NEAREST);
        fds.add(PairInstance.FD_POSITIVE);
        r_pro.setHeader(fds);

    }
    
    @Override
    public int resolveSingle(List<Mention> mentions, int ana) {
        Mention m_ana=mentions.get(ana);
        if (m_ana.getPronoun()) {
            // pronoun: look for morphologically compatible candidates
            // in the last 2 sentences
            // keep searching if no morph. compatible candidates can be found
            int last_sent=m_ana.getSentId()-2;
                boolean found=false;
                List<PairInstance> cands=new ArrayList<PairInstance>();
                for (int i=ana-1;i>=0;i--) {
                    Mention m_i=mentions.get(i);
                    if (m_i.getSentId()<last_sent) {
                        if (found) break;
                        last_sent--;
                    }
                    if (m_ana.overlapsWith(m_i) || m_ana.embeds(m_i))
                        continue;
                   // check morphological compatibility
                    if (m_i.getNumber()==m_ana.getNumber() &&
                            (m_i.getGender()==Gender.UNKNOWN ||
                             m_ana.getGender()==Gender.UNKNOWN ||
                             m_i.getGender()==m_ana.getGender())) //&&
                             //CandRankEncoder.compatiblePerson(m_i,m_ana))
                    {
                        PairInstance inst = new PairInstance(m_ana, m_i);
                        for (PairFeatureExtractor fe : _fes_pro) {
                            fe.extractFeatures(inst);
                        }  
                        boolean posInst = m_ana.isCoreferent(m_i);
                        inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                        inst.setFeature(FD_IS_NEAREST,!found);
                        cands.add(inst);
                        found=true;
                    }
                }
                if (cands.isEmpty()) return -1;
                Mention ante=_r_pro.getHighestRanked(cands).getAntecedent();
                return mentions.indexOf(ante);
        } else {
            // non-pronouns: get all candidates from the Soon algorithm
            // and then do some ranking
            List<PairInstance> cands1=new ArrayList<PairInstance>();
            List<Boolean> result1=new ArrayList<Boolean>();
            for (int i=ana-1;i>=0;i--) {
                Mention m_i=mentions.get(i);
                if (m_ana.overlapsWith(m_i) || m_ana.embeds(m_i))
                    continue;
                PairInstance inst=new PairInstance(m_ana, m_i);
                for (PairFeatureExtractor fe : _fes_np) {
                     fe.extractFeatures(inst);
                }
                boolean posInst = m_ana.isCoreferent(m_i);
                inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                cands1.add(inst);
            }
            if (cands1.isEmpty()) return -1;
            _oc_np.classify(cands1, result1);
            List<PairInstance> cands2=new ArrayList<PairInstance>();
            boolean found=false;
            for (int i=cands1.size()-1;i>=0;i--) {
                if (result1.get(i)) {
                    PairInstance inst=cands1.get(i);
                    inst.setFeature(FD_IS_NEAREST,!found);
                    cands2.add(0,inst);
                    found=true;
                }
            }
            if (cands2.isEmpty()) {
                return -1;
            }
            Mention ante=_r_np.getHighestRanked(cands2).getAntecedent();
            return mentions.indexOf(ante);
        }
    }
}
