/*
 * FE_SemClass.java
 *
 * Created on August 20, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.nlp.util.Gender;

import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.ml.TriValued;
import elkfed.coref.mentions.Mention;

/**
 *  hierarchical semclass agreement (loc-loc, loc-obj, not loc-org)
 * for pairs of enamex, uses enamextype, not wn!
 * @author olga
 */
public class FE_SemClassAgree implements PairFeatureExtractor {
    public static final FeatureDescription<TriValued> FD_IS_SEMCLASSAGREE=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM,
                TriValued.class, "SemClass_Agree");
    
    /** Creates a new instance of FE_SemClass */
    public FE_SemClassAgree() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_SEMCLASSAGREE);
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_SEMCLASSAGREE, compareSemClassHierarchy(inst));                
    }
    
    public static TriValued compareSemClassHierarchy(PairInstance inst) {

      if (inst.getAntecedent().isEnamex() && inst.getAnaphor().isEnamex()) 
         return compareSemClassHierarchyNENE(inst.getAnaphor(),inst.getAntecedent());
      if (inst.getAntecedent().isEnamex())
         return compareSemClassHierarchyNPNE(inst.getAnaphor(),inst.getAntecedent());
      if (inst.getAnaphor().isEnamex())
         return compareSemClassHierarchyNPNE(inst.getAntecedent(),inst.getAnaphor());
      return compareSemClassHierarchyNPNP(inst.getAnaphor(),inst.getAntecedent());

//ToDo: check what happens to pronouns (esp. "it", "they")

}    
    private static TriValued compareSemClassHierarchyNPNE(Mention np, Mention ne) {
// compare semclass hierarchically when the first mention is non-ne and the second is ne
       if (np.getSemanticClass().equals(SemanticClass.UNKNOWN)){
           if (np.getHeadOrName().equalsIgnoreCase(
               ne.getHeadOrName()))
             return TriValued.TRUE; // this shouldn't probably happen
           return TriValued.UNKNOWN;
       }

// if NE is a person

       if (ne.getEnamexType().toLowerCase().startsWith("per")) {
         if (!SemanticClass.isaPerson(np.getSemanticClass())) 
           return TriValued.FALSE;
         if (np.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
         if (ne.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
         if (np.getGender().equals(ne.getGender()))
           return TriValued.TRUE;
         return TriValued.FALSE;
       }

// if NE is not a person
         if (!SemanticClass.isaObject(np.getSemanticClass())) 
           return TriValued.FALSE;
         if (np.getSemanticClass().equals(SemanticClass.OBJECT)) return TriValued.TRUE;
         if (np.getSemanticClass().equals(SemanticClass.ORGANIZATION)) {
           if (ne.getEnamexType().toLowerCase().startsWith("org")) 
             return TriValued.TRUE;
           return TriValued.FALSE;
         }
         if (np.getSemanticClass().equals(SemanticClass.LOCATION)) {
           if (ne.getEnamexType().toLowerCase().startsWith("loc")) 
             return TriValued.TRUE;
           if (ne.getEnamexType().toLowerCase().startsWith("gpe")) 
             return TriValued.TRUE;
           return TriValued.FALSE;
         }
         return TriValued.FALSE;

   }
    private static TriValued compareSemClassHierarchyNPNP(Mention np1, Mention np2) {
// compare semclass hierarchically when both mentions are nps
       if (np1.getSemanticClass().equals(SemanticClass.UNKNOWN) ||
           np2.getSemanticClass().equals(SemanticClass.UNKNOWN)) {
           if (np1.getHeadOrName().equalsIgnoreCase(
               np2.getHeadOrName()))
             return TriValued.TRUE;
           return TriValued.UNKNOWN;
       }

       if (SemanticClass.isaPerson(np1.getSemanticClass()) && 
           SemanticClass.isaPerson(np2.getSemanticClass())) {

         if (np1.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
         if (np2.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
         if (np1.getGender().equals(np2.getGender()))
           return TriValued.TRUE;
         return TriValued.FALSE;
       }
       if (SemanticClass.isaObject(np1.getSemanticClass()) && 
           SemanticClass.isaObject(np2.getSemanticClass())) {
         if (np1.getSemanticClass().equals(SemanticClass.OBJECT)) return TriValued.TRUE;
         if (np2.getSemanticClass().equals(SemanticClass.OBJECT)) return TriValued.TRUE;
         if (np1.getSemanticClass().equals(np2.getSemanticClass())) return TriValued.TRUE;
         return TriValued.FALSE;
       }
       return TriValued.FALSE;
   }



    private static TriValued compareSemClassHierarchyNENE(Mention ne1, Mention ne2) {
// compare semclass hierarchically when both mentions are NE

//for loc/gpe -- allow loc-gpe match, otherwise no enamextype mismatch allowed

    if (!ne1.getEnamexType().equalsIgnoreCase(ne2.getEnamexType())) {

    if (ne1.getEnamexType().toLowerCase().startsWith("loc") &&
        ne2.getEnamexType().toLowerCase().startsWith("gpe")) 
           return TriValued.TRUE;
    if (ne2.getEnamexType().toLowerCase().startsWith("loc") &&
        ne1.getEnamexType().toLowerCase().startsWith("gpe")) 
           return TriValued.TRUE;
      return TriValued.FALSE;
     }

// for person -- check gender
    if (ne2.getEnamexType().toLowerCase().startsWith("per")) {
      if (ne1.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
      if (ne2.getGender().equals(Gender.UNKNOWN)) return TriValued.UNKNOWN;
      if (ne1.getGender().equals(ne2.getGender()))
         return TriValued.TRUE;
      return TriValued.FALSE;
    }
    return TriValued.TRUE;


   }
}
