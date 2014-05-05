/*
 * FE_HeadMatch.java
 *
 * Created on July 12, 2007, 4:16 PM
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
 Feature used to compare the head strings of the instance pairs NPs. Either T/F
 * @author vae2101
 */
public class FE_HeadMatch implements PairFeatureExtractor {
    
     public static final FeatureDescription<Boolean> FD_IS_HEADMATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "HeadMatch");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_HEADMATCH);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_HEADMATCH,getHead(inst));
    }
    
    private boolean getHead(PairInstance inst)
    {   
        if (
                inst.getAntecedent().getHeadString().
            equalsIgnoreCase(
                inst.getAnaphor().getHeadString()
            )
        )
        {
            return true; 
        }
        else
        { return false; }  
    }
}
