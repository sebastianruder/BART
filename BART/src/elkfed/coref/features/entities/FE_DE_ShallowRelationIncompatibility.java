/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.entities;

/**
 *
 * @author kepa
 */
import elkfed.coref.*;

import elkfed.coref.discourse_entities.*;

import elkfed.knowledge.SemanticTreeFeature;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;

//For the StrMatch
public class FE_DE_ShallowRelationIncompatibility implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_SHALLOWRELATIONINCOMPATIBILITY =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeStringIncompatibility");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_SHALLOWRELATIONINCOMPATIBILITY);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_SHALLOWRELATIONINCOMPATIBILITY, getDEShallowRelationIncompatibility(inst));
    }

    public boolean getDEShallowRelationIncompatibility(PairInstance inst) {
        boolean incomp = false;
        Integer incompatibility = 0;
//        if (inst.getAntecedent().getDiscourseEntity() != null && inst.getAnaphor().getDiscourseEntity() != null) {
        if (inst.getAnaphor().getDefinite() || inst.getAnaphor().getIndefinite() || inst.getAnaphor().getPronoun()) {

            DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
            DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

            Set<Property> de_ante_rel = de_ante.getRelations();
            Set<Property> de_ana_rel = de_ana.getRelations();



            if (de_ante_rel.size() == 0 && de_ana_rel.size() > 0) {
                incompatibility++;
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
        return incomp;
    }
}


