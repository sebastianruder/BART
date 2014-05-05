/*
 *   Copyright 2007 Project ELERFED
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

import elkfed.coref.mentions.Mention;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;

import elkfed.config.ConfigProperties;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.NodeCategory;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * Reinhart's c-command
 * @author olga
 */
public class FE_CCommand implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_CCOMMAND =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "CCommand");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_CCOMMAND);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_CCOMMAND, getCCommand(inst));
    }

    public static Boolean getCCommand(PairInstance inst) {

// should be in the same sentence
      if (inst.getAnaphor().getSentId()!=inst.getAntecedent().getSentId()) 
           return false;

//Ana should not be reflexive or reciprocal pronoun
      if (inst.getAnaphor().getReflPronoun()) return false;


// should have not-null  maxnp-trees (otherwise -- problematic mentions)

      Tree sentenceTree=inst.getAnaphor().getSentenceTree();
      Tree AnaTree=inst.getAnaphor().getMaxNPParseTree();
      Tree AnteTree=inst.getAntecedent().getMaxNPParseTree();
      if (sentenceTree==null) return false;
      if (AnaTree==null) return false;
      if (AnteTree==null) return false;


// should not dominate each other
      if (AnaTree.dominates(AnteTree)) return false;
      if (AnteTree.dominates(AnaTree)) return false;

//the first branching node for ante should dominate ana (but not via S-node)
      AnteTree=AnteTree.parent(sentenceTree);
      while(AnteTree!=null ) {
      
        if (AnteTree.children().length>1) {
           if (!AnteTree.dominates(AnaTree)) return false;
           while(AnaTree!=null && AnaTree!=AnteTree) {
             if (AnaTree.value().toLowerCase().startsWith("s")) return false;
             AnaTree=AnaTree.parent(sentenceTree);
           }
           return true;
        }  
        AnteTree=AnteTree.parent(sentenceTree);
      }

      return false;
   }

}