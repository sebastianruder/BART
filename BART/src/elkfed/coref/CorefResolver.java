/*
 * CorefResolver.java
 *
 * Created on July 16, 2007, 5:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref;

import elkfed.coref.mentions.Mention;
import java.util.List;
import java.util.Map;
import net.cscott.jutil.DisjointSet;

/** Common interface for any coreference resolver.
 *
 * @author versley
 */
public interface CorefResolver {
       public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
               Map<Mention,Mention> antecedents);
       public void printStatistics();
}
