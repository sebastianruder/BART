/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.coref.algorithms.cand_rank;

import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.InstanceWriter;
import elkfed.ml.RankerSink;
import elkfed.ml.stacking.StackingClassifierFactory;
import elkfed.nlp.util.Gender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * hybrid approach that does a maxent-based ranking for pronouns
 * with candidate selection as in Yang et al (2003)
 * and using plain Soon-style decoding for other mentions
 * @author versley
 */
public class MixedRankEncoder implements CorefTrainer {

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
        // allow 'normal' antecedents to speech pronouns
        //return (person2==1&&SemanticClass.isaPerson(m1.getSemanticClass())
        //        || person1==person2);
        if (SemanticClass.isaPerson(m1.getSemanticClass())) {
            return SemanticClass.isaPerson(m2.getSemanticClass()) ||
                    m2.getSemanticClass()==SemanticClass.UNKNOWN;
        } else if (m1.getSemanticClass() == SemanticClass.UNKNOWN) {
            return true;
        } else {
            return !SemanticClass.isaPerson(m2.getSemanticClass()) ||
                    m2.getSemanticClass() == SemanticClass.UNKNOWN;
        }
    }

    protected final List<PairFeatureExtractor> _fes_np;
    protected final List<PairFeatureExtractor> _fes_pro;
    protected final InstanceWriter _iw_np;
    protected final RankerSink _r_pro;
    public static FeatureDescription FD_IS_NEAREST =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "isNearest");
    public static FeatureDescription FD_NEAREST_LR =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "isNearestLR");
    int fold_no = 0;

    public MixedRankEncoder(List<List<PairFeatureExtractor>> fess,
            InstanceWriter iw_np,
            RankerSink r_pro) throws IOException {
        _fes_pro = fess.get(0);
        _fes_np = fess.get(1);
        _iw_np = iw_np;
        _r_pro = r_pro;
        ArrayList<FeatureDescription> fds;
        // describe NP classifier header
        fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes_np) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        iw_np.setHeader(fds);
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

    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (int i = 1; i < mentions.size(); i++) {
            Mention m_i = mentions.get(i);
            Mention m_last=null;
            PairInstance inst_last=null;
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
                             m_k.getGender()==m_i.getGender()) &&
                             compatiblePerson(m_k, m_i))
                        //TODO: check person compatibility
                    {
                        PairInstance inst = new PairInstance(m_i, m_k);
                        for (PairFeatureExtractor fe : _fes_pro) {
                            fe.extractFeatures(inst);
                        }  
                        boolean posInst = m_i.isCoreferent(m_k);
                        inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                        inst.setFeature(FD_IS_NEAREST,!found);
                        inst.setFeature(StackingClassifierFactory.FD_FOLD_NO,
                                fold_no);
                        inst.setFeature(FD_NEAREST_LR,false);
                        cands.add(inst);
                        if (!found) {
                            m_last=m_k;
                            inst_last=inst;
                        } else {
                            // we do left-to-right recency except for the
                            // "current" sentence.
                            if (m_k.getSentId()==m_last.getSentId() &&
                                    m_last.getSentId()!=m_i.getSentId()) {
                                m_last=m_k;
                                inst_last=inst;
                            }
                        }
                        found=true;
                    }
                }
                if (inst_last!=null)
                    inst_last.setFeature(FD_NEAREST_LR,true);
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
                            inst.setFeature(StackingClassifierFactory.FD_FOLD_NO,
                                fold_no);
                            _iw_np.write(inst);
                        }
                        break;
                    }
                }
            }
        }
        _r_pro.flush();
        _iw_np.flush();
        fold_no++;
    }
}
