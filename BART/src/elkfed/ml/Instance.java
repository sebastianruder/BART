/*
 * Instance.java
 *
 * Created on July 9, 2007, 5:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

import elkfed.ml.FeatureDescription;

/**An Instance represents a generic classification instance for ML learning.
 *
 */
public interface Instance {
    <T> T getFeature(FeatureDescription<T> descr);
    <T> void setFeature(FeatureDescription<T> descr, T value);
    String getDebugInfo();
}
