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
import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairInstance;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.stacking.StackingClassifierFactory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import elkfed.coref.algorithms.soon.PostFilter;

/** uses the Soon et al. scheme for generating instances.
 *
 * @author versley
 */

public class SoonEncoder implements CorefTrainer {
    protected final List<PairFeatureExtractor> _fes;
    protected final InstanceWriter _iw;
    int fold_no=0;
    PostFilter _filter;
    
    public SoonEncoder(List<PairFeatureExtractor> fes, InstanceWriter iw)
            throws IOException {
        _fes=fes;
        _iw=iw;
        _filter= new PostFilter();
         ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        iw.setHeader(fds);
    }

    public void encodeDocument(List<Mention> mentions) throws IOException {
        int i;
        int j;
        for (i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            for (j=i-1; j>=0; j--) {
                Mention m_j=mentions.get(j);
                PairInstance instft=new PairInstance(m_i, m_j);
                if (_filter.FilterOut(instft)) continue;
                if (m_i.isCoreferent(m_j)) {
                    // hooray, found a pair
                    // we only generate negative instances iff
                    // they occur between two positive instances
                    // i.e. not if they occur with a discourse-new mention
                    for (int k=i-1; k>=j; k--) {
                        Mention m_k=mentions.get(k);
                        if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
                            if(!m_i.getRelPronoun()) {
                                if (m_i.overlapsWith(m_k) || m_k.embeds(m_i))
                                continue;
                            }
                        } else {
                            if (m_i.overlapsWith(m_j) || m_k.embeds(m_i))
                            continue;
                        }
                        PairInstance inst=new PairInstance(m_i, m_k);
                        if (_filter.FilterOut(inst)) continue;
                        for (PairFeatureExtractor fe: _fes) {
                            fe.extractFeatures(inst);
                        }
                        boolean posInst=m_i.isCoreferent(m_k);
                        inst.setFeature(StackingClassifierFactory.FD_FOLD_NO,
                                fold_no);
                        inst.setFeature(PairInstance.FD_POSITIVE,posInst);
                        _iw.write(inst);
                    }
                    // merge discourse entities
                   m_i.linkToAntecedent(m_j);
                    break;
                }
            }
        }
        _iw.flush();
        fold_no++;
    }
}
