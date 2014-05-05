/*
 * FE_NameStructure.java
 *
 * Created on August 15, 2007, 3:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.nlp.util.NameStructure;
import java.util.List;
import elkfed.ml.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class finds internal structure in names. It is adapted from a
 * Perl script found at
 *
 * http://www.cs.utah.edu/~hal/HAPNIS/
 *
 *
 * @author ajern
 */
public class FE_NameStructure implements PairFeatureExtractor {
    
    private String nameTags[] = {"Role", "Forename", "Middle", "Link", "Surname", "Suffix"};
    
    public static final FeatureDescription<String> FD_NAME_STRUCT=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, "NameStructure");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_NAME_STRUCT);
    }
    
    
    public void extractFeatures(PairInstance inst) {
        String f = "(nametree";

        HashMap<String,String> nameStructure1 = (new NameStructure()).getNameStructure(inst.getAnaphor().getMarkableString());
        HashMap<String,String> nameStructure2 = (new NameStructure()).getNameStructure(inst.getAntecedent().getMarkableString());
        
        // Add the features
        for (String tag : nameTags) {
            if (!nameStructure1.containsKey(tag) || !nameStructure2.containsKey(tag))
                f += " (" + tag + " (0.0))";
            else  {
                f += " (" + tag + " (" +
                        (new FE_StringKernel()).getSK(nameStructure1.get(tag),nameStructure2.get(tag)) +
                        "))";
            }
            
        }
        f += ")";
        inst.setFeature(FD_NAME_STRUCT,f);

    }
}
