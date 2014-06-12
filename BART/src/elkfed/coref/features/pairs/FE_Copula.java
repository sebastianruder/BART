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
import elkfed.knowledge.SemanticClass;

/**
 * Uses parse trees, not surface regexp!
 * @author olga (cleaned up by sebastian)
 */
public class FE_Copula implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_COPULA =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Copula");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_COPULA);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_COPULA, getCopula(inst));
    }

    public static Boolean getCopula(PairInstance inst) {
   
    // should be in the same sentence
      if (inst.getAnaphor().getSentId()!=inst.getAntecedent().getSentId()) {
    	  return false;
      }
           
      // should have not-null  maxnp-trees (otherwise -- problematic mentions)

      Tree sentenceTree = inst.getAnaphor().getSentenceTree();
      Tree AnaTree = inst.getAnaphor().getMaxNPParseTree();
      Tree AnteTree = inst.getAntecedent().getMaxNPParseTree();
      if (sentenceTree == null) {
    	  return false;
      }
      if (AnaTree == null) {
    	  return false;
      }
      if (AnteTree == null) {
    	  return false;
      }

      // exclude "there is .." (ToDo: exclude other expletives!)

      if (inst.getAntecedent().getMarkableString().toLowerCase().matches(NONREF_NP)) {
    	  return false;
      }

      //exclude date and time

      if (inst.getAnaphor().getSemanticClass()==SemanticClass.TIME) {
    	  return false;
      }
      if (inst.getAnaphor().getSemanticClass()==SemanticClass.DATE) {
    	  return false;
      }
      if (inst.getAntecedent().getSemanticClass()==SemanticClass.TIME) {
    	  return false;
      }
      if (inst.getAntecedent().getSemanticClass()==SemanticClass.DATE) {
    	  return false;
      }


      // should be subj-obj of the same verb

      Tree vp = AnaTree.parent(sentenceTree);
      if (vp == null) {
    	  return false;
      }
      if (!vp.value().equalsIgnoreCase("vp")) {
    	  return false;
      }
      while(vp.parent(sentenceTree)!=null && vp.parent(sentenceTree).value().equalsIgnoreCase("vp")) {
    	  vp=vp.parent(sentenceTree);
      }
      if (vp.parent(sentenceTree) == null) {
    	  return false;
      }

      Boolean foundante = false;
      Tree[] chldsup = vp.parent(sentenceTree).children();
      for (int i = 0; i < chldsup.length; i++) {
    	  if (chldsup[i] == AnteTree) {
    		  foundante = true;
    	  }
    	  if (chldsup[i] == vp && foundante == false) {
    	   return false;
    	  }
      }
       
      vp = AnaTree.parent(sentenceTree); // we do not want to go higher here -- "S is *ing O" fires otherwise

      // should not contain a modal verb
      Tree[] chlds = vp.children();
      for (int i = 0; i < chlds.length; i++) {
    	  if (chlds[i].value().equalsIgnoreCase("rb")){
    		  return false;
    	  }
    	  if (chlds[i].value().equalsIgnoreCase("md") &&
    			  chlds[i].getLeaves().get(0).value().toLowerCase().matches(MODAL_VERB)) {
    		  return false;
    	  }
      }
  
      // the verb should be one of the copula verbs

      for (int i=0; i<chlds.length; i++) {

    	  if (chlds[i].value().equalsIgnoreCase("vbd") ||
    			  chlds[i].value().equalsIgnoreCase("aux") ||
    			  chlds[i].value().equalsIgnoreCase("vbn") ||
    			  chlds[i].value().equalsIgnoreCase("vb") ||
    			  chlds[i].value().equalsIgnoreCase("vbd") ||
    			  chlds[i].value().equalsIgnoreCase("vbp") ||
    			  chlds[i].value().equalsIgnoreCase("vbz") ||
    			  chlds[i].value().equalsIgnoreCase("vbg")) {
    		  if (chlds[i].getLeaves().get(0).value().toLowerCase().matches(COPULA_VERB)) {
    			  /*
					System.out.println("Found positive copula verb (" 
 					+chlds[i].getLeaves().get(0).value() +
					") for ("+
					inst.getAnaphor().getMarkableString()+
					"),("+
					inst.getAntecedent().getMarkableString()+
					") ");
    			  */
    			  return true;
    		  }
    	  }
      }
      return false;
   }
}