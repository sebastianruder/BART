/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.InstanceWriter;
import elkfed.ml.RankerSink;
import elkfed.nlp.util.Gender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * implements candidate ranking as in Yang et al(2003)
 * candidates are identified by either
 * - selecting compatible antecedents in the last 2 sentences (pronouns) or
 * - taking the candidates returned by a Soon et al-style classifier (testing),
 *   substituted by the antecedent + candidates from sentences around it (training)
 * @author versley
 */
public class CandRankEncoder implements CorefTrainer {

    static boolean compatiblePerson(Mention m1, Mention m2) {
        // TODO: distinguish between 1st and 2nd person pronouns
        // (needs work with LanguagePlugin, MentionType, ...)
        // TODO: maybe put this into a feature extractor???
        int person1, person2;
        if (m1.getPronoun()) {
            if (m1.getIsFirstSecondPerson()) {
                person1=1;
            } else {
                person1=3;
            }
        } else {
            person1=3;
        }
        if (m2.getPronoun()) {
            if (m2.getIsFirstSecondPerson()) {
                person2=1;
            } else {
                person2=3;
            }
        } else {
            person2=3;
        }
        return (person1==person2);
    }

    protected final List<PairFeatureExtractor> _fes_np;
    protected final List<PairFeatureExtractor> _fes_pro;
    protected final InstanceWriter _iw_np;
    protected final RankerSink _r_pro;
    protected final RankerSink _r_np;
    public static FeatureDescription FD_IS_NEAREST =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "isNearest");

    public CandRankEncoder(List<PairFeatureExtractor> fes,
            InstanceWriter iw_np,
            RankerSink r_pro, RankerSink r_np) throws IOException {
        _fes_np = fes;
        _fes_pro = fes;
        _iw_np = iw_np;
        _r_pro = r_pro;
        _r_np = r_np;
        ArrayList<FeatureDescription> fds;
        // describe NP classifier header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_np) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        iw_np.setHeader(fds);
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

    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (int i = 1; i < mentions.size(); i++) {
            Mention m_i = mentions.get(i);
            if (m_i.getPronoun()) {
            // pronoun: look for morphologically compatible candidates
            // in the last 2 sentences
            // keep searching if no morph. compatible candidates can be found
                int last_sent=m_i.getSentId()-2;
                boolean found=false;
                List<PairInstance> cands=new ArrayList<PairInstance>();
                for (int k=i-1;k>=0;k--) {
                    Mention m_k=mentions.get(k);
                    if (m_k.getSentId()<last_sent) {
                        if (found) break;
                        last_sent--;
                    }
                    if (m_i.overlapsWith(m_k) || m_i.embeds(m_k))
                        continue;
                    // check morphological compatibility
                    if (m_k.getNumber()==m_i.getNumber() &&
                            (m_k.getGender()==Gender.UNKNOWN ||
                             m_i.getGender()==Gender.UNKNOWN ||
                             m_k.getGender()==m_i.getGender()))
                        //TODO: check person compatibility
                    {
                        PairInstance inst = new PairInstance(m_i, m_k);
                        for (PairFeatureExtractor fe : _fes_pro) {
                            fe.extractFeatures(inst);
                        }  
                        boolean posInst = m_i.isCoreferent(m_k);
                        inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                        inst.setFeature(FD_IS_NEAREST,!found);
                        cands.add(inst);
                        found=true;
                    }
                }
                _r_pro.write(cands);
            } else {
                // extract binary ante-cand instances
                for (int j = i - 1; j >= 0; j--) {
                    Mention m_j = mentions.get(j);
                    if (m_i.isCoreferent(m_j)) {
                        // hooray, found a pair
                        // we only generate negative instances iff
                        // they occur between two positive instances
                        // i.e. not if they occur with a discourse-new mention
                        for (int k = i - 1; k >= j; k--) {
                            Mention m_k = mentions.get(k);
                            if (m_i.overlapsWith(m_k) || m_i.embeds(m_k)) {
                                continue;
                            }
                            PairInstance inst = new PairInstance(m_i, m_k);
                            for (PairFeatureExtractor fe : _fes_np) {
                                fe.extractFeatures(inst);
                            }
                            boolean posInst = m_i.isCoreferent(m_k);
                            inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                            _iw_np.write(inst);
                        }
                        // non-pronoun: use antecedent + candidates in two neighbouring
                        // sentences
                        List<PairInstance> cands=new ArrayList<PairInstance>();
                        boolean found=false;
                        for (int k=i-1; k>=0;k--) {
                            Mention m_k=mentions.get(k);
                            int sentDiff=m_k.getSentId()-m_j.getSentId();
                            if (sentDiff<-2 || sentDiff>2) continue;
                            PairInstance inst = new PairInstance(m_i, m_k);
                            for (PairFeatureExtractor fe : _fes_np) {
                                fe.extractFeatures(inst);
                            }
                            boolean posInst = m_i.isCoreferent(m_k);
                            inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                            inst.setFeature(FD_IS_NEAREST,!found);
                            cands.add(inst);
                            found=true;
                        }
                        _r_np.write(cands);
                        break;
                    }
                }

            }
        }
        _r_pro.flush();
        _r_np.flush();
        _iw_np.flush();
    }
}
