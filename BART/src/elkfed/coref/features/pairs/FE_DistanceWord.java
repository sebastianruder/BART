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
import static elkfed.mmax.MarkableLevels.DEFAULT_MARKABLE_LEVEL;

/**
 *
 * @author vae2101
 */
public class FE_DistanceWord implements PairFeatureExtractor {

    public static final FeatureDescription<Double> FD_WORDDIST =
            new FeatureDescription<Double>(FeatureType.FT_SCALAR, "WordDistance");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_WORDDIST);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_WORDDIST,getWordDist(inst));
    }

    public Double getWordDist(PairInstance inst) {
        Markable anaphor = inst.getAnaphor().getMarkable();
        Markable antecedent = inst.getAntecedent().getMarkable();

        Markable finalantecedent = antecedent;
        
        List<Markable> markables = inst.getAntecedent().getDocument().getMarkableLevelByName(DEFAULT_MARKABLE_LEVEL).getMarkables();

        for (Markable markable : markables) {
            if (    !markable.equals(antecedent) &&
                    markable.getIntID() < anaphor.getIntID() &&
                    !embeds(markable, anaphor) &&
                    embeds(markable, antecedent)) {
                finalantecedent = markable;
            }
        }

//        return anaphor.getLeftmostDiscoursePosition() - finalantecedent.getRightmostDiscoursePosition();
        int distance = anaphor.getLeftmostDiscoursePosition() - finalantecedent.getLeftmostDiscoursePosition();
        return Math.log(distance);
    }

    public boolean embeds(Markable m1, Markable m2) {
        return m1.getLeftmostDiscoursePosition() <= m2.getLeftmostDiscoursePosition() &&
                m1.getRightmostDiscoursePosition() >= m2.getRightmostDiscoursePosition();
    }

}
