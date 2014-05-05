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

//For the StrMatch
/**
 * Extract coref chain features: length of the antecedent chain > 1
 *
 * @author kepa
 */
public class FE_CorefChainLengthMoreThan1 implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_I_CC_LENGTHMORETHAN1 =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_lengthmorethan1");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_LENGTHMORETHAN1);
    }

    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_I_CC_LENGTHMORETHAN1, getLenghtMoreThanOne(inst));
    }

    /**
     **/
    public Boolean getLenghtMoreThanOne(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        Vector<Mention> cc = de.getcorefChain();
        if (cc != null && cc.size() > 1) {
            return true;
        } else {
            return false;
        }
    }
}
