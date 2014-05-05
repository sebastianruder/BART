/*
 * FE_HeadMatch.java
 */
package elkfed.coref.features.pairs.de;

import elkfed.coref.*;
import elkfed.lang.util.de.appositions.HeadMatcher;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
Feature used to compare the head strings of the instance pairs NPs. Either T/F
 * @author samuel
 */
public class FE_HeadMatch extends HeadMatcher implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_HEADMATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "HeadMatch");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_HEADMATCH);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_HEADMATCH, getHeadMatch(inst, new CompleteMatcher()));
    }

    public class CompleteMatcher extends BooleanMatcher {
        @Override
        public boolean match(String string1, String string2) {
//            System.out.println("MATCHING " + string1 + " with "  + string2);
            return string1.equalsIgnoreCase(string2);
        }
    }
}
