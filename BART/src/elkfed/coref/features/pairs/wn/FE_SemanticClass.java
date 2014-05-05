/*
 * FE_SemanticClass.java
 *
 * Created on July 18, 2007, 6:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.wn;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.TriValued;
import java.util.List;
import elkfed.knowledge.SemanticClass;

/**
 * Feature used to determine Semantic Class relatedness of a pair instance. Either True/False or Unknown.
 *
 * @author Vlad
 */
public class FE_SemanticClass  implements PairFeatureExtractor{
    
    
    public static final FeatureDescription<TriValued> FD_IS_SEMANTIC_CLASS=
        new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "SemanticCompatibilty");
    
    /** Creates a new instance of FE_SemanticClass */
    public FE_SemanticClass() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
	        fds.add(FD_IS_SEMANTIC_CLASS);
	        
	     }
 
     public void extractFeatures(PairInstance inst) {
inst.setFeature(FD_IS_SEMANTIC_CLASS,getSemClass(inst));
}
public static TriValued getSemClass(PairInstance inst) {

	    	 // if either semantic class is "unknown"...
	         
	    	
	    	 if (   inst.getAntecedent().getSemanticClass().equals(SemanticClass.UNKNOWN)
	             ||
	                 inst.getAnaphor().getSemanticClass().equals(SemanticClass.UNKNOWN)
	            )
	         {
	             // ... then the head noun strings of both markables are compared.
	             // If they are the same, return true; else return unknown.
                 if (inst.getAntecedent().getHeadOrName().equalsIgnoreCase(
                         inst.getAnaphor().getHeadOrName()))
	             return TriValued.TRUE;
                     
	         else
	             return TriValued.UNKNOWN;

	         }
	         /*  else the semantic classes of markables i and j are in agreement if
	             one is the parent of the other (e.g., chairman with semantic class
	             "person" and Mr. Lim with semantic class "male"), or they are the
	             same (e.g., Mr. Lim and he, both of semantic class "male"). The
	             value returned for such cases is true. If the semantic classes of
	             i and j are not the same (e.g., IBM with semantic class
	             "organization" and Mr. Lim with semantic class "male"), return false.
	          */
	         else if (
	                  (
	                     SemanticClass.isaPerson(inst.getAntecedent().getSemanticClass())
	                    &&
	                     SemanticClass.isaPerson(inst.getAnaphor().getSemanticClass())
	                 )
	                ||
	                 (
	                     SemanticClass.isaObject(inst.getAntecedent().getSemanticClass())
	                    &&
	                     SemanticClass.isaObject(inst.getAnaphor().getSemanticClass())
	                 )
	             )
	          return TriValued.TRUE; 
	         else
	          return TriValued.FALSE;
	    	 
	     }
    
}
