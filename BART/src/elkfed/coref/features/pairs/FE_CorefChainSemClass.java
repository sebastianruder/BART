/*
 * FE_CorefChain.java
 *
 * Created on March 2nd, 2008
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.*;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;

import elkfed.mmax.util.NPHeadFinder;
import elkfed.knowledge.SemanticClass;

/**
 * Extract coref chain features: semantic agreement
 *
 * @author kepa
 */
public class FE_CorefChainSemClass implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_I_CC_SEM_CLASS_COMPAT =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sem_class_compat");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_SEM_CLASS_COMPAT);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_I_CC_SEM_CLASS_COMPAT, getSemClassCompat(inst));
    }

    /**
     **/
    public Boolean getSemClassCompat(PairInstance inst) {
        int compatYes = 0;
        int compatNo = 0;
        int compatUnknown = 0;
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    //if semantic class of anaphor or member of coref chain is unknown
                    if (inst.getAnaphor().getSemanticClass().equals(SemanticClass.UNKNOWN) ||
                            cc.get(idx).getSemanticClass().equals(SemanticClass.UNKNOWN)) {
                        System.out.println("  ==> CHECKPOINT 1: 1 " + inst.getAnaphor().getSemanticClass() + " 2 " + cc.get(idx).getSemanticClass());
                        if (NPHeadFinder.getInstance().getHeadLemma(inst.getAnaphor().getMarkable()).
                                equals(NPHeadFinder.getInstance().getHeadLemma(cc.get(idx).getMarkable()))) {
                            idx++;
                            compatYes++;
                            System.out.println("  ==> CHECKPOINT 2");
                        } else {
                            idx++;
                            //    compatUnknown++;
                            //    compatNo++;
                            System.out.println("  ==> CHECKPOINT 3");
                        }
                    } //if both semantic classes are the same or one is the parent
                    //of the other.
                    else if ((SemanticClass.isaPerson(inst.getAnaphor().getSemanticClass()) &&
                            SemanticClass.isaPerson(cc.get(idx).getSemanticClass())) ||
                            (SemanticClass.isaNumeric(inst.getAnaphor().getSemanticClass()) &&
                            SemanticClass.isaNumeric(cc.get(idx).getSemanticClass())) ||
                            (inst.getAnaphor().getSemanticClass() ==
                            cc.get(idx).getSemanticClass())) {
                        System.out.println("  ==> CHECKPOINT 4: 1" + inst.getAnaphor().getSemanticClass() + " 2 " + cc.get(idx).getSemanticClass());
                        idx++;
                        compatYes++;
                    } else {
                        idx++;
                        compatNo++;
                    //                      System.out.println("  ==> CHECKPOINT 5");
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
//        if ((compatYes > 0) && (compatNo == 0)) {
        if (compatYes > 0) {
            return true;
        } else {
            return false;
        }
    }
}
