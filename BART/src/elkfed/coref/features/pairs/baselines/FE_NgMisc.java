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

package elkfed.coref.features.pairs.baselines;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.ml.TriValued;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.FE_Span;
import elkfed.coref.features.pairs.FE_CCommand;
import elkfed.coref.features.pairs.FE_SameMaxNP;


/**
 * Features from Soon et al that are not represented by single FEs
 * (mention type)
 * @author olga
 */
public class FE_NgMisc implements PairFeatureExtractor {
   
    public static final FeatureDescription<Boolean> FD_J_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPronoun");


    public static final FeatureDescription<Boolean> FD_I_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsPronoun");

    public static final FeatureDescription<Boolean> FD_ARE_PROPERNAMES=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "areProperName");

    public static final FeatureDescription<Boolean> FD_ARE_PRONOUNS=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "arePronouns");

    public static final FeatureDescription<TriValued> FD_IS_AGREE=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "agreeNumGen");

    public static final FeatureDescription<Boolean> FD_IS_SYNTAX=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "syntaxConstrants");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_PRONOUN);  //cf. MentionType_Coarse
        fds.add(FD_I_IS_PRONOUN); //cf. MentionType_Coarse
        fds.add(FD_ARE_PROPERNAMES); //cf. MentionType_Coarse
        fds.add(FD_ARE_PRONOUNS); //cf. MentionType_Coarse
        fds.add(FD_IS_AGREE); //composite morph agree feature
        fds.add(FD_IS_SYNTAX); //composite synt constraints feature


    }

    public void extractFeatures(PairInstance inst) {


        inst.setFeature(FD_J_IS_PRONOUN, inst.getAnaphor().getPronoun());

        inst.setFeature(FD_I_IS_PRONOUN, inst.getAntecedent().getPronoun());
        inst.setFeature(FD_ARE_PROPERNAMES, areBothProperNames(inst));
        inst.setFeature(FD_ARE_PRONOUNS, areBothPronouns(inst));
        inst.setFeature(FD_IS_AGREE, agreeNG(inst));
        inst.setFeature(FD_IS_SYNTAX, syntaxMSC(inst));
    }
    
    private  static Boolean syntaxMSC(PairInstance inst) {
      if (FE_Span.getSpanEmbed(inst)) return true;
      if (FE_SameMaxNP.getSameMaxNP(inst)) return true;
      if (FE_CCommand.getCCommand(inst)) return true;
      return false;
    }
    private  static TriValued agreeNG(PairInstance inst) {
//hm.. this looks strange, but that's what they say in the paper
      if (FE_Number.getNumber(inst)) {
        if (FE_Gender.getGender(inst)==TriValued.TRUE) return TriValued.TRUE;
      }else{
        if (FE_Gender.getGender(inst)==TriValued.FALSE) return TriValued.FALSE;
      }

      return TriValued.UNKNOWN;
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
