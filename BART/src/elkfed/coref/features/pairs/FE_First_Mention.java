/*
 * FE_First_Mention.java
 *
 * Created on August 7, 2007, 10:56 PM
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
 * Extract first mention feature
 *
 * @author massimo
 */
public class FE_First_Mention implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_FIRST_MENTION=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsFirstMention");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_FIRST_MENTION);
    }
    
    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_FIRST_MENTION,inst.getAntecedent().getIsFirstMention());
    }
    
}
