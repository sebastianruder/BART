/*
 * FE_MentionType_Salience.java
 *
 * Created on August 3, 2007, 11:03 AM
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
 * More detailed features to determine type of anaphor (J) 
 *
 * @author massimo
 */
public class FE_MentionType_Fine_Ana implements PairFeatureExtractor {
    
//    Anaphora type
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_NOMINAL=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemNominal");
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_REFL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsReflPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_REL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsRelPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PERS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPersPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_POSS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPossPronoun");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_DEM_NOMINAL);
        fds.add(FD_J_IS_DEM_PRONOUN);
        fds.add(FD_J_IS_REFL_PRONOUN);
        fds.add(FD_J_IS_REL_PRONOUN);
        fds.add(FD_J_IS_PERS_PRONOUN);
        fds.add(FD_J_IS_POSS_PRONOUN);

    }
    
    /*  Extract features from mention and stores them in instance */
    public void extractFeatures(PairInstance inst) {

        inst.setFeature(FD_J_IS_DEM_NOMINAL,inst.getAnaphor().getDemNominal()); 
        inst.setFeature(FD_J_IS_DEM_PRONOUN,inst.getAnaphor().getDemPronoun()); 
        inst.setFeature(FD_J_IS_REFL_PRONOUN, inst.getAnaphor().getReflPronoun()); 
        inst.setFeature(FD_J_IS_REL_PRONOUN, inst.getAnaphor().getRelPronoun()); 
        inst.setFeature(FD_J_IS_PERS_PRONOUN, inst.getAnaphor().getPersPronoun());          
        inst.setFeature(FD_J_IS_POSS_PRONOUN, inst.getAnaphor().getPossPronoun());          

    }
    
    
}
