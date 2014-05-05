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
public class FE_DE_ShallowAttributeDirectionIncompatibility implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_SHALLOWATTRIBUTEDIRECTIONINCOMPATIBILITY =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeShallowAtributeDirectionIncompatibility");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_SHALLOWATTRIBUTEDIRECTIONINCOMPATIBILITY);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_SHALLOWATTRIBUTEDIRECTIONINCOMPATIBILITY, getDEShallowAttributeIncompatibility(inst));
    }

    public boolean getDEShallowAttributeIncompatibility(PairInstance inst) {
        boolean incomp = false;
        Integer incompatibility = 0;
//        if (inst.getAntecedent().getDiscourseEntity() != null && inst.getAnaphor().getDiscourseEntity() != null) {
        if (inst.getAnaphor().getDefinite() || inst.getAnaphor().getIndefinite() || inst.getAnaphor().getPronoun()) {

            DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
            DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

            Set<Property> de_ante_att = de_ante.getAttributes();
            Set<Property> de_ana_att = de_ana.getAttributes();



            for (Property ana_att : de_ana_att) {
                if (!(de_ante_att.contains(ana_att))) {
                    incompatibility++;
                    break;
                }
            }
        }

        if (incompatibility > 0) {
            incomp = true;
        }
        return incomp;
    }
}
