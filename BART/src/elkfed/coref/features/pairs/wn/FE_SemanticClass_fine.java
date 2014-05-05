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
 * Feature used to determine Semantic Class relatedness of a pair instance.
 * Either True/False or Unknown.
 *
 * @author Vlad
 */
public class FE_SemanticClass_fine  implements PairFeatureExtractor{
    
    
    public static final FeatureDescription<TriValued> FD_IS_SEMANTIC_CLASS=
        new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "SemanticCompatibilty");
    
    /** Creates a new instance of FE_SemanticClass */
    public FE_SemanticClass_fine() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
	        fds.add(FD_IS_SEMANTIC_CLASS);
	        
	     }
 
     public void extractFeatures(PairInstance inst) {
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
	             {
                     inst.setFeature(FD_IS_SEMANTIC_CLASS,TriValued.TRUE);
                 } else {
                     inst.setFeature(FD_IS_SEMANTIC_CLASS,TriValued.UNKNOWN);
                 }
	         } else {
	         /*  else the semantic classes of markables i and j are in agreement if
	             one is the parent of the other (e.g., chairman with semantic class
	             "person" and Mr. Lim with semantic class "male"), or they are the
	             same (e.g., Mr. Lim and he, both of semantic class "male"). The
	             value returned for such cases is true. If the semantic classes of
	             i and j are not the same (e.g., IBM with semantic class
	             "organization" and Mr. Lim with semantic class "male"), return false.
	          */
              SemanticClass ante_semcls=inst.getAntecedent().getSemanticClass();
              SemanticClass ana_semcls=inst.getAnaphor().getSemanticClass();
	          if (SemanticClass.isaPerson(ante_semcls) &&
    	          SemanticClass.isaPerson(ana_semcls) ||
                  SemanticClass.isaNumeric(ante_semcls) &&
                  SemanticClass.isaNumeric(ana_semcls) ||
                  ante_semcls==ana_semcls) {
                inst.setFeature(FD_IS_SEMANTIC_CLASS,TriValued.TRUE);
              } else {
                  inst.setFeature(FD_IS_SEMANTIC_CLASS,TriValued.FALSE);
              }
	     }
     }
}
