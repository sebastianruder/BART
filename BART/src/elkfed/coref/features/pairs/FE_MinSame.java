/*
 * FE_HeadMatch.java
 *
 * Created on July 12, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.mmax.minidisc.Markable;

/**
 Feature used to compare the heads (ids, not strings! -- for overlapping marks) of the instance pairs NPs. Either T/F
 * @author olga
 */
public class FE_MinSame implements PairFeatureExtractor {
    

     public static final FeatureDescription<Boolean> FD_IS_MINSAME=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "MinSame");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_MINSAME);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_MINSAME,getsamemin(inst));
    }
    
    private boolean getsamemin(PairInstance inst)
    {   

       Markable mante=inst.getAntecedent().getMarkable();
       Markable mana=inst.getAnaphor().getMarkable();
//System.err.println("min_ante= " + mante.getAttributeValue("min_ids", null));
// System.err.println("min_ana= " + mana.getAttributeValue("min_ids", null));
       if ((mante.getAttributeValue("min_ids", null) != null) &&
           (mana.getAttributeValue("min_ids", null).equals( mante.getAttributeValue("min_ids", null)))) return true;
       return false;   
    }
}
