/*

 */

package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.lang.GermanLinguisticConstants;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;


/**
 * Feature that returns the pronoun word form of the anaphor
 * @author samuel
 */
public class FE_PronounWordForm implements PairFeatureExtractor{
    
    public static final FeatureDescription<String> FD_IS_PRONWORDFORM=
            new FeatureDescription<String>(FeatureType.FT_STRING, String.class, "PronounWordForm");
    
    
 
    @Override
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_PRONWORDFORM);
    }

    @Override
    public void extractFeatures(PairInstance inst) {
        if (inst.getAnaphor().getPronoun()) {
            String pronoun = inst.getAnaphor().getHeadString().toLowerCase();
            if(pronoun.equals(GermanLinguisticConstants.SAXON_GENITIVE)) {
                inst.setFeature(FD_IS_PRONWORDFORM, "SAXON_GENETIV");
            } else {
                inst.setFeature(FD_IS_PRONWORDFORM, pronoun);
            }
        } else {
            inst.setFeature(FD_IS_PRONWORDFORM, "*NULL*");
        }
    }
}
