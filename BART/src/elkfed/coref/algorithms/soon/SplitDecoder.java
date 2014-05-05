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

import java.util.ArrayList;
import java.util.List;

import elkfed.config.ConfigProperties;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.split.Splitting;
import elkfed.coref.mentions.Mention;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author yannick
 */
public class SplitDecoder extends LocalDecoder {
    private static final int CHUNK_SIZE=10;
    
    List<List<PairFeatureExtractor>> _fess;
    List<OfflineClassifier> _models;
    Splitting _spl;
    PostFilter _filter;
    
    public SplitDecoder(List<List<PairFeatureExtractor>> fess,
            List<OfflineClassifier> models,
            Splitting spl) {
        _fess=fess;
        _models=models;
        _spl=spl;
        _filter= new PostFilter();
         for (int i=0; i<_fess.size(); i++)
        {
            ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
            for (PairFeatureExtractor fe : _fess.get(i)) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            _models.get(i).setHeader(fds);
        }
    }
    
    public int resolveSingle(List<Mention> mentions, int ana) {
        List<PairInstance> candLinks=new ArrayList<PairInstance>();
        TIntArrayList types=new TIntArrayList();
        List<Boolean> result=new ArrayList<Boolean>();
        Mention m_i=mentions.get(ana);
        int anaphorType;
        base_j_loop:
        for (int base_j=ana-1; base_j>=0; base_j -= CHUNK_SIZE) {
            candLinks.clear();
            types.clear();
            result.clear();
            final int low_j;
            if (base_j<CHUNK_SIZE)
                low_j=0;
            else
                low_j=base_j-CHUNK_SIZE+1;
            for (int j=base_j; j>=low_j; j--) {
                Mention m_j=mentions.get(j);
                if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
                    if(!m_i.getRelPronoun()) {
                        if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                        continue;
                    }
                } else {
                    if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                    continue;
                }
                anaphorType=_spl.getInstanceType(m_i, m_j);
                if (anaphorType==-1)
                    continue;
                PairInstance inst=new PairInstance(m_i, m_j);
                for (PairFeatureExtractor fe: _fess.get(anaphorType)) {
                    fe.extractFeatures(inst);
                }
                // for debugging purposes, add the isCoreferent value to
                // the learning instances
                inst.setFeature(PairInstance.FD_POSITIVE,
                        inst.getAnaphor().isCoreferent(inst.getAntecedent()));
               if (_filter.FilterOut(inst)) continue;
                 candLinks.add(inst);
                types.add(anaphorType);
            }
            assert types.size()==candLinks.size();
            boolean[] classifications=new boolean[candLinks.size()];
            List<PairInstance> cand2=new ArrayList<PairInstance>(candLinks.size());
            int[] indices=new int[candLinks.size()];
            for (int t=0; t<_models.size(); t++) {
                cand2.clear();
                result.clear();
                //System.err.format("model %d\n",t);
                int k=0;
                for (int j=0; j<candLinks.size(); j++) {
                    if (types.get(j)==t) {
                        //System.err.format("put item %d cand2:%d\n", j, k);
                        indices[k]=j;
                        cand2.add(candLinks.get(j));
                        k++;
                    }
                }
                if (k>0) {
                    _models.get(t).classify(cand2,result);
                    for (int j=0;j<k;j++) {
                        //System.err.format("classified cand2:%d => item %d, result %s\n",
                        //        j,indices[j],
                        //        result.get(j).toString());
                        assert types.get(indices[j])==t;
                        classifications[indices[j]]=result.get(j);
                    }
                }
            }
            for (int j=0; j<classifications.length; j++) {
                if (classifications[j]) {
                    System.err.print(".");
                    PairInstance lnk=candLinks.get(j);
                    return mentions.indexOf(lnk.getAntecedent());
                }
            }
        }
        return -1;
    }
}
