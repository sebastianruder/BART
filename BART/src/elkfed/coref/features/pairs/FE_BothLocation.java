/*
 * FE_BothLocation.java
 *
 * Created on August 17, 2007, 8:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class FE_BothLocation implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_BOTH_LOCATION=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BothLocation");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_BOTH_LOCATION);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_BOTH_LOCATION,
                inst.getAnaphor().getSemanticClass()==
                    SemanticClass.LOCATION &&
                inst.getAntecedent().getSemanticClass()==
                    SemanticClass.LOCATION);
    }
    
}
