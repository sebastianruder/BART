/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.coref.features.pairs.FE_BetterNames;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import java.util.regex.Pattern;
import elkfed.ml.TriValued;
import static elkfed.mmax.pipeline.MarkableCreator.SPEAKER_ATTRIBUTE;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 *
 * @author olga
 */
public class FE_SpeakerAlias implements PairFeatureExtractor {

    public static final FeatureDescription<TriValued> FD_SPALIAS_PROPRO=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "SpeakerAliasProPro");

    public static final FeatureDescription<TriValued> FD_SPALIAS_PRONE=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "SpeakerAliasProNE");



    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SPALIAS_PROPRO);
        fds.add(FD_SPALIAS_PRONE);
    }
    public void extractFeatures(PairInstance inst) {

    if (inst.getAnaphor().getPronoun() && inst.getAntecedent().getPronoun()) {
       inst.setFeature(FD_SPALIAS_PRONE, TriValued.UNKNOWN);
       inst.setFeature(FD_SPALIAS_PROPRO, comparepropro(inst.getAnaphor(),inst.getAntecedent()));
       return;
    }
    if (inst.getAnaphor().getPronoun() && inst.getAntecedent().isEnamex()) {
       inst.setFeature(FD_SPALIAS_PROPRO, TriValued.UNKNOWN);
       inst.setFeature(FD_SPALIAS_PRONE, compareprone(inst.getAnaphor(),inst.getAntecedent()));
       return;
    }
    if (inst.getAntecedent().getPronoun() && inst.getAnaphor().isEnamex()) {
       inst.setFeature(FD_SPALIAS_PROPRO, TriValued.UNKNOWN);
       inst.setFeature(FD_SPALIAS_PRONE, compareprone(inst.getAntecedent(),inst.getAnaphor()));
       return;
    }
    inst.setFeature(FD_SPALIAS_PROPRO, TriValued.UNKNOWN);
    inst.setFeature(FD_SPALIAS_PRONE, TriValued.UNKNOWN);

    }

    private static TriValued comparepropro (Mention ana, Mention ante) {
      String sp1=ana.getMarkable().getAttributeValue(SPEAKER_ATTRIBUTE);
      String sp2=ante.getMarkable().getAttributeValue(SPEAKER_ATTRIBUTE);

if (sp1==null) sp1="-";
if (sp2==null) sp2="-";
      if ((sp1.equals("-") || sp1.equals("") || sp1.equals(" ")) &&  
          (sp2.equals("-") || sp2.equals("") || sp2.equals(" ")))
        return TriValued.UNKNOWN;

      if (sp1.equalsIgnoreCase(sp2)) {

        if(ana.getMarkableString().toLowerCase().matches(FIRST_PERSON_SG_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(FIRST_PERSON_SG_PRO))
             return TriValued.TRUE;
          return TriValued.FALSE;
        }
/*
        if(ana.getMarkableString().toLowerCase().matches(FIRST_PERSON_PL_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(FIRST_PERSON_PL_PRO))
             return TriValued.TRUE;
          return TriValued.FALSE;
        }
*/
/*
        if(ana.getMarkableString().toLowerCase().matches(SECOND_PERSON_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(SECOND_PERSON_PRO))
             return TriValued.TRUE;
          return TriValued.FALSE;
        }
*/
        return TriValued.UNKNOWN;
    } // else

        if(ana.getMarkableString().toLowerCase().matches(FIRST_PERSON_SG_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(FIRST_PERSON_SG_PRO))
             return TriValued.FALSE;
          return TriValued.UNKNOWN;
        }
        if(ana.getMarkableString().toLowerCase().matches(FIRST_PERSON_PL_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(FIRST_PERSON_PL_PRO))
             return TriValued.FALSE;
          return TriValued.UNKNOWN;
        }
        if(ana.getMarkableString().toLowerCase().matches(SECOND_PERSON_PRO)) {
          if(ante.getMarkableString().toLowerCase().matches(SECOND_PERSON_PRO))
             return TriValued.FALSE;
          return TriValued.UNKNOWN;
        }
        return TriValued.UNKNOWN;

   }   

    private static TriValued compareprone (Mention pro, Mention ne) {

      if(!pro.getMarkableString().toLowerCase().matches(FIRST_PERSON_SG_PRO)) 
        return TriValued.UNKNOWN;
      
      if (!(ne.getEnamexType().toLowerCase().startsWith("per"))) 
        return TriValued.FALSE;
      if (ne.isCoord()) 
        return TriValued.UNKNOWN;

      String sp=pro.getMarkable().getAttributeValue(SPEAKER_ATTRIBUTE);
if (sp==null) sp="-";
      if (sp.equals("-") || sp.equals("") || sp.equals(" ")) 
        return TriValued.UNKNOWN;

      if (FE_BetterNames.AliasBnamesPS(sp,ne.getHeadOrName()))
        return TriValued.TRUE;
      return TriValued.FALSE;
    }

}
