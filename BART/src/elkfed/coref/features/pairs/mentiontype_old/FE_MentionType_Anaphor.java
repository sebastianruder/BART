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
 * Additional features providing a finer classification of type of anaphor (J) 
 *
 * @author massimo
 */
public class FE_MentionType_Anaphor implements PairFeatureExtractor {
    
//    Keep in mind that I is ante, J is antecedent
//    Anaphora type
    public static final FeatureDescription<Boolean> FD_J_IS_PN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPN");
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_NOMINAL=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemNominal");
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_REFL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsReflPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PERS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPersPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_POSS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPossPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_REL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsRelPronoun");


    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_PN);
        fds.add(FD_J_IS_DEM_NOMINAL);
        fds.add(FD_J_IS_DEM_PRONOUN);
        fds.add(FD_J_IS_REFL_PRONOUN);
        fds.add(FD_J_IS_REL_PRONOUN);
        fds.add(FD_J_IS_PERS_PRONOUN);
        fds.add(FD_J_IS_POSS_PRONOUN);
    }
    
    /*  Extract features from mention and stores them in instance */
    public void extractFeatures(PairInstance inst) {
        // anaphor
        inst.setFeature(FD_J_IS_PN,inst.getAnaphor().getProperName());
        inst.setFeature(FD_J_IS_DEM_NOMINAL,inst.getAnaphor().getDemNominal()); 
        inst.setFeature(FD_J_IS_DEM_PRONOUN,inst.getAnaphor().getDemPronoun()); 
        inst.setFeature(FD_J_IS_REFL_PRONOUN, inst.getAnaphor().getReflPronoun()); 
        inst.setFeature(FD_J_IS_PERS_PRONOUN, inst.getAnaphor().getPersPronoun());          
        inst.setFeature(FD_J_IS_POSS_PRONOUN, inst.getAnaphor().getPossPronoun());          
        inst.setFeature(FD_J_IS_REL_PRONOUN, inst.getAnaphor().getRelPronoun());          
    }
    
}
