/*
 * FE_MentionType_Salience.java
 *
 * Created on August 3, 2007, 11:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.mentiontype_old;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 * More detailed features to determine type of anaphor (J) and antecedent (I)
 *
 * @author massimo
 */
public class FE_MentionType_Salience implements PairFeatureExtractor {
    
//    Keep in mind that I is ante, J is antecedent
//    Anaphora type
    public static final FeatureDescription<Boolean> FD_J_IS_PN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPN");
    public static final FeatureDescription<Boolean> FD_J_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDefinite");   
    public static final FeatureDescription<Boolean> FD_J_IS_DEMONSTRATIVE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemonstrative");
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_NOMINAL=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemNominal");
    public static final FeatureDescription<Boolean> FD_J_IS_DEM_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDemPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_REFL_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsReflPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_PERS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPersPronoun");
    public static final FeatureDescription<Boolean> FD_J_IS_POSS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsPossPronoun");
//    Ante type 
        // two extra features for 2.9b
    //public static final FeatureDescription<Boolean> FD_I_IS_PN=
    //        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsPN");
    //public static final FeatureDescription<Boolean> FD_I_IS_DEFINITE=
    //        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsDefinite");
    public static final FeatureDescription<Boolean> FD_I_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsPronoun");
//    Keep around odd Soon et al feature just in case
    public static final FeatureDescription<Boolean> FD_ARE_PROPERNAMES=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "areProperName");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_PN);
        fds.add(FD_J_IS_DEFINITE);
        fds.add(FD_J_IS_DEMONSTRATIVE);
        fds.add(FD_J_IS_DEM_NOMINAL);
        fds.add(FD_J_IS_DEM_PRONOUN);
        fds.add(FD_J_IS_PRONOUN);
        fds.add(FD_J_IS_REFL_PRONOUN);
        fds.add(FD_J_IS_PERS_PRONOUN);
        fds.add(FD_J_IS_POSS_PRONOUN);
        // two extra features for 2.9b
        //fds.add(FD_I_IS_PN);
        //fds.add(FD_I_IS_DEFINITE);
        fds.add(FD_I_IS_PRONOUN);
        fds.add(FD_ARE_PROPERNAMES);
    }
    
    /*  Extract features from mention and stores them in instance */
    public void extractFeatures(PairInstance inst) {
        // anaphor
        inst.setFeature(FD_J_IS_PN,inst.getAnaphor().getProperName());
        inst.setFeature(FD_J_IS_DEFINITE,inst.getAnaphor().getDefinite());
        inst.setFeature(FD_J_IS_DEMONSTRATIVE,inst.getAnaphor().getDemonstrative()); 
        inst.setFeature(FD_J_IS_DEM_NOMINAL,inst.getAnaphor().getDemNominal()); 
        inst.setFeature(FD_J_IS_DEM_PRONOUN,inst.getAnaphor().getDemPronoun()); 
        inst.setFeature(FD_J_IS_PRONOUN, inst.getAnaphor().getPronoun()); 
        inst.setFeature(FD_J_IS_REFL_PRONOUN, inst.getAnaphor().getReflPronoun()); 
        inst.setFeature(FD_J_IS_PERS_PRONOUN, inst.getAnaphor().getPersPronoun());          
        inst.setFeature(FD_J_IS_POSS_PRONOUN, inst.getAnaphor().getPossPronoun());          
        // ante
        // two extra features for 2.9b
        //inst.setFeature(FD_I_IS_PN,inst.getAntecedent().getProperName());
        //inst.setFeature(FD_I_IS_DEFINITE, inst.getAntecedent().getDefinite());
        inst.setFeature(FD_I_IS_PRONOUN, inst.getAntecedent().getPronoun());
        inst.setFeature(FD_ARE_PROPERNAMES, areBothProperNames(inst));
    }
    
    private Boolean areBothProperNames(PairInstance inst)
    {
        return (
                inst.getAntecedent().getProperName()
             &&
                inst.getAnaphor().getProperName()
        );
    }
    
}
