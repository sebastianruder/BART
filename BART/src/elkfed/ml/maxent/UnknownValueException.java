/*
 * UnknownValueException.java
 *
 * Created on 27. September 2007, 10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml.maxent;

import elkfed.ml.FeatureDescription;

/**
 *
 * @author versley
 */
public class UnknownValueException extends Exception {
    protected FeatureDescription _fd;
    public UnknownValueException(FeatureDescription fd)
    {
        _fd=fd;
    }
    public String toString() {
        return String.format("Feature has no value: %s",_fd);
    }
}
