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
 * More detailed features to determine type of antecedent (I) 
 *
 * @author olga
 */
public class FE_MentionType_Fine_Ante implements PairFeatureExtractor {
    
//    Antecedent type
    public static final FeatureDescription<Boolean> FD_I_IS_DEM_NOMINAL=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsDemNominal");
    public static final FeatureDescription<Boolean> FD_I_IS_DEM_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsDemPronoun");
    public static final FeatureDescription<Boolean> FD_I_IS_REFL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsReflPronoun");
    public static final FeatureDescription<Boolean> FD_I_IS_REL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsRelPronoun");
    public static final FeatureDescription<Boolean> FD_I_IS_PERS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsPersPronoun");
    public static final FeatureDescription<Boolean> FD_I_IS_POSS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anteIsPossPronoun");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_IS_DEM_NOMINAL);
        fds.add(FD_I_IS_DEM_PRONOUN);
        fds.add(FD_I_IS_REFL_PRONOUN);
        fds.add(FD_I_IS_REL_PRONOUN);
        fds.add(FD_I_IS_PERS_PRONOUN);
        fds.add(FD_I_IS_POSS_PRONOUN);

    }
    
    /*  Extract features from mention and stores them in instance */
    public void extractFeatures(PairInstance inst) {

        inst.setFeature(FD_I_IS_DEM_NOMINAL,inst.getAntecedent().getDemNominal()); 
        inst.setFeature(FD_I_IS_DEM_PRONOUN,inst.getAntecedent().getDemPronoun()); 
        inst.setFeature(FD_I_IS_REFL_PRONOUN, inst.getAntecedent().getReflPronoun()); 
        inst.setFeature(FD_I_IS_REL_PRONOUN, inst.getAntecedent().getRelPronoun()); 
        inst.setFeature(FD_I_IS_PERS_PRONOUN, inst.getAntecedent().getPersPronoun());          
        inst.setFeature(FD_I_IS_POSS_PRONOUN, inst.getAntecedent().getPossPronoun());          

    }
    
    
}
