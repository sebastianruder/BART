/*
 * FE_Number.java
 *
 * Created on July 12, 2007, 5:16 PM
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
 * Feature used to determine number agreement in an instance pair
 * @author vae2101
 */
public class FE_Number implements PairFeatureExtractor {
    
     public static final FeatureDescription<Boolean> FD_IS_NUMBER=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Number");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_NUMBER);            
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_NUMBER,getNumber(inst));
    }
    
    
    public static boolean getNumber(PairInstance inst)
    {
        if (inst.getAnaphor().getNumber() == inst.getAntecedent().getNumber())
        { 
            return true; // instance.setFeature(feature, Boolean.T.getInt());
        } 
        
        else
        {
            return false; // instance.setFeature(feature, Boolean.F.getInt());
        }
    }
}
