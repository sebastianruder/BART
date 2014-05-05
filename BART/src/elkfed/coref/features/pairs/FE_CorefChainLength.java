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

/**
 * Extract coref chain features: length of the chain
 *
 * @author massimo
 */
public class FE_CorefChainLength implements PairFeatureExtractor {

    public static final FeatureDescription<Integer> FD_I_CC_LENGTH =
            new FeatureDescription<Integer>(FeatureType.FT_SCALAR, "ante_corefchain_length");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_LENGTH);
    }

    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_I_CC_LENGTH, getChainLength(inst));
    }

    /**
     **/
    public Integer getChainLength(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                return cc.size();
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
                return 0;
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
            return 0;
        }
    }
}
