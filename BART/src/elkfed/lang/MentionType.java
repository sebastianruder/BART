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
package elkfed.lang;

import elkfed.knowledge.SemanticClass;
import elkfed.nlp.util.Gender;
import java.util.EnumSet;

/**
 * contains all the linguistic (as opposed to implementation-dependent)
 * information on a mention.
 * @author versley
 */
public class MentionType {
    // TODO: isDemPronoun is just isDemonstrative&isPronoun,
    // similar for isDemNominal. Could we maybe get rid of them?
    public enum Features
    {
        isProperName, isEnamex,
        isPronoun, isNominal, isCoord,
        isDefinite, isDemonstrative,
        isFirstSecondPerson, isReflexive, isRelative,
        isPersPronoun, isPossPronoun, isDemPronoun,
        isDemNominal, isIndefinite, isDnewDeterminer,
// isDnewDeterminer == "another N" etc
        isPossNominal, //"her N" etc
        isSingular, isPlural;
    }

    public String toStr () {
      String s="";
      if (features.contains(Features.isProperName)) s+="ProperName_";
      if (features.contains(Features.isEnamex)) s+="Enamex_";
      if (features.contains(Features.isPronoun)) s+="Pronoun_";
      if (features.contains(Features.isNominal)) s+="Nominal_";
      if (features.contains(Features.isCoord)) s+="Coord_";
      if (features.contains(Features.isDefinite)) s+="Definite_";
      if (features.contains(Features.isIndefinite)) s+="Indefinite_";
      if (features.contains(Features.isDemonstrative)) s+="Demonstrative_";
      if (features.contains(Features.isFirstSecondPerson)) s+="Pro12_";
      if (features.contains(Features.isReflexive)) s+="ProReflex_";
      if (features.contains(Features.isRelative)) s+="ProRel_";
      if (features.contains(Features.isPersPronoun)) s+="ProPers_";
      if (features.contains(Features.isPossPronoun)) s+="ProPoss_";
      if (features.contains(Features.isDemPronoun)) s+="ProDem_";
      if (features.contains(Features.isDemNominal)) s+="NomDem_";
      if (features.contains(Features.isDnewDeterminer)) s+="DnewDet_";
      if (features.contains(Features.isPossNominal)) s+="NomPoss_";
      if (features.contains(Features.isSingular)) s+="Sg_";
      if (features.contains(Features.isPlural)) s+="Pl_";
      return s;
    }
    public EnumSet<Features> features=EnumSet.noneOf(Features.class);
    public Gender gender=Gender.UNKNOWN;
    public SemanticClass semanticClass=SemanticClass.UNKNOWN;
}
