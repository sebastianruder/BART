/*
 * AFE_PronounString.java
 *
 * Created on August 10, 2007, 4:20 PM
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
public class AFE_PronounString implements FeatureExtractor<AnaphoricityInstance>
{
    public static final FeatureDescription<String> FD_PRO_STRING=
            new FeatureDescription(FeatureType.FT_STRING, "AFE_PronString");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_PRO_STRING);
    }

    public void extractFeatures(AnaphoricityInstance inst) {
        if (inst.getMention().getPronoun())
        {
            inst.setFeature(FD_PRO_STRING,inst.getMention().getMarkableString());
        }
    }    
}
