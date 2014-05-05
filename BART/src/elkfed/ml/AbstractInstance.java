/*
 * AbstractInstance.java
 *
 * Created on July 9, 2007, 6:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author versley
 */
public abstract class AbstractInstance implements Instance {
   
	protected Map<FeatureDescription<?>,Object> _featureVals;
   
	public AbstractInstance()
	{
		_featureVals=new HashMap<FeatureDescription<?>, Object>();
	}
   
    @SuppressWarnings("unchecked")
	public <T> T getFeature(FeatureDescription<T> descr)
    {
        return (T)_featureVals.get(descr);
    }
    public <T> void setFeature(FeatureDescription<T> descr, T value)
    {
        _featureVals.put(descr,value);
    }
    
    public String getDebugInfo()
    {
        return null;
    }
}
