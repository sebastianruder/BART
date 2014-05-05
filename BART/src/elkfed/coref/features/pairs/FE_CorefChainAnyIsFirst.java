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
 * Extract coref chain features: any is first
 *
 * @author massimo
 */
public class FE_CorefChainAnyIsFirst implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_FIRST =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_any_is_first");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_ANY_IS_FIRST);
    }

    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_I_CC_ANY_IS_FIRST,
                inst.getAntecedent().getDiscourseEntity().anyMention_isFirstMention());
    }
    /**
     **/
}
