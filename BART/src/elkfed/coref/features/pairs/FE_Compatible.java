/*
 * FE_SemClass.java
 *
 * Created on August 20, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.knowledge.SemanticClass;
import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.ml.TriValued;
import elkfed.config.ConfigProperties;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.FE_CCommand;
import elkfed.coref.features.pairs.FE_Span;
import elkfed.coref.features.pairs.FE_Appositive_iCab;
import elkfed.coref.features.pairs.FE_AppositiveParse;
import elkfed.coref.features.pairs.FE_SemClassAgree;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.List;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.HashMap;
import java.util.Map;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;

/**
 * ante is the closes mention to ana, that has suitable agreement properties,
does not violate c-command, does not make a pronoun-nonpro pair, and they do not have the same maxnp (exception: appositive)
 * NB: assumes that mentions are in linear order!
 * will not produce mess otherwise, but will not be useful either
 * (will just output "some compatible"
 * @author olga
 */
public class FE_Compatible implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_IS_COMPATIBLE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"Compatible");
    public static final FeatureDescription<Boolean> FD_IS_COMPATIBLE_ANAPRO=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"Compatible_AnaPro");

    public FE_Compatible() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_COMPATIBLE);
        fds.add(FD_IS_COMPATIBLE_ANAPRO);
    }
    
    public void extractFeatures(PairInstance inst) {

        Boolean compa=Compatible(inst);
        inst.setFeature(FD_IS_COMPATIBLE,compa);
        if (inst.getAnaphor().getPronoun()) 
          inst.setFeature(FD_IS_COMPATIBLE_ANAPRO,compa);
        else
          inst.setFeature(FD_IS_COMPATIBLE_ANAPRO,false);
    }

public static Boolean Compatible(PairInstance inst) {


// discard pronominal antecedents (allow only pro-pro)
if (inst.getAntecedent().getPronoun() && !inst.getAnaphor().getPronoun()) return false;


// discard pairs that violate gender, number, semclass, animacy agreement
//NB: animacy not needed, follows from semclass

   if (!FE_Number.getNumber(inst)) return false;
   if (FE_SemClassAgree.compareSemClassHierarchy(inst)==TriValued.FALSE)
          return false;
   if (FE_Gender.getGender(inst)==TriValued.FALSE)
          return false;

// allow appositives, appo_icab -- they should not be affected by further constraints

   if (FE_Appositive_iCab.getAppositive(inst)) return true;
   if (FE_AppositiveParse.getAppositivePrs(inst)) return true;

// discard pairs that violate c-command
   if (FE_CCommand.getCCommand(inst)) return false;

//discard pairs where one maxnp is embedded in another 

   if (FE_Span.getSpanEmbed(inst)) return false;

   return true;

}


}
