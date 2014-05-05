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
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.ml.TriValued;

/**
 *  this just mirrors wn/FE_SemanticClass
 * @author olga
 */
public class FE_AnimacyAgree implements PairFeatureExtractor {
    public static final FeatureDescription<TriValued> FD_IS_ANIMACY=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM,
                TriValued.class, "Animacy_Agree");
    
    /** Creates a new instance of FE_SemClass */
    public FE_AnimacyAgree() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_ANIMACY);
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_ANIMACY, FE_SemanticClass.getSemClass(inst));                
    }
    
}
