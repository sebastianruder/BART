/*
 * FE_SentenceDistance.java
 *
 * Created on July 12, 2007, 5:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *
 * @author samuel
 */
public class FE_DistanceSentence implements PairFeatureExtractor {

    public static final FeatureDescription<BigDecimal> FD_SENTDIST =
            new FeatureDescription<BigDecimal>(FeatureType.FT_SCALAR, "SentenceDistance");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SENTDIST);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_SENTDIST, getSentDist(inst));
    }

    public BigDecimal getSentDist(PairInstance inst) {
        return new BigDecimal(Math.log(1 + inst.getAnaphor().getSentId() - inst.getAntecedent().getSentId())).setScale(1, RoundingMode.UP);
    }
}
