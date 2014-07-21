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
import static elkfed.mmax.pipeline.MarkableCreator.SENTENCE_ID_ATTRIBUTE;

/**
 *
 * @author vae2101
 */
public class FE_SentenceDistance implements PairFeatureExtractor{
    
    public static final FeatureDescription<Integer> FD_SENTDIST=
        new FeatureDescription<Integer>(FeatureType.FT_SCALAR, "SentenceDistance");
    
    
 
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SENTDIST);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_SENTDIST,getSentDist(inst));
    }
    
     public static Integer getSentDist(PairInstance inst)
    {
        return //instance.setFeature(feature,
                getDistance(inst.getAntecedent().getMarkable(), inst.getAnaphor().getMarkable());
    }
    
    /** Computes the sentence distance among two markables */
    private static int getDistance(final Markable markable1, final Markable markable2)
    {
        final int distance1 = Integer.parseInt(
                markable1.getAttributeValue(SENTENCE_ID_ATTRIBUTE));
        final int distance2 = Integer.parseInt(
                markable2.getAttributeValue(SENTENCE_ID_ATTRIBUTE));
        return Math.abs((distance1-distance2));
    }
}
