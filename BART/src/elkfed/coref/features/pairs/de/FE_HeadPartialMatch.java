/*
 * FE_HeadPartialMatch.java
 */
package elkfed.coref.features.pairs.de;

import elkfed.coref.*;
import elkfed.lang.util.de.appositions.HeadMatcher;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
Feature used to partially compare the head strings of the instance pairs NPs. Either T/F
 * @author samuel
 */
public class FE_HeadPartialMatch extends HeadMatcher implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_HEADPARTIAL_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "HeadPartialMatch");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_HEADPARTIAL_MATCH);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_HEADPARTIAL_MATCH, getHeadMatch(inst, new PartialMatcher()));
    }

    public class PartialMatcher extends BooleanMatcher {

        @Override
        public boolean match(String string1, String string2) {
//            System.out.println("MATCHING " + string1 + " with "  + string2);
            if (string1.contains(string2) ||
                    string2.contains(string1)) {
                return true;
            } else {
                return false;
            }
        }
    }

}
