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
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author samuel
 */
public class FE_DistanceMarkable implements PairFeatureExtractor {

    public static final FeatureDescription<BigDecimal> FD_MARKDIST =
            new FeatureDescription<BigDecimal>(FeatureType.FT_SCALAR, "MarkableDistance");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_MARKDIST);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_MARKDIST, getMarkDist(inst));
    }

    public BigDecimal getMarkDist(PairInstance inst) {
        Markable anaphor = inst.getAnaphor().getMarkable();
        Markable antecedent = inst.getAntecedent().getMarkable();

        List<Markable> markables = inst.getAntecedent().getDocument().getMarkableLevelByName("markable").getMarkables();

        int distance = 1;

        for (Markable markable : markables) {
            if (    markable.getIntID() < antecedent.getIntID() &&
                    !embeds(markable, anaphor) &&
                    embeds(markable, antecedent)) {
                distance++;
            } else if (markable.getIntID() > antecedent.getIntID() && markable.getIntID() < anaphor.getIntID()) {
                distance++;
            }
//FIXME: Uncomment if sure that markables are always in order of their id
//            if (markable.getIntID() >= anaphor.getIntID()) {
//                break;
//            }
        }
        return new BigDecimal(Math.log(distance)).setScale(1, RoundingMode.UP);
    }

    public boolean embeds(Markable m1, Markable m2) {
        return m1.getLeftmostDiscoursePosition() <= m2.getLeftmostDiscoursePosition() &&
                m1.getRightmostDiscoursePosition() >= m2.getRightmostDiscoursePosition();
    }
}
