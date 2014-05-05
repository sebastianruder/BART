/*
 * FE_CoRef.java
 *
 * Created on July 17, 2007, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;
import elkfed.coref.*;
import java.util.List;
import elkfed.ml.*;


/**
 * Feature used to identify in training whether two mentions are coreferent with each other. Either T/F
 * @author vae2101
 */
public class FE_CoRef implements PairFeatureExtractor {
 
     public static final FeatureDescription<Boolean> FD_IS_COREF=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "CoreferentPair");
     
     public void describeFeatures(List<FeatureDescription> fds) {
         fds.add(FD_IS_COREF);     
     }

     
      public void extractFeatures(PairInstance inst) {             
         inst.setFeature(FD_IS_COREF,inst.getAnaphor().isCoreferent(inst.getAntecedent()));
      }
     

    
}
