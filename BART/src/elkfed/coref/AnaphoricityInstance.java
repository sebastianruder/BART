/*
 * AnaphoricityInstance.java
 *
 * Created on August 4, 2007, 3:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref;

import elkfed.coref.mentions.Mention;
import elkfed.ml.AbstractInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

/**
 *
 * @author yannick
 */
public class AnaphoricityInstance extends AbstractInstance {
    public static FeatureDescription<Boolean> FD_BIAS_NONE=new FeatureDescription<Boolean>(
            FeatureType.FT_BOOL, "non-anaphoricity bias");
    Mention _m;
    
    /** Creates a new instance of AnaphoricityInstance */
    public AnaphoricityInstance(Mention m) {
        _m=m;
    }
    
    public Mention getMention()
    { return _m; }
}
