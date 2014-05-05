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

package elkfed.mmax.pipeline;

import elkfed.mmax.util.Markables;
import java.util.ArrayList;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

import static elkfed.mmax.MarkableLevels.*;
import static elkfed.lang.EnglishLinguisticConstants.*;
import static elkfed.mmax.pipeline.MarkableCreator.MAXSPAN_ATTRIBUTE;
import elkfed.config.ConfigProperties;


        
/**
 * This module does the following, as described in Soon, et. al (2001):
 * <blockquote>... both the noun phrases determined by the noun phrase identification 
 * module and the named entities are merged in such a way that if the noun phrase 
 * overlaps with a named entity, the noun phrase boundaries will be adjusted to subsume 
 * the named entity. </blockquote>
 * <P>
 * This component cannot be run until after the NER (named entity recognizer) module
 * is run and the data has been chunked (using either the Chunker module or the 
 * chunked data from the CharniakParser module).
 *
 * @author jason
 */
public class Merger extends MarkableCreator
{
    /** A reusable list of enamex */
    protected List<Markable> enamexes;
    
    /** Creates a new instance of Merger */
    public Merger() {
        super();
        this.enamexes = new ArrayList<Markable>();
    }
    
    /** Merges NP and enamex chunks into a single markable level */ 
    protected void runComponent()
    {
        // 0. clean up namely remove leading and trailing genitives and punctuation
        cleanUp();
        
        /*  1. Fix the candidate markable boundaries following Soon et al.:
         *  "both the noun phrases determined by the noun phrase identification
         *  module and the named entities are merged in such a way that if the
         *  noun phrase overlaps with a named entity, the noun phrase boundaries
         *  will be adjusted to subsume the named entity." 
         *
         *  2. Added embedded enamex are examded to full NP bounday
         *  [president [Clinton]_enamex]_np -----> [president Clinton]_enamex
         *
         */
        
        this.enamexes = getEnamex();
        
        ForEachNPChunk:
        for (Markable np : getNPs())
        {
//get the exact np boundaries (for maxspan adjustments)
  final int npstart=np.getLeftmostDiscoursePosition()+1;
  final int npend=np.getRightmostDiscoursePosition()+1;
           // get the two markable spans (but no articles etc)
      final int[] npSpan = Markables.getInstance().getCorrectBoundaries(np);

            // ForEachNamedEntity:
            for (int ne = 0; ne < enamexes.size(); ne++)
            {
                final Markable enamex = enamexes.get(ne);
                // get the two markable spans (but no articles etc)
                final int[] enamexSpan = Markables.getInstance().getCorrectBoundaries(enamex);
                

                // if they have the same span, remove the NP 
                //but keep the maxspan attribute, if present   
                // same thing if NP right embed enamex
               
                if (Markables.getInstance().haveSameSpan(npSpan, enamexSpan))
                {         

if (! MAXSPAN_ATTRIBUTE.equals("")) {

              if (np.getAttributeValue(MAXSPAN_ATTRIBUTE)!=null) 
                      enamex.setAttributeValue(MAXSPAN_ATTRIBUTE,np.getAttributeValue(MAXSPAN_ATTRIBUTE));
else 
            enamex.setAttributeValue(MAXSPAN_ATTRIBUTE,"word_"+npstart+"..word_"+npend);

}

                  continue ForEachNPChunk; 
                 }
                // else if they overlap (but not embed) fix *enamex* onset/offset
                else if (Markables.getInstance().rightembed(npSpan,enamexSpan))
                {
                   // adjust the enamex boundaries
                    int[] new_span=Markables.span_union(npSpan,enamexSpan);
                    enamex.adjustSpan(new_span[0],new_span[1]);
if (! MAXSPAN_ATTRIBUTE.equals("")) {
              if (np.getAttributeValue(MAXSPAN_ATTRIBUTE)!=null) 
                      enamex.setAttributeValue(MAXSPAN_ATTRIBUTE,np.getAttributeValue(MAXSPAN_ATTRIBUTE));
else 
            enamex.setAttributeValue(MAXSPAN_ATTRIBUTE,"word_"+npstart+"..word_"+npend);

}
                   enamexes.set(ne,enamex);
                   continue ForEachNPChunk;
                }
                // else if they overlap (but not embed) fix *np* onset/offset
                else if (Markables.getInstance().overlap(npSpan, enamexSpan))    
                {
                    // adjust the noun phrases boundaries
                    int[] new_span=Markables.span_union(npSpan, enamexSpan);
                    np.adjustSpan(new_span[0],new_span[1]);
/*
if (! MAXSPAN_ATTRIBUTE.equals("")) {

        if (np.getAttributeValue(MAXSPAN_ATTRIBUTE)==null) 
            np.setAttributeValue(MAXSPAN_ATTRIBUTE,"word_"+npstart+"..word_"+npend);

}
*/

                }
            }
if (! MAXSPAN_ATTRIBUTE.equals("")) {

        if (np.getAttributeValue(MAXSPAN_ATTRIBUTE)==null) 
            np.setAttributeValue(MAXSPAN_ATTRIBUTE,"word_"+npstart+"..word_"+npend);

}
            // if we made it so far, keep the np
            this.nps.add(np);
        }
    }
    
    /** Add coreference candidate markables to a document */
    protected void addMarkables()
    {
        for (Markable markable : nps)
        { addMarkable(markable, DEFAULT_CHUNK_LEVEL); }
        for (Markable markable : enamexes)
        { addMarkable(markable, DEFAULT_ENAMEX_LEVEL); }
    }
    
    /** Does some string massaging prior to markable level generation */
    protected void cleanUp()
    {   
        // 2. do some clean up
        for (Markable np : getNPs())
        { cleanMarkableUp(np, currentChunkLevel); } 
        for (Markable enamex : getEnamex())
        { cleanMarkableUp(enamex, currentEnamexLevel); }
    }
    
    /** Remove trailing saxon genitives and quotation marks from a markable */
    private void cleanMarkableUp(Markable markable, MarkableLevel level)
    {
        String firstToken = markable.getDiscourseElements()[0].toLowerCase();
        String lastToken = markable.getDiscourseElements()[markable.getDiscourseElements().length-1].toLowerCase();
        
         if (ConfigProperties.getInstance().getMarkCleanup()) {
        while (
                markable != null
                &&
                    (
                        firstToken.equals(SAXON_GENITIVE)
                      ||
                        firstToken.matches(PUNCTUATION_MARK)
                      ||
                        firstToken.matches(RELATIVE_PRONOUN)
                    )
        )
        { 
            markable = removeLeadingToken(markable, level);
            if (markable != null)
            { firstToken = markable.getDiscourseElements()[0].toLowerCase(); }
        }

        while (markable != null &&
               lastToken.matches(PUNCTUATION_MARK)) {
            markable = removeTrailingToken(markable, level);
            if (markable != null)
            { lastToken = markable.getDiscourseElements()[markable.getDiscourseElements().length-1].toLowerCase(); }
        }

       }
       if (markable!=null && lastToken.equals(SAXON_GENITIVE)) {
         if (ConfigProperties.getInstance().getFullPossessives()) {
//set maxspan attribute to output full possessives
          int start=markable.getLeftmostDiscoursePosition()+1;
          int end=markable.getRightmostDiscoursePosition()+1;
          markable.setAttributeValue(MAXSPAN_ATTRIBUTE,"word_"+start+"..word_"+end);
         }
         markable = removeTrailingToken(markable, level);
       }



    }    

    /** Used to remove leading tokens from markable */
    private Markable removeLeadingToken(final Markable markable, final MarkableLevel level)
    {    
        // we simply remove stand-alone markables
        if (markable.getDiscourseElements().length == 1)
        { return deleteMarkable(markable, level); }
        
        // we check we do not add nested NP with the same span
        for (Markable previousMarkable : level.getMarkables())
        {
            if (
                    previousMarkable.getLeftmostDiscoursePosition()
                    ==
                    markable.getLeftmostDiscoursePosition()+1
                &&
                    previousMarkable.getRightmostDiscoursePosition()
                    ==
                    markable.getRightmostDiscoursePosition()
               )
            { return deleteMarkable(markable, level); }
        }
        
        markable.adjustSpan(markable.getLeftmostDiscoursePosition()+1,
                    markable.getRightmostDiscoursePosition());
        
        return markable;
    }
    
    /** Used to remove trailing tokens from markable */
    private Markable removeTrailingToken(final Markable markable, final MarkableLevel level)
    {   
        // we simply remove stand-alone markables
        if (markable.getDiscourseElements().length == 1)
        { return deleteMarkable(markable, level); }
        
        // we check we do not add nested NP with the same span
        for (Markable previousMarkable : level.getMarkables())
        {
            if (
                    previousMarkable.getLeftmostDiscoursePosition()
                    ==
                    markable.getLeftmostDiscoursePosition()
                &&
                    previousMarkable.getRightmostDiscoursePosition()
                    ==
                    markable.getRightmostDiscoursePosition()-1
               )
            { return deleteMarkable(markable, level); }
        }

        markable.adjustSpan(markable.getLeftmostDiscoursePosition(),
                    markable.getRightmostDiscoursePosition()-1);
        
        return markable;
    }
    
    /** Returns complement of an ArrayList, elements in a which are NOT in b */
    private String[] complement(final ArrayList<String> a, final ArrayList<String> b)
    {
        if (a == null)
        { return null; }
        if (b == null)
        { return (String[]) a.toArray(new String[a.size()]); }
        
        final ArrayList<String> c = new ArrayList<String>();
        for (String elem : a)
        {
            if (!b.contains(elem))
            { c.add(elem); }
        }
        return (String[])c.toArray(new String[c.size()]);
    }
    
    private Markable deleteMarkable(final Markable markable, final MarkableLevel level)
    {
        try
        { level.deleteMarkable(markable); }
        catch (NullPointerException npe)
        {
            // MMAX2 deleteMarkable crappola
            // I hate
            // (1) to catch npe
            // (2) to handle with nothing
        }
        return null;
    }
    
    protected String isPrenominal()
    { return "false"; }
}
