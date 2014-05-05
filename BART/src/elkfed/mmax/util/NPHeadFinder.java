/*
 * Copyright 2007 EML Research
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

package elkfed.mmax.util;

import elkfed.mmax.minidisc.Markable;

/** Implements the NP head finding rules from the Appendix A
 *  of Michael Collins thesis
 * 
 * @author ponzo
 */
public class NPHeadFinder
{
    
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static NPHeadFinder singleton;
    
    /** Getter for instance */
    public static synchronized NPHeadFinder getInstance()
    {
        if (singleton == null)
        { singleton = new NPHeadFinder(); }
        return singleton;
    }
    
    /** Finds the head of a NP (token). See Collins (1999), Appendix A */
    public String getHead(final Markable markable, final String[] markablePos)
    {
        final String[] tokens = markable.getDiscourseElements();
        if (tokens.length==1) return tokens[0];
        return tokens[getHeadIndex(markablePos)];
    }
    
    /** Finds the head of a NP (token). See Collins (1999), Appendix A */
    public String getHead(final Markable markable)
    {
        final String[] tokens = markable.getDiscourseElements();
        if (tokens.length==1) return tokens[0];
        return tokens[getHeadIndex(markable)];
    }

    /** Finds the head of a NP (morphological root). See Collins (1999), Appendix A */
    public String getHeadLemma(final String[] lemmata, final String[] markablePos)
    { return lemmata[getHeadIndex(markablePos)]; }
    
    /** Finds the head of a NP (morphological root). See Collins (1999), Appendix A */
    public String getHeadLemma(final Markable markable)
    {
        final String[] lemmata = markable.getAttributeValue("lemmata").split(" ");
        return lemmata[getHeadIndex(markable)];
    }
    
    /* Finds the index of the NP head */
    public int getHeadIndex(final Markable markable)
    {   return getHeadIndex(markable.getAttributeValue("pos").split(" ")); }
    
    /* Finds the index of the NP head */
    public int getHeadIndex(final String[] posTags)
    {
        // If the last word is tagged POS, return (last-word)
        if (posTags[(posTags.length-1)].equals("pos"))
        { return posTags.length-1; }
        
        // Else search from right to left for the first child which
        // is an NN, NNP, NNPS, NNS, NX, POS, or JJR
        for (int pos = posTags.length-1; pos >= 0; pos--)
        {
            if (posTags[pos].matches("(nn|nns|np|nps|nnp|nnps|nx|pos|jjr)"))
            { return pos; }
        }
        
        // Else search from left to right for the first child which is an NP
        for (int pos = 0; pos < posTags.length; pos++)
        {
            if (posTags[pos].equals("np"))
            { return pos; }
        }
        
        // Else search from right to left for the first child which is a
        // $, ADJP or PRN
        for (int pos = posTags.length-1; pos >= 0; pos--)
        {
            if (posTags[pos].matches("(\\$|adjp|prn)"))
            { return pos; }
        }
        
        // Else search from right to left for the first child which is a CD
        for (int pos = posTags.length-1; pos >= 0; pos--)
        {
            if (posTags[pos].equals("cd"))
            { return pos; }
        }
        
        // Else search from right to left for the first child which is a
        // JJ, JJS, RB or QP
        for (int pos = posTags.length-1; pos >= 0; pos--)
        {
            if (posTags[pos].matches("(jj|jjs|rb|qb)"))
            { return pos; }
        }
        
        // Else return the last word
        return posTags.length-1;
    }
}
