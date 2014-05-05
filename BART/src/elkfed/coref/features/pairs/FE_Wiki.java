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
import elkfed.coref.features.pairs.FE_Yago;  //for the emptynoun function only
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;


public class FE_Wiki implements PairFeatureExtractor {
   
    public static final FeatureDescription<Boolean> FD_WIKI1_MATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sameWiki1");
    public static final FeatureDescription<Boolean> FD_WIKI2_MATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sameWiki2");
    public static final FeatureDescription<Boolean> FD_WIKI1_NEMATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sameWiki1ne");
    public static final FeatureDescription<Boolean> FD_WIKI2_NEMATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sameWiki2ne");

    public void describeFeatures(List<FeatureDescription> fds) {

//        fds.add(FD_WIKI1_MATCH);  // the best setting includes just this one

        fds.add(FD_WIKI2_MATCH);
/*
        fds.add(FD_WIKI1_NEMATCH);
        fds.add(FD_WIKI2_NEMATCH);
*/
    }

    public void extractFeatures(PairInstance inst) {
//inst.setFeature(FD_WIKI1_MATCH,WikiMatch(inst,"wiki1"));

inst.setFeature(FD_WIKI2_MATCH,WikiMatch(inst,"wiki2"));
/*
inst.setFeature(FD_WIKI1_NEMATCH,WikiMatchProperNames(inst,"wiki1"));
inst.setFeature(FD_WIKI2_NEMATCH,WikiMatchProperNames(inst,"wiki2"));
*/
    }
    
    private Boolean WikiMatch(PairInstance inst,String wikiattr) {

// discard matching links to aux wiki pages
/*
      if (inst.getAnaphor().getMarkable().getAttributeValue(wikiattr).equals("concept_not_found")) return false;
      if (inst.getAnaphor().getMarkable().getAttributeValue(wikiattr).startsWith("List_of_")) return false;

// discard pronouns

      if (inst.getAnaphor().getPronoun()) return false;
      if (inst.getAntecedent().getPronoun()) return false;


// discard anaphors with a dnew determiner

       if (inst.getAnaphor().getDnewDeterminer()) return false;


//discard same-heads with different number
      if (inst.getAnaphor().getHeadLemma().equalsIgnoreCase(inst.getAntecedent().getHeadLemma()) && (inst.getAnaphor().getNumber() != inst.getAntecedent().getNumber())) return false;

//discard empty nouns
      if (FE_Yago.emptynoun(inst.getAnaphor().getHeadString().toLowerCase())) return false;
      if (FE_Yago.emptynoun(inst.getAntecedent().getHeadString().toLowerCase())) return false;
*/      
      return inst.getAnaphor().getMarkable().getAttributeValue(wikiattr).equals(inst.getAntecedent().getMarkable().getAttributeValue(wikiattr));

    }
    private Boolean WikiMatchProperNames(PairInstance inst, String wikiattr)
    {
        
        if (inst.getAntecedent().getProperName() &&
            inst.getAnaphor().getProperName()) 
           return WikiMatch(inst, wikiattr);
        return false;
    }



}
