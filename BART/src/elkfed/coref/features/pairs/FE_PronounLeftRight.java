/*

 */

package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;


/**
 * Feature that returns if the anaphor is a pronoun (RIGHT) xor the ante is a pronoun (LEFT) xor NONE
 * @author samuel
 */
public class FE_PronounLeftRight implements PairFeatureExtractor{

    public enum PronounPosition
    {
    LEFT,RIGHT,NONE;
    }

    public static final FeatureDescription<PronounPosition> FD_IS_PRONLEFTRIGHT=
            new FeatureDescription<PronounPosition>(FeatureType.FT_NOMINAL_ENUM, PronounPosition.class, "PronounLeftRight");
 
    @Override
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_PRONLEFTRIGHT);
    }

    @Override
    public void extractFeatures(PairInstance inst) {
        if (inst.getAnaphor().getPronoun()) {
            inst.setFeature(FD_IS_PRONLEFTRIGHT, PronounPosition.RIGHT);
        } else if (inst.getAntecedent().getPronoun()) {
            inst.setFeature(FD_IS_PRONLEFTRIGHT, PronounPosition.LEFT);
        } else {
            inst.setFeature(FD_IS_PRONLEFTRIGHT, PronounPosition.NONE);
        }
    }
}
