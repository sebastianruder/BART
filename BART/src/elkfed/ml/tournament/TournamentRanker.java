/*
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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
package elkfed.ml.tournament;

import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.Ranker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author versley
 */
public class TournamentRanker implements Ranker {
    List<FeatureExtractor<CandPairInstance>> _combos;
    OfflineClassifier _oc;
    
    public TournamentRanker(List<FeatureExtractor<CandPairInstance>> combos,
            OfflineClassifier oc) {
        _combos=combos;
        _oc=oc;
    }

    public void setHeader(List<FeatureDescription> fds) {
        List<FeatureDescription> myFDs=new ArrayList<FeatureDescription>();
        for (FeatureExtractor <CandPairInstance> cmb: _combos) {
            cmb.describeFeatures(myFDs);
        }
        myFDs.add(TournamentSink.FD_CHOICE);
        _oc.setHeader(myFDs);
    }

    public double getScore(Instance inst) {
        throw new UnsupportedOperationException("Not for tournament ranking");
    }

    public <T extends PairInstance> int[] do_ranking(List<T> cands) {
        int[] result=new int[cands.size()];
        List<CandPairInstance> problems=new ArrayList<CandPairInstance>();
        List<Boolean> response=new ArrayList<Boolean>();
        for (int i=0; i<cands.size()-1; i++) {
            PairInstance cand_i=cands.get(i);
            boolean pos_i=cand_i.getFeature(PairInstance.FD_POSITIVE);
            for (int j=i+1; j<cands.size(); j++) {
                PairInstance cand_j=cands.get(j);
                boolean pos_j=cand_j.getFeature(PairInstance.FD_POSITIVE);
                CandPairInstance instP=new CandPairInstance(cand_i,cand_j);
                for (FeatureExtractor <CandPairInstance> cmb: _combos) {
                    cmb.extractFeatures(instP);
                }
                if (pos_i && !pos_j) {
                    instP.setFeature(TournamentSink.FD_CHOICE, true);
                } else if (!pos_i && pos_j) {
                    instP.setFeature(TournamentSink.FD_CHOICE, false);                    
                }
                problems.add(instP);
            }
        }
        _oc.classify(problems, response);
        int idx=0;
        for (int i=0; i<cands.size()-1; i++) {
            for (int j=i+1; j<cands.size(); j++) {
                boolean choice=response.get(i);
                if (choice) {
                    result[i]++;
                } else {
                    result[j]++;
                }
            }
        }
        return result;
    }
    public <T extends Instance> T getHighestRanked(List<T> cands) {
        int[] result=do_ranking((List<PairInstance>)cands);
        int best_score=-1;
        T best_cand=null;
        for (int i=0; i<cands.size(); i++) {
            // TODO: what to do if two candidates have the same
            // number of wins
            if (result[i]>best_score) {
                best_cand=cands.get(i);
                best_score=result[i];
            }
        }
        return best_cand;
    }

    public <T extends Instance> List<T> getRanking(List<T> cands) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
