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
public class FE_DE_ModificatorSetSubsumedVP implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_MODIFSETSUBSUMEDVP =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeModifSetSubsumedVP");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_MODIFSETSUBSUMEDVP);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_MODIFSETSUBSUMEDVP, getDEModificatorSetSubsumedVP(inst));
    }

    public boolean getDEModificatorSetSubsumedVP(PairInstance inst) {

        // boolean subsumed = true;

        DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
        DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

        Set<Property> de_ante_att = de_ante.getAttributes();
        Set<Property> de_ana_att = de_ana.getAttributes();

        Set<Property> de_ante_rel = de_ante.getRelations();
        Set<Property> de_ana_rel = de_ana.getRelations();

        Set<Property> de_ante_heads = de_ante.getTypes();
        Set<Property> de_ana_heads = de_ana.getTypes();

        for (Property ana_rel : de_ana_rel) {
            if (!(de_ante_att.contains(ana_rel))) {
                return false;
            }
        }

        for (Property ana_head : de_ana_heads) {
            if (de_ante_att.size() == 0 &&
                    !(de_ante_heads.contains(ana_head))) {
                return false;
            } else if (de_ante_att.size() == 0 &&
                    de_ante_heads.contains(ana_head)) {
                return true;
            }
        }

        for (Property ana_att : de_ana_att) {
            if (!(de_ante_att.contains(ana_att))) {
                return false;
            }
        }

        return true;
    }
}
