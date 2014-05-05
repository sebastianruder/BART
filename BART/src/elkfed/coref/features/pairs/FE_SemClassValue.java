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
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class FE_SemClassValue implements PairFeatureExtractor {
    public static final FeatureDescription<SemanticClass> FD_ANTE_SEMCLASS=
            new FeatureDescription<SemanticClass>(FeatureType.FT_NOMINAL_ENUM,
                SemanticClass.class, "Ante_Semclass");
    public static final FeatureDescription<SemanticClass> FD_ANA_SEMCLASS=
            new FeatureDescription<SemanticClass>(FeatureType.FT_NOMINAL_ENUM,
                SemanticClass.class, "Ana_Semclass");
    public static final FeatureDescription<String> FD_SEMCLASS_PAIR=
            new FeatureDescription<String>(FeatureType.FT_STRING,"Semclass_Pair");
    
    /** Creates a new instance of FE_SemClass */
    public FE_SemClassValue() {
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_ANTE_SEMCLASS);
        fds.add(FD_ANA_SEMCLASS);
        fds.add(FD_SEMCLASS_PAIR);
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_ANTE_SEMCLASS, inst.getAntecedent().getSemanticClass());
        inst.setFeature(FD_ANA_SEMCLASS, inst.getAnaphor().getSemanticClass());
        inst.setFeature(FD_SEMCLASS_PAIR, String.format("%s-%s",
                inst.getAnaphor().getSemanticClass(),
                inst.getAntecedent().getSemanticClass()));
                
    }
    
}
