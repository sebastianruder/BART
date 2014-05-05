/*
 * FE_MentionType_extra.java
 *
 * Created on August 10, 2007, 4:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.mentiontype_old;

import java.util.List;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

/**
 * Feature to determine type of anaphor and antecedent not superseded 
 * by MentionType_Anaphor and MentionType_Salience and not included
 * anywhere else
 * 
 * @author massimo
 */

public class FE_MentionType_extra implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_J_IS_DEFINITE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "anaIsDefinite");
    public static final FeatureDescription<Boolean> FD_I_IS_PRONOUN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "antIsPronoun");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_J_IS_DEFINITE);
        fds.add(FD_I_IS_PRONOUN);
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_J_IS_DEFINITE,inst.getAnaphor().getDefinite());
        inst.setFeature(FD_I_IS_PRONOUN, inst.getAntecedent().getPronoun());
    }
    
}
