/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.entities;

/**
 *
 * @author kepa
 */

/*
 * Checks wether there is an alias/string incompatibility  
 * for heads of the NP and arguments of post-modifiers 
 */
import elkfed.coref.*;
import elkfed.coref.discourse_entities.DiscourseEntity;

import elkfed.coref.discourse_entities.*;
import elkfed.coref.mentions.*;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Object;

import elkfed.ml.TriValued;
import elkfed.nlp.util.Gender;
import elkfed.mmax.util.NPHeadFinder;
import elkfed.knowledge.SemanticClass;

import elkfed.util.DateParser;
import elkfed.knowledge.DE_SemanticTree;


//For the StrMatch
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.mmax.minidisc.Markable;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.lang.EnglishLinguisticConstants.*;
import elkfed.coref.features.pairs.FE_Alias;

public class FE_DE_AliasIncompatibility implements PairFeatureExtractor {

    public static final FeatureDescription<Double> FD_DE_ALIASINCOMPATIBILITY =
            new FeatureDescription<Double>(FeatureType.FT_SCALAR, "DeAliasIncompatibility");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_ALIASINCOMPATIBILITY);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_ALIASINCOMPATIBILITY, getDEAliasIncompatibility(inst));
    }

    public double getDEAliasIncompatibility(PairInstance inst) {

        double incompatibility = 0;
        double counter = 0;

        DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
        DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();


        // Attributes will be not used in the actual version of alias
        // Set<Property> de_ante_att = de_ante.getAttributes();
        // Set<Property> de_ana_att = de_ana.getAttributes();

        Set<Property> de_ante_rel = de_ante.getRelations();
        Set<Property> de_ana_rel = de_ana.getRelations();

        Set<Property> de_ante_heads = de_ante.getTypes();
        Set<Property> de_ana_heads = de_ana.getTypes();


        for (Property ana_head : de_ana_heads) {
            if (!(de_ante_heads.contains(ana_head))) {
                incompatibility = incompatibility - 1;
                counter++;
                break;
            } else {
                incompatibility = incompatibility + 1;
                counter++;
//                break;
            }
        }

        if (de_ante_rel.size() > 0 && de_ana_rel.size() > 0) {
            for (Property ana_rel : de_ana_rel) {
                for (Property ante_rel : de_ante_rel) {

                }
            }

        }

        return 1;
    }
}
