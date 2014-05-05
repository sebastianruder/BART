/*
 * SemanticTreeFeature.java
 *
 * Created on August 17, 2007, 12:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

import elkfed.coref.mentions.Mention;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.discourse_entities.Property;
import java.util.Iterator;
import java.util.Set;


/** Extract semantic trees out of discourse entities
 *
 * @author massimo
 */
public class SemanticTreeFeature {

    public String GetSemanticFeature(Mention m) {
                                /* This method outputs a 'semantic tree' of form
                                 * (DE 
                                 *      (TYPE X) .... (TYPE Y) 
                                 *      (ATTR Z) ...  (ATTR W)
                                 *      (REL  R1) .... (REL R2)
                                 */
        DiscourseEntity de = m.getDiscourseEntity();
        StringBuffer textBuffer = new StringBuffer();
        
        
        Set<Property> de_types = de.getTypes();
        Set<Property> de_attrs = de.getAttributes();
        Set<Property> de_rels  = de.getRelations();
        
        if (de_types.isEmpty() && de_attrs.isEmpty() && de_rels.isEmpty()) {
            // discourse entity for pronoun
            textBuffer.append("(DE  (TYPE ANYTHING))");
        }  else  {
            // discourse entity for nominal
            textBuffer.append("(DE ");
            // Add the head
            for (Iterator type_it = de_types.iterator(); type_it.hasNext(); ) {
                Property  de_type = (Property) type_it.next();
                textBuffer.append(" (TYPE "+de_type.getPredicate()+")");
            }
            // Add  the attributes
            for (Iterator attr_it = de_attrs.iterator(); attr_it.hasNext(); ) {
                Property  de_attr = (Property) attr_it.next();
                textBuffer.append(" (ATTR "+de_attr.getPredicate()+")");
            }
            // Add  the relations
            for (Iterator rel_it = de_rels.iterator(); rel_it.hasNext(); ) {
                Property  de_rel = (Property) rel_it.next();
                textBuffer.append(" (RELATION "
                                  +"("+de_rel.getPredicate()+")"
                                  +" "
                                  +"("+de_rel.getArgument()+")"
                                  +")");
            }
            textBuffer.append(")");
        } 
        //DEBUG
        System.out.println(textBuffer.toString());        
        return textBuffer.toString();   
    }
}