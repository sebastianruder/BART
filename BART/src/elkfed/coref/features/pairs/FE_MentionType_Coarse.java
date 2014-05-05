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

package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 * Feature used to determine whether anaphor/ante is definite, demonstrative, pronoun, ne; 
 * 
 * @author olga
 */
public class FE_MentionType_Coarse implements PairFeatureExtractor {
   
    public static final FeatureDescription<Boolean> FD_J_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDefinite");
    public static final FeatureDescription<Boolean> FD_J_IS_DEMONSTRATIVE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemonstrative");
    public static final FeatureDescription<Boolean> FD_J_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsProperName");

    public static final FeatureDescription<Boolean> FD_I_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsDefinite");
    public static final FeatureDescription<Boolean> FD_I_IS_DEMONSTRATIVE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsDemonstrative");
    public static final FeatureDescription<Boolean> FD_I_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsPronoun");
    public static final FeatureDescription<Boolean> FD_I_IS_PN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsProperName");

    public static final FeatureDescription<Boolean> FD_ARE_PROPERNAMES=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "areProperName");

    public static final FeatureDescription<Boolean> FD_ARE_PRONOUNS=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "arePronouns");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_DEFINITE);
        fds.add(FD_J_IS_DEMONSTRATIVE);
        fds.add(FD_J_IS_PRONOUN);
        fds.add(FD_J_IS_PN);
        fds.add(FD_I_IS_DEFINITE);
        fds.add(FD_I_IS_DEMONSTRATIVE);
        fds.add(FD_I_IS_PRONOUN);
        fds.add(FD_I_IS_PN);
        fds.add(FD_ARE_PROPERNAMES);
        fds.add(FD_ARE_PRONOUNS);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_J_IS_DEFINITE,inst.getAnaphor().getDefinite());
        inst.setFeature(FD_J_IS_DEMONSTRATIVE,inst.getAnaphor().getDemonstrative());
        inst.setFeature(FD_J_IS_PN, inst.getAnaphor().getProperName());
        inst.setFeature(FD_J_IS_PRONOUN, inst.getAnaphor().getPronoun());

        inst.setFeature(FD_I_IS_DEFINITE,inst.getAntecedent().getDefinite());
        inst.setFeature(FD_I_IS_DEMONSTRATIVE,inst.getAntecedent().getDemonstrative());
        inst.setFeature(FD_I_IS_PN, inst.getAntecedent().getProperName());
        inst.setFeature(FD_I_IS_PRONOUN, inst.getAntecedent().getPronoun());
        inst.setFeature(FD_ARE_PROPERNAMES, areBothProperNames(inst));
        inst.setFeature(FD_ARE_PRONOUNS, areBothPronouns(inst));
    }
    
    private Boolean areBothProperNames(PairInstance inst)
    {
        return (
                inst.getAntecedent().getProperName()
             &&
                inst.getAnaphor().getProperName()
        );
    }
    private Boolean areBothPronouns(PairInstance inst)
    {
        return (
                inst.getAntecedent().getPronoun()
             &&
                inst.getAnaphor().getPronoun()
        );
    }
}
