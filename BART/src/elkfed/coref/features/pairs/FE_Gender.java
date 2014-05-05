/*
 * FE_Gender.java
 *
 * Created on July 11, 2007, 6:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.mentions.Mention;
import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.TriValued;
import elkfed.nlp.util.Gender;
import java.util.List;


/**
 * Feature used to determine whether gender of the pair instance matches. Either T/F/Uknown
 * @author vae2101
 */
public class FE_Gender implements PairFeatureExtractor{
    
    public static final FeatureDescription<TriValued> FD_IS_GENDER=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM, TriValued.class, "Gender");
    
    
 
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_GENDER);
    }

    public void extractFeatures(PairInstance inst) {
       inst.setFeature(FD_IS_GENDER,getGender(inst));
    }


    public static TriValued getGender(PairInstance inst) {
        Mention m1=inst.getAntecedent();
        Mention m2=inst.getAnaphor();
        if (m1.getGender().equals(Gender.UNKNOWN) ||
            m2.getGender().equals(Gender.UNKNOWN))
        { 
           if (m1.getHeadOrName().equalsIgnoreCase(
               m2.getHeadOrName()))
             return TriValued.TRUE; 
          return TriValued.UNKNOWN;
        }

        // else check whether they match
        if (m1.getGender().equals(m2.getGender()))
          return TriValued.TRUE;
        return TriValued.FALSE;
    }
}
