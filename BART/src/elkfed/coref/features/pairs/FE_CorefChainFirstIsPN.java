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

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.List;

/**
 * Extract coref chain features: first is PN
 *
 * @author massimo
 */
public class FE_CorefChainFirstIsPN implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_I_CC_FIRST_IS_PN =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_first_is_pn");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_FIRST_IS_PN);
    }

    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_I_CC_FIRST_IS_PN,
                inst.getAntecedent().getDiscourseEntity().firstMention_isProperName());
    }
}
