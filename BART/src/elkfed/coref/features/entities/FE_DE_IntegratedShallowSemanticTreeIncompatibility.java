/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.entities;

/**
 *
 * @author kepa.rodriguez
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
import elkfed.knowledge.SemanticClass;

//For the StrMatch
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.mmax.minidisc.Markable;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.lang.EnglishLinguisticConstants.*;

public class FE_DE_IntegratedShallowSemanticTreeIncompatibility implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_INTEGR_SHALLOWSEM_TREE_INCOMP =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "IntegratedShallowSemanticTreeIncompatibility");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_INTEGR_SHALLOWSEM_TREE_INCOMP);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_INTEGR_SHALLOWSEM_TREE_INCOMP, getIntegratedShallowSemanticTreeIncompatibility(inst));
    }

    public boolean getIntegratedShallowSemanticTreeIncompatibility(PairInstance inst) {
        boolean incomp = false;
        Integer incompatibility = 0;
        if (inst.getAntecedent().getDiscourseEntity() != null && inst.getAnaphor().getDiscourseEntity() != null) {
            if (inst.getAnaphor().getDefinite() || inst.getAnaphor().getIndefinite() || inst.getAnaphor().getPronoun() || inst.getAnaphor().getProperName()) {

                DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
                DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

                HashMap<String, Set<Property>> de_ante_names = de_ante.getNames();
                HashMap<String, Set<Property>> de_ana_names = de_ana.getNames();

                Set<Property> de_ante_att = de_ante.getAttributes();
                Set<Property> de_ana_att = de_ana.getAttributes();

                Set<Property> de_ante_rel = de_ante.getRelations();
                Set<Property> de_ana_rel = de_ana.getRelations();

                Set<Property> de_ante_heads = de_ante.getTypes();
                Set<Property> de_ana_heads = de_ana.getTypes();

//                if (inst.getAnaphor().getSemanticClass() == inst.getAntecedent().getSemanticClass()) {
                if (inst.getAnaphor().getPronoun() && (inst.getAnaphor().getSemanticClass() != inst.getAntecedent().getSemanticClass())) {
                    incompatibility++;
                }


                if (de_ante_names.get("Role") != null &&
                        de_ana_names.get("Role") != null) {
//            System.out.println("Roles1");
                    if (((de_ante_names.get("Role").contains("mr.") ||
                            de_ante_names.get("Role").contains("sir")) &&
                            (de_ana_names.get("Role").contains("madam") ||
                            de_ana_names.get("Role").contains("miss") ||
                            de_ana_names.get("Role").contains("mrs") ||
                            de_ana_names.get("Role").contains("lady"))) ||
                            /* check for gender compatibility */
                            ((de_ana_names.get("Role").contains("mr.") ||
                            de_ana_names.get("Role").contains("sir")) &&
                            (de_ante_names.get("Role").contains("madam") ||
                            de_ante_names.get("Role").contains("miss") ||
                            de_ante_names.get("Role").contains("mrs") ||
                            de_ante_names.get("Role").contains("lady")))) {
                        incompatibility++;
                        System.out.println("Incompatibility: Role");
                    }
                }
                if (de_ante_names.get("Forename") != null && de_ana_names.get("Forename") != null &&
                        de_ante_names.get("Forename").size() > 0) {
//            System.out.println("Forename1");
                    for (Property ana_names_fn : de_ana_names.get("Forename")) {
                        System.out.println("Checking FN " + ana_names_fn.toString());
                        for (Property de_ante_name : de_ante_names.get("Forename")) {
                            System.out.println("DE-ANTE-FORE: " + de_ante_name.toString());
                        }
                        if (!(de_ante_names.get("Forename").contains(ana_names_fn))) {
                            incompatibility++;
                            System.out.println("Incompatibility: Forename");
                            break;
                        }
                    }
                }
                if (de_ante_names.get("Surname") != null && de_ana_names.get("Surname") != null &&
                        de_ante_names.get("Surname").size() > 0) {
                    for (Property ana_names_sn : de_ana_names.get("Surname")) {
                        System.out.println("Checking SN " + ana_names_sn.toString());
                        for (Property de_ante_name : de_ante_names.get("Surname")) {
                            System.out.println("DE-ANTE-SURN: " + de_ante_name.toString());
                        }
                        if (!(de_ante_names.get("Surname").contains(ana_names_sn))) {
                            incompatibility++;
                            System.out.println("Incompatibility: Lastname");
                            break;
                        }
                    }
                }


                if (inst.getAnaphor().getPronoun() &&
                        (inst.getAnaphor().getSemanticClass() != inst.getAntecedent().getSemanticClass())) {
                    incompatibility++;
                }


                for (Property ana_head : de_ana_heads) {
                    if (!de_ante_heads.contains(ana_head)) {
                        incompatibility++;
                        break;

                    }
                }

                if (de_ante_att.size() > 0 && de_ana_att.size() > 0) {
                    for (Property ana_att : de_ana_att) {
                        if (!(de_ante_att.contains(ana_att))) {
                            incompatibility++;
                            break;
                        }
                    }
                }
                if (de_ante_rel.size() > 0 && de_ana_rel.size() > 0) {
                    for (Property ana_rel : de_ana_rel) {
                        for (Property ante_rel : de_ante_rel) {
                            if (ana_rel.getPredicate().toString().equals(ante_rel.getPredicate().toString()) &&
                                    !(ana_rel.getArgument().toString().equals(ante_rel.getArgument().toString()))) {
                                incompatibility++;
                                break;
                            }
                        }
                    }
                }

            }
            if (incompatibility > 0) {
                incomp = true;
            }
        }
        return incomp;
    }
}
