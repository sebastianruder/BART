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
package elkfed.expletives;

import edu.stanford.nlp.process.Morphology;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 * extracts the lemmas of the preceding and following verbs
 * @author versley
 */
public class EF_LRVerb implements FeatureExtractor<ExpletiveInstance> {
    FeatureDescription<String> FD_LEFT_VERB=
            new FeatureDescription<String>(FeatureType.FT_STRING,
                    "verbL");
    FeatureDescription<String> FD_RIGHT_VERB=
            new FeatureDescription<String>(FeatureType.FT_STRING,
                    "verbR");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_LEFT_VERB);
        fds.add(FD_RIGHT_VERB);
    }

    public void extractFeatures(ExpletiveInstance inst) {
        String[] pos=inst.getPOS();
        String[] words=inst.getWords();
        int idx=inst.getIdx();
        for (int i=idx-1; i>=0; i--) {
            if (pos[i].startsWith("VB")) {
                inst.setFeature(FD_LEFT_VERB, 
                        Morphology.stemStatic(words[i],pos[i]).toString());
                break;
            }
        }
        //TODO: look for the full verb - i.e. skip auxiliaries
        for (int i=idx+1; i<pos.length; i++) {
            if (pos[i].startsWith("VB")) {
                inst.setFeature(FD_RIGHT_VERB, 
                        Morphology.stemStatic(words[i],pos[i]).toString());
                break;
            }
        }        
    }

}
