/*
 * Copyright 2007 Project ELERFED
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
package elkfed.coref.algorithms.soon;

import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.crash.FE_Expletive;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.stacking.StackingClassifierFactory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/** uses the Soon et al. scheme for generating instances.
 *
 * @author versley
 */
public class SoonEncoder_Expl implements CorefTrainer {

    protected final List<PairFeatureExtractor> _fes;
    protected final InstanceWriter _iw;
    int fold_no = 0;
    FE_Expletive fee = new FE_Expletive();

    public SoonEncoder_Expl(List<PairFeatureExtractor> fes, InstanceWriter iw)
            throws IOException {
        _fes = fes;
        _iw = iw;
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        iw.setHeader(fds);
    }

    public void encodeDocument(List<Mention> mentions) throws IOException {
        boolean[] expl_it = new boolean[mentions.size()];

        for (int i = 0; i < mentions.size(); i++) {
            Mention m_i = mentions.get(i);
            if (fee.is_nonref_it(m_i)) {
                expl_it[i] = true;
            }
        }
        for (int i = 1; i < mentions.size(); i++) {
            if (expl_it[i]) { continue; }
            Mention m_i = mentions.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Mention m_j = mentions.get(j);
                if (expl_it[j]) { continue; }
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
                        if (expl_it[j]) { continue; }
                        PairInstance inst = new PairInstance(m_i, m_k);
                        for (PairFeatureExtractor fe : _fes) {
                            fe.extractFeatures(inst);
                        }
                        boolean posInst = m_i.isCoreferent(m_k);
                        inst.setFeature(StackingClassifierFactory.FD_FOLD_NO,
                                fold_no);
                        inst.setFeature(PairInstance.FD_POSITIVE, posInst);
                        _iw.write(inst);
                    }
                    break;
                }
            }
        }
        _iw.flush();
        fold_no++;
    }
}
