/*
 * AFE_Postmod.java
 *
 * Created on August 4, 2007, 3:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.anaphoricity;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.AnaphoricityInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class AFE_Postmod implements FeatureExtractor<AnaphoricityInstance>
{
    public static final FeatureDescription<Boolean> FD_HAS_POSTMOD=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "has_postmodifier");
    public static final FeatureDescription<String> FD_POSTMOD=
            new FeatureDescription<String>(FeatureType.FT_STRING, "postmodifier");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_POSTMOD);
    }

    public void extractFeatures(AnaphoricityInstance inst) {
        List<Tree> postmods=inst.getMention().getPostmodifiers();
        if (postmods==null)
        {
            inst.setFeature(FD_POSTMOD,"UNPARSED");
            return;
        }
        inst.setFeature(FD_HAS_POSTMOD,!postmods.isEmpty());
        if (!postmods.isEmpty())
        {
            inst.setFeature(FD_POSTMOD,
                    postmods.get(0).value());
        }
    }    
}
