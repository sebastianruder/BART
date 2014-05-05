/*
 * Copyright 2007 Project ELERFED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elkfed.coref.features.pairs;


import elkfed.config.ConfigProperties;
import elkfed.coref.*;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.NodeCategory;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 *
 * @author vae2101
 */
public class FE_StringMatch implements PairFeatureExtractor {
    
    public static final FeatureDescription<Boolean> FD_IS_STRINGMATCH=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "StringMatch");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_STRINGMATCH);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_STRINGMATCH, getStringMatch(inst));
    }
    
    public boolean getStringMatch(PairInstance inst)
    {
       if (getMarkableString(inst.getAntecedent().getMarkable()).equals("")) 
         return false;
        if (getMarkableString(inst.getAntecedent().getMarkable()).
                equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable())))
        {
            return true; //instance.setFeature(feature, Boolean.T.getInt()); 
        }
        else
        {
            return false; // instance.setFeature(feature, Boolean.F.getInt());
        }
    }
    
    /** 1. Removes the square brackets from a the Markable string
     *  2. Removes articles and demonstrative pronouns
     */
    protected String getMarkableString(final Markable markable)
    {
        LanguagePlugin lang=ConfigProperties.getInstance().getLanguagePlugin();
        final String[] tokens = lang.markableString(markable).split(" ");
        final String[] pos = lang.markablePOS(markable).split(" ");
        final StringBuffer clean = new StringBuffer();

        if (tokens.length > 1)
        {
            for (int token = 0; token < tokens.length; token++)
            {
                NodeCategory pos_cat=lang.labelCat(pos[token]);
                if (
                        pos_cat!=NodeCategory.DT
                        &&  
                        pos_cat!=NodeCategory.PUNCT
                        &&
                            !tokens[token].toLowerCase().matches(SAXON_GENITIVE)
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
