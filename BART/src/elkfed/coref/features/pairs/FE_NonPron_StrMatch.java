/*
 * FE_NonPron_StrMatch.java
 *
 * Created on August 3, 2007, 2:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import static elkfed.lang.EnglishLinguisticConstants.*;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;


/**
 *
 * @author vae2101
 */
public class FE_NonPron_StrMatch implements PairFeatureExtractor {
 
    
        
    /** Creates a new instance of FE_NonPron_StrMatch */
    public FE_NonPron_StrMatch() {
    }

    
    public static final FeatureDescription<Boolean> FD_IS_NonPRO_STR=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Non_Pronominal_StringMatch");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_NonPRO_STR);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_NonPRO_STR, getNonPROStringMatch(inst));
    }
    
    public boolean getNonPROStringMatch(PairInstance inst)
    {   
       if (inst.getAntecedent().getPronoun()) return false;
       if (inst.getAnaphor().getPronoun()) return false;

       if (getMarkableString(inst.getAntecedent().getMarkable()).equals("")) 
         return false;

       if (getMarkableString(inst.getAntecedent().getMarkable()).
         equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable())))
                return true; 

       return false; 

    }
    
    /** 1. Removes the square brackets from a the Markable string
     *  2. Removes articles and demonstrative pronouns
     */
    protected String getMarkableString(final Markable markable)
    {
        final String[] tokens = markable.getDiscourseElements();
        final String[] pos =
            markable.getAttributeValue(DEFAULT_POS_LEVEL).split(" ");
        final StringBuffer clean = new StringBuffer();
        
        // if it's just one token there is nothing to remove
        // (e.g. demonstrative pronouns)
        if (tokens.length > 1)
        {
            for (int token = 0; token < tokens.length; token++)
            {
                if (        
                            !tokens[token].toLowerCase().matches(ARTICLE)
                        &&  
                            !tokens[token].toLowerCase().matches(DEMONSTRATIVE)
                        &&
                            !tokens[token].toLowerCase().matches(PUNCTUATION_MARK)
                        &&
                            !tokens[token].toLowerCase().matches(SAXON_GENITIVE)
                        &&
                            !pos[token].toLowerCase().matches(DETERMINER_POS)

                   )
                {  clean.append(" ").append(tokens[token]); }
            }
            try
            { return clean.deleteCharAt(0).toString(); }
            catch (StringIndexOutOfBoundsException e)
            {
                // insane exception handling... this is to take
                // of an expression such as "The A"... NLP sucks!
                return "";
            }
        }
        else
        { return tokens[0]; }
    }
}
    

