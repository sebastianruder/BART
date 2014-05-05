/*
 * FeatureExtractor.java
 *
 * Created on July 10, 2007, 5:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

import java.util.List;

/** Interface used when implementing a feature extractor.
 * 
 */
public interface FeatureExtractor<T extends Instance> {
    /** Add feature descriptions for the features extracted by this
     *  FeatureExtractor to a list.
     */
    void describeFeatures(List<FeatureDescription> fds);
    /** actually enrich an instance with features, ie. call the setFeature method  */
    void extractFeatures(T inst);
}
