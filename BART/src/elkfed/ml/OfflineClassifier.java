/*
 * OfflineClassifier.java
 *
 * Created on July 12, 2007, 5:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

import java.util.List;

/**
 *
 * @author versley
 */
@SuppressWarnings("unchecked")
public interface OfflineClassifier {
    /** used to match features to classifier features */
    void setHeader(List<FeatureDescription> fds);
    /** classify a bunch of instances */
    void classify(List<? extends Instance> problems, List output);
    void classify(List<? extends Instance> problems, List output,
            List<Double> confidence);    
}
