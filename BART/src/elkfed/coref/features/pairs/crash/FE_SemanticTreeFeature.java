/*
 * FE_SemanticTreeFeature.java
 *
 * Created on August 17, 2007, 12:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.crash;

import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.knowledge.SemanticTreeFeature;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/** Extracting semantic trees from discourse entities
 *
 * @author massimo
 */
public class FE_SemanticTreeFeature implements PairFeatureExtractor {
    
    /** Creates a new instance of FE_SemanticTreeFeature */
    //public FE_SemanticTreeFeature() {
    //}
    
    public static final FeatureDescription<String> FD_SEM_TREE_FILECARD_ANA=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Entire_Sem_Tree_Ana");
    public static final FeatureDescription<String> FD_SEM_TREE_FILECARD_ANTE=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Entire_Sem_Tree_Ante");
    
    static SemanticTreeFeature _semFeature=new SemanticTreeFeature();
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SEM_TREE_FILECARD_ANA);   
        fds.add(FD_SEM_TREE_FILECARD_ANTE);
    }
    
    public void extractFeatures(PairInstance inst) {
        Mention ana=inst.getAnaphor();     // NB these should be extracted from 
        Mention ante=inst.getAntecedent();  // Coref chains
        if (ana.getDiscourseEntity()==null || ante.getDiscourseEntity()==null)
        {   
            return; 
        }
        else
        {
            String anteSemFeature =  _semFeature.GetSemanticFeature(ante);
            String anaSemFeature  =  _semFeature.GetSemanticFeature(ana);
            
            inst.setFeature(FD_SEM_TREE_FILECARD_ANA, anteSemFeature);
            inst.setFeature(FD_SEM_TREE_FILECARD_ANTE, anaSemFeature);
        }
    }
}
