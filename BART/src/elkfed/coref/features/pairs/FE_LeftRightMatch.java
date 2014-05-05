/*
 * FE_LeftRightMatch.java
 *
 * Created on July 12, 2007, 5:36 PM
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
 *
 * @author vae2101
 */
public class FE_LeftRightMatch implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_IS_LMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "LeftMatch");
    
    public static final FeatureDescription<Boolean> FD_IS_RMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "RightMatch");
    
 
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_LMATCH);
        fds.add(FD_IS_RMATCH);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_LMATCH,setLeftMatch(inst));
        inst.setFeature(FD_IS_RMATCH,setRightMatch(inst));
    }
    
   
    /** Sets the left match feature */
    private boolean setLeftMatch(PairInstance inst)
    {
        if (
                (
                    inst.getAntecedent().getMarkableString().toLowerCase().
                        startsWith(
                    inst.getAnaphor().getMarkableString().toLowerCase())
                )
            ||
                (
                    inst.getAnaphor().getMarkableString().toLowerCase().
                        startsWith(
                    inst.getAntecedent().getMarkableString().toLowerCase())
                )
        )       
        {
            return true; // instance.setFeature(feature, Boolean.T.getInt()); 
        }
        else
        {
            return false; // instance.setFeature(feature, Boolean.F.getInt()); 
        }
    }
    
    /** Sets the left match feature */
    private boolean setRightMatch(PairInstance inst)
    {
        if (
                (
                    inst.getAntecedent().getMarkableString().toLowerCase().
                        endsWith(
                    inst.getAnaphor().getMarkableString().toLowerCase())
                )
            ||
                (
                    inst.getAnaphor().getMarkableString().toLowerCase().
                        endsWith(
                    inst.getAntecedent().getMarkableString().toLowerCase())
                )
        )       
        {
            return true; // instance.setFeature(otherFeature, Boolean.T.getInt()); 
        }
        else
        {
            return false; // instance.setFeature(otherFeature, Boolean.F.getInt());
        }
    }
}
