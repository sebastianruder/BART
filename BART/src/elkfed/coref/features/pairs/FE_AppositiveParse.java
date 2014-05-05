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
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

/**
 * Feature used to determine whether markble is a possible
 * appositive construct of another markable
 * Uses parse trees, not surface regexp!
 * should not fire for corrdination and "LOC, LOC"
 * @author olga
 */
public class FE_AppositiveParse implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_APPOSITIVE_PRS =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AppositivePrs");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_APPOSITIVE_PRS);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_APPOSITIVE_PRS, getAppositivePrs(inst));
    }

    public static Boolean getAppositivePrs(PairInstance inst) {

// should be in the same sentence
        if (inst.getAnaphor().getSentId()!=inst.getAntecedent().getSentId()) 
           return false;

// exclude pairs where anaphor is an NE -- this might be a bad idea though..
        if (inst.getAnaphor().isEnamex()) 
            return false;


        if (inst.getAntecedent().isEnamex() &&
            inst.getAnaphor().isEnamex()) {

// exclude pairs of NE that have different type

          if (!(inst.getAntecedent().getEnamexType().equals(
                 inst.getAnaphor().getEnamexType())))
              return false;

// exclude pairs of LOC-ne
          if (inst.getAntecedent().getEnamexType().toLowerCase().startsWith("gpe"))
             return false;
          if (inst.getAntecedent().getEnamexType().toLowerCase().startsWith("loc"))
             return false;
        }

// should have not-null  maxnp-trees (otherwise -- problematic mentions)

Tree sentenceTree=inst.getAnaphor().getSentenceTree();
Tree AnaTree=inst.getAnaphor().getMaxNPParseTree();
Tree AnteTree=inst.getAntecedent().getMaxNPParseTree();
if (sentenceTree==null) return false;
if (AnaTree==null) return false;
if (AnteTree==null) return false;


// the structure should be ( * (,) (ANA)) or ( * (,) (ANTE)) -- depends on the ordering, annotation, mention extraction etc

   if (AnteTree.parent(sentenceTree)==AnaTree) {
      Tree[] chlds=AnaTree.children();
      Boolean lastcomma=false;
      for (int i=0; i<chlds.length && chlds[i]!=AnteTree; i++) {
        lastcomma=false;
        if (chlds[i].value().equalsIgnoreCase(",")) lastcomma=true;
      }
      return lastcomma;
   }
   if (AnaTree.parent(sentenceTree)==AnteTree) {

      Tree[] chlds=AnteTree.children();
      Boolean lastcomma=false;
      for (int i=0; i<chlds.length && chlds[i]!=AnaTree; i++) {
        lastcomma=false;
        if (chlds[i].value().equalsIgnoreCase(",")) lastcomma=true;
      }
      return lastcomma;

   }

   return false;

  }
}
