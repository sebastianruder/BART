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

package elkfed.coref.features.pairs.mentiontype_old;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 * More detailed features to determine type of antecedent (I)
 *
 * @author massimo
 */
public class FE_MentionType_Ante implements PairFeatureExtractor {
    
//    Keep in mind that I is ante, J is antecedent
//    Ante type 
    // two extra features for 2.9b
    public static final FeatureDescription<Boolean> FD_I_IS_PN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsPN");
    public static final FeatureDescription<Boolean> FD_I_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsDefinite");
    public static final FeatureDescription<Boolean> FD_I_IS_INDEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsIndefinite");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_IS_PN);
        fds.add(FD_I_IS_DEFINITE);
    }
    
    /*  Extract features from mention and stores them in instance */
    public void extractFeatures(PairInstance inst) {
        // ante
        // two extra features for 2.9b
        inst.setFeature(FD_I_IS_PN,inst.getAntecedent().getProperName());
        inst.setFeature(FD_I_IS_DEFINITE, inst.getAntecedent().getDefinite());
    }
    

    
}
