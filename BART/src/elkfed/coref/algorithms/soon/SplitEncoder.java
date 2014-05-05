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
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.split.Splitting;
import elkfed.coref.mentions.Mention;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.stacking.StackingClassifierFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import elkfed.coref.algorithms.soon.PostFilter;

/** implements a multi-classifier model
 *
 * @author yannick
 */
public class SplitEncoder implements CorefTrainer {
    protected final List<List<PairFeatureExtractor>> _fess;
    protected final InstanceWriter[] _iws;
    Splitting _spl;
    int fold_no=0;
    PostFilter _filter;
    
    public SplitEncoder(List<List<PairFeatureExtractor>> fess, InstanceWriter[] iws,
            Splitting spl)
    throws IOException {
        _spl=spl;
        _fess=fess;
        _iws=iws;
        _filter= new PostFilter();
        for (int i=0; i<fess.size(); i++) {
            ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
            for (PairFeatureExtractor fe : _fess.get(i)) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            iws[i].setHeader(fds);
        }
    }
        
    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            int anaphorType;
            for (int j=i-1; j>=0; j--) {
                Mention m_j=mentions.get(j);
                // don't use positive examples that would not be taken
                if (_spl.getInstanceType(m_i, m_j)==-1)
                    continue;
                if (m_i.isCoreferent(m_j)) {
                PairInstance instft=new PairInstance(m_i, m_j);
                if (_filter.FilterOut(instft)) continue;
                    // hooray, found a pair
                    // we only generate negative instances iff
                    // they occur between two positive instances
                    // i.e. not if they occur with a discourse-new mention
                    for (int k=i-1; k>=j; k--) {
                        Mention m_k=mentions.get(k);
                        if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
                            if(!m_i.getRelPronoun()) {
                                if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                                continue;
                            }
                        } else {
                            if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                            continue;
                        }
                        anaphorType=_spl.getInstanceType(m_i,m_k);
                        if (anaphorType==-1)
                            continue;
                        PairInstance inst=new PairInstance(m_i, m_k);
                        if (_filter.FilterOut(inst)) continue;
                        for (PairFeatureExtractor fe: _fess.get(anaphorType)) {
                            fe.extractFeatures(inst);
                        }
                        inst.setFeature(StackingClassifierFactory.FD_FOLD_NO,
                                fold_no);

                        boolean posInst=m_i.isCoreferent(m_k);
                        inst.setFeature(PairInstance.FD_POSITIVE,posInst);
                        _iws[anaphorType].write(inst);
                    }
                    break;
                }
            }
            fold_no++;
        }
        for (InstanceWriter iw: _iws) {
            iw.flush();
        }
    }
}
