/*
 * CorefTrainer.java
 *
 * Created on July 18, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref;

import elkfed.coref.mentions.Mention;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author yannick
 */
public interface CorefTrainer {
    void encodeDocument(List<Mention> mentions) throws IOException;

}
