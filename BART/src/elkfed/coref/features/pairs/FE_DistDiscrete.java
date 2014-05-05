/*
 * FE_DistDiscrete.java
 *
 * Created on July 30, 2007, 6:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class FE_DistDiscrete implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_DIST_SAME=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Dist_Same");
    public static final FeatureDescription<Boolean> FD_DIST_LAST=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Dist_Last");
 
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DIST_SAME);
	fds.add(FD_DIST_LAST);
    }

    public void extractFeatures(PairInstance inst) {
        int dist=inst.getAnaphor().getSentId() -
                inst.getAntecedent().getSentId();
        inst.setFeature(FD_DIST_SAME,dist==0);
	inst.setFeature(FD_DIST_LAST,dist==1);
    }
}
