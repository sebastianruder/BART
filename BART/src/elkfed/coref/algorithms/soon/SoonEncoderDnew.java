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
import elkfed.coref.features.pairs.FE_Dnew;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.stacking.StackingClassifierFactory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import elkfed.config.ConfigProperties;
import elkfed.lang.GermanLanguagePlugin;

/** uses the Soon et al. scheme for generating instances.
 *
 * @author olga, based on yannick's expletives stuff
 */
public class SoonEncoderDnew implements CorefTrainer {

    protected final List<PairFeatureExtractor> _fes;
    protected final List<PairFeatureExtractor> _fesdnew;
    protected final InstanceWriter _iw;
    protected final InstanceWriter _iwdnew;
    int fold_no = 0;

    public SoonEncoderDnew(List<PairFeatureExtractor> fes, InstanceWriter iw,List<PairFeatureExtractor> fesdnew, InstanceWriter iwdnew)
            throws IOException {
        _fes = fes;
        _fesdnew = fesdnew;
        _iw = iw;
        _iwdnew=iwdnew;

        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        iw.setHeader(fds);

        ArrayList<FeatureDescription> fdsdnew = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fesdnew) {
            fe.describeFeatures(fdsdnew);
        }
        fdsdnew.add(PairInstance.FD_POSITIVE);
        iwdnew.setHeader(fdsdnew);
    }

    public void encodeDocument(List<Mention> mentions) throws IOException {
        boolean[] expl_it = new boolean[mentions.size()];

/* ---------- dnew part ----------- */
        for (int i = 0; i < mentions.size(); i++) {
            boolean noantefound=true;
            Mention m_i = mentions.get(i);
            for (int j = i - 1; j >= 0 && noantefound ; j--) {
                Mention m_j = mentions.get(j);
                if (m_i.isCoreferent(m_j))  
                   noantefound=false;
            }
                                                
            PairInstance inst = new PairInstance(m_i, m_i);
            for (PairFeatureExtractor fe : _fes) {
              fe.extractFeatures(inst);
            }
            inst.setFeature(PairInstance.FD_POSITIVE, noantefound);
            _iwdnew.write(inst);

        }
        _iwdnew.flush();
    

/*    --------- link part ----------- */

        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            for (int j=i-1; j>=0; j--) {
                Mention m_j=mentions.get(j);
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
