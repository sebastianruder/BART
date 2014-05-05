/*
 * Copyright 2007 Project EML Research
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
 * Feature used to determine whether anaphor is definite, demonstrative; 
 * or whether anaphor and antecendent are both proper names or pronouns.
 *
 * This is a variant that is bug-compatible to the old Soon et al. implementation
 * i.e. X_IS_DEFINITE returns true for 3rd person plural pronouns
 * @author versley
 */
public class FE_MentionType_Buggy implements PairFeatureExtractor {
   
    public static final FeatureDescription<Boolean> FD_J_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDefinite");
    public static final FeatureDescription<Boolean> FD_J_IS_DEMONSTRATIVE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemonstrative");
    public static final FeatureDescription<Boolean> FD_I_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPronoun");
    public static final FeatureDescription<Boolean> FD_ARE_PROPERNAMES=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "areProperName");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_DEFINITE);
        fds.add(FD_J_IS_DEMONSTRATIVE);
        fds.add(FD_I_IS_PRONOUN);
        fds.add(FD_J_IS_PRONOUN);
        fds.add(FD_ARE_PROPERNAMES);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_J_IS_DEFINITE,
                inst.getAnaphor().getMarkableString().toLowerCase().startsWith("the"));
        inst.setFeature(FD_J_IS_DEMONSTRATIVE,inst.getAnaphor().getDemonstrative());
        inst.setFeature(FD_I_IS_PRONOUN, inst.getAntecedent().getPronoun());
        inst.setFeature(FD_J_IS_PRONOUN, inst.getAnaphor().getPronoun());
        inst.setFeature(FD_ARE_PROPERNAMES, areBothProperNames(inst));
    }
    
    private Boolean areBothProperNames(PairInstance inst)
    {
        return (
                inst.getAntecedent().getProperName()
             &&
                inst.getAnaphor().getProperName()
        );
    }
}
