/*
 * InstanceWriter.java
 *
 * Created on July 10, 2007, 5:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;
import java.io.IOException;
import java.util.List;

/** Takes instances and writes them to a data file
 *  that can be used for training.
 */
@SuppressWarnings("unchecked")
public interface InstanceWriter {
    /** sets up the information that describes the features
     *  that will be used
     */
	void setHeader(List<FeatureDescription> fds) throws IOException;
    /** writes an instance to the file */
    void write(Instance inst) throws IOException;
    /** close file(s) and do cleanup */
    void close() throws IOException;
    /** flush file(s) */
    void flush() throws IOException;
}
