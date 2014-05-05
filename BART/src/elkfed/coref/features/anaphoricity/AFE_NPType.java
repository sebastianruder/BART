/*
 * AFE_NPType.java
 *
 * Created on August 4, 2007, 3:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.anaphoricity;

import elkfed.coref.AnaphoricityInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class AFE_NPType implements FeatureExtractor<AnaphoricityInstance>
{
    public static final FeatureDescription<Boolean> FD_IS_DEFINITE=
            new FeatureDescription(FeatureType.FT_BOOL, "AFE_isDefinite");
    public static final FeatureDescription<Boolean> FD_IS_NAME=
            new FeatureDescription(FeatureType.FT_BOOL, "AFE_isName");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_DEFINITE);
        fds.add(FD_IS_NAME);
    }

    public void extractFeatures(AnaphoricityInstance inst) {
        inst.setFeature(FD_IS_DEFINITE,
                inst.getMention().getDefinite());
       inst.setFeature(FD_IS_NAME,
                inst.getMention().getProperName());
    }

}
