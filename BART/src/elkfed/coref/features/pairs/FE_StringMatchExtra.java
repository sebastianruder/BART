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


import elkfed.config.ConfigProperties;
import elkfed.coref.*;
import elkfed.coref.mentions.Mention;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.MentionType;
import elkfed.lang.NodeCategory;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * Extra features for string matching
 * @author olga
 */
public class FE_StringMatchExtra implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_IS_EXACTSTRINGMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ExactStringMatch");

    public static final FeatureDescription<Boolean> FD_IS_PROSTRINGMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ProStringMatch");
    
    public static final FeatureDescription<Boolean> FD_IS_NONPROSTRINGMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "NonProStringMatch");

    public static final FeatureDescription<Boolean> FD_IS_BAREPLSTRINGMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BarePlStringMatch");

    public static final FeatureDescription<Boolean> FD_IS_BAREPLSTRINGMISMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BarePlStringMisMatch");



    
    public void describeFeatures(List<FeatureDescription> fds) {
/*
        fds.add(FD_IS_PROSTRINGMATCH);        
        fds.add(FD_IS_NONPROSTRINGMATCH);        
*/
        fds.add(FD_IS_BAREPLSTRINGMATCH);        
        fds.add(FD_IS_BAREPLSTRINGMISMATCH);        
        fds.add(FD_IS_EXACTSTRINGMATCH);        
    }

    private boolean isBarePlural(PairInstance inst) {
      if (inst.getAnaphor().mentionType().features.contains(MentionType.Features.isNominal)==false) return false;
      if (inst.getAnaphor().mentionType().features.contains(MentionType.Features.isPlural)==false) return false;
      if (inst.getAnaphor().getDefinite()) return false;
      if (inst.getAnaphor().mentionType().features.contains(MentionType.Features.isPossNominal)) return false;
      if (inst.getAnaphor().getDemNominal()) return false;
      if (inst.getAnaphor().getIndefinite()) return false;


      if (inst.getAntecedent().mentionType().features.contains(MentionType.Features.isNominal)==false) return false;
      if (inst.getAntecedent().mentionType().features.contains(MentionType.Features.isPlural)==false) return false;
      if (inst.getAntecedent().getDefinite()) return false;
      if (inst.getAntecedent().mentionType().features.contains(MentionType.Features.isPossNominal)) return false;
      if (inst.getAntecedent().getDemNominal()) return false;
      if (inst.getAntecedent().getIndefinite()) return false;
      return true;
    }

    public void extractFeatures(PairInstance inst) {
        boolean exactmatch=getExactStringMatch(inst);
        inst.setFeature(FD_IS_EXACTSTRINGMATCH, exactmatch);
        if(isBarePlural(inst)) {
          inst.setFeature(FD_IS_BAREPLSTRINGMATCH, exactmatch);
          inst.setFeature(FD_IS_BAREPLSTRINGMISMATCH, !exactmatch);
        }else{
          inst.setFeature(FD_IS_BAREPLSTRINGMATCH, false);
          inst.setFeature(FD_IS_BAREPLSTRINGMISMATCH, false);
        }
    }
    
    private boolean getExactStringMatch(PairInstance inst)
    {
        LanguagePlugin lang=ConfigProperties.getInstance().getLanguagePlugin();

        if (lang.markableString(inst.getAntecedent().getMarkable()).
                equalsIgnoreCase(lang.markableString(inst.getAnaphor().getMarkable())))
        
            return true; 
        return false; // instance.setFeature(feature, Boolean.F.getInt());

    }
    
}
