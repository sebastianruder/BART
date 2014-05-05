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
import elkfed.ml.FeatureType;
import elkfed.ml.Instance;
import elkfed.ml.InstanceWriter;
import elkfed.ml.RankerSink;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author versley
 */
public class TournamentSink implements RankerSink {
    List<FeatureExtractor<CandPairInstance>> _combos;
    InstanceWriter _iw;
    public static final FeatureDescription<Boolean> FD_CHOICE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"Choice");
    
    public TournamentSink(List<FeatureExtractor<CandPairInstance>> combos,
            InstanceWriter iw) {
        _combos=combos;
        _iw=iw;
    }
    
    public void setHeader(List<FeatureDescription> fds) throws IOException {
        List<FeatureDescription> myFDs=new ArrayList<FeatureDescription>();
        for (FeatureExtractor <CandPairInstance> cmb: _combos) {
            cmb.describeFeatures(myFDs);
        }
        myFDs.add(FD_CHOICE);
        _iw.setHeader(myFDs);
    }

    public void write(List<? extends Instance> insts0) throws IOException {
        List<PairInstance> insts=(List<PairInstance>) insts0;
        int num_pos=0, num_neg=0;
        for (PairInstance inst: insts) {
            if (inst.getFeature(PairInstance.FD_POSITIVE))
                num_pos++;
            else
                num_neg++;
        }
        if (num_pos==0 || num_neg==0) return;
        for (int i=0; i<insts.size()-1; i++) {
            PairInstance pair_i=insts.get(i);
            boolean positive_i=pair_i.getFeature(PairInstance.FD_POSITIVE);
            for (int j=i+1; j<insts.size(); j++) {
                PairInstance pair_j=insts.get(j);
                boolean positive_j=pair_j.getFeature(PairInstance.FD_POSITIVE);
                if (positive_i || positive_j) {                    
                    if (positive_i && positive_j) {
                        // ignore pairs where both are positive.
                        // should we do further instance selection
                        // in the case of multiple positive instances?
                    } else {
                        CandPairInstance instP=new CandPairInstance(pair_i, pair_j);
                        for (FeatureExtractor <CandPairInstance> cmb: _combos) {
                            cmb.extractFeatures(instP);
                        }
                        instP.setFeature(FD_CHOICE, positive_i);
                    }
                }
            }
        }
    }

    public void flush() throws FileNotFoundException, IOException {
        _iw.flush();
    }

    public void close() throws FileNotFoundException, IOException {
        _iw.close();
    }

}
