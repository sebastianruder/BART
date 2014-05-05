/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.entities;

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


//For the StrMatch
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.mmax.minidisc.Markable;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 *
 * @author kepa
 */
public class FE_DE_ModificatorStringMatch implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_MODIFSTRINGMATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeModifStringMatch");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_MODIFSTRINGMATCH);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_MODIFSTRINGMATCH, getDEModifStringMatch(inst));
    }

    public boolean getDEModifStringMatch(PairInstance inst) {
        int matched = 0;

        DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
        DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

        Set<Property> de_ante_att = de_ante.getAttributes();
        Set<Property> de_ana_att = de_ana.getAttributes();

        Set<Property> de_ante_rel = de_ante.getRelations();
        Set<Property> de_ana_rel = de_ana.getRelations();

        Object De_ana_att[] = de_ana_att.toArray();
        Object De_ana_rel[] = de_ana_rel.toArray();

        if (De_ana_att.length == 0) {
            matched = matched;
        } else {

//        for (Property ana_att : De_ana_att) 
            for (int i = 0; i < De_ana_att.length; i++) {
          System.out.println("Att--> " + De_ana_att[i].toString()); 
                if (de_ante_att.contains(De_ana_att[i]) ||
                        de_ante_rel.contains(De_ana_att[i])) {
                    matched++;
                    System.out.println("MATCHED ATT: " + De_ana_att[i].toString());
                } else {
                    matched = matched;
                }
            }
        }


        if (De_ana_att.length == 0) {
            matched = matched;
        } else {

            for (int i = 0; i < De_ana_rel.length; i++) {
                       System.out.println("Rel--> " + De_ana_rel[i].toString()); 
                if (de_ante_att.contains(De_ana_rel[i]) ||
                        de_ante_rel.contains(De_ana_rel[i])) {
                    System.out.println("MATCHED REL: " + De_ana_rel[i].toString());
                    matched++;
                } else {
                    matched = matched;
                }
            }
        }
        if (matched > 0) {
            return true;
        } else {
            return false;
        }
    }
}
    
    
