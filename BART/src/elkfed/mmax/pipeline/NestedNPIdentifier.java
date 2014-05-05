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

import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.util.Markables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import elkfed.mmax.minidisc.Markable;

import elkfed.mmax.minidisc.MiniDiscourse;
import static elkfed.mmax.MarkableLevels.*;

import static elkfed.mmax.pipeline.SentenceDetector.ORDER_ID_ATTRIBUTE;
import static elkfed.mmax.pipeline.taggers.MorphoAnalyser.LEMMA_ATTRIBUTE;

/** A generic identifier for nested NPs.
 * <p>
 * This component requires sentence and chunk markables.  
 * {@link elkfed.mmax.pipeline.SentenceDetector} and {@link elkfed.mmax.pipeline.Chunker}
 * currently provide this data.
 *
 * @author jason
 */
public abstract class NestedNPIdentifier extends MarkableCreator {
    
    /** The spans of the embedded NP chunks */
    protected List<int[]> embeddedNPSpans;
    
    /** Creates a new instance of NestedNPIdentifier */
    public NestedNPIdentifier()
    { 
        super();
        this.embeddedNPSpans = new ArrayList<int[]>();
    }
    
    protected void runComponent() {
        this.nps.addAll(getNPs());
        getNestedNPs();
    }
    
    /** Identifies nested NPs */
    private void getNestedNPs()
    {
        // 0. reset the spans of the embedded NP chunks
        embeddedNPSpans.clear();
        
        // for each NP
        for (Markable np : nps)
        {
            // get tokens, discourse positions and pos tags
            final String[] tokens = np.getDiscourseElements();
            final String[] posTags = DiscourseUtils.getPosTags(currentDocument,np); 
            final int[] discourseElementPositions =
                    DiscourseUtils.getDiscourseElementPositions(np);
            
            // perform embedded NP scan
            nestedNPScan(tokens, posTags, discourseElementPositions);
        }     
    }
    
    /** Scans for nested NPs within a given markable. */
    protected abstract void nestedNPScan(
            final String[] tokens, final String[] posTags, final int[] discourseElementPositions);
    
    /** Add embedded NPs as coreference candidate markables to the document */
    protected void addMarkables()
    {  
        for (int[] span : embeddedNPSpans)
        {
            // we check we have no duplicated element among the previously
            // detected markable
            boolean isNew = true;
            for (Markable markable : currentLevel.getMarkables())
            {
                int[] markableSpan = 
                        Markables.getInstance().getCorrectBoundaries(markable); 
                if (Markables.getInstance().haveSameSpan(markableSpan, span)) 
                { isNew = false; break; }
            }

            // if we made it so far, add a new markable
            if (isNew)
            {
                /* attributes!
                 * ------------------------------ > NOTE < ------------------------------
                 * we add "sentenceid", "lemmas", "pos" for computing later the features
                 */
                HashMap<String,String> attributes = new HashMap<String,String>(levelAttributes);
                // basic attributes
                attributes = addBaseAttributes(attributes);
                // which sentence does it belong to?
                attributes = addSentenceAttribute(attributes, span[0]);
                // the lemmata of each token
                attributes = addLemmataAttribute(attributes, span);
                // the pos of each token
                attributes = addPoSAttribute(attributes, span);
                
                currentLevel.addMarkable(span[0], span[1], attributes);
            }
        }        
    }
    
    /** Add base attributes of a markable to the attribute hashmap */
    private HashMap<String,String> addBaseAttributes(final HashMap<String,String> attributes)
    {
        // is a prenominal or modifier?
        attributes.put(ISPRENOMINAL_ATTRIBUTE, isPrenominal());
        // is it a np chunk or enamex?
        attributes.put(TYPE_ATTRIBUTE, DEFAULT_CHUNK_LEVEL);
        // it is an NP
        attributes.put(LABEL_ATTRIBUTE, "np");
        return attributes;
    }    
    
    /** Add sentenceid attribute of a markable to the attribute hashmap */
    private HashMap<String,String> addSentenceAttribute(
            final HashMap<String,String> attributes, final int leftMostDiscourseElement)
    {
        attributes.put(SENTENCE_ID_ATTRIBUTE,
                currentSentenceLevel.getMarkablesAtDiscoursePosition(leftMostDiscourseElement,
                               MiniDiscourse.DISCOURSEORDERCMP).get(0).
                getAttributeValue(ORDER_ID_ATTRIBUTE));
        return attributes;
    }
    
    /** Add lemmata attribute of a markable to the attribute hashmap */
    private HashMap<String,String>
            addLemmataAttribute(final HashMap<String,String> attributes, final int[] span)
    {
        final StringBuffer lemmata = new StringBuffer();
        for (int discourseElementPosition = span[0];
             discourseElementPosition < (span[1]+1);
             discourseElementPosition++)
        {
            lemmata.append(" ").append(
                    currentMorphLevel.getMarkablesAtDiscoursePosition(discourseElementPosition,
                                 MiniDiscourse.DISCOURSEORDERCMP).get(0).
                    getAttributeValue(LEMMA_ATTRIBUTE));
        }
        attributes.put(LEMMATA_ATTRIBUTE, lemmata.deleteCharAt(0).toString());
        return attributes;
    }
    
    /** Add PoS attribute of a markable to the attribute hashmap */
    private HashMap<String,String>
            addPoSAttribute(final HashMap<String,String> attributes, int[] span)
    {
        final StringBuffer pos = new StringBuffer();
        for (int discourseElementPosition = span[0];
             discourseElementPosition < (span[1]+1);
             discourseElementPosition++)
        {
            pos.append(" ").append(
                 currentPosLevel.getMarkablesAtDiscoursePosition(discourseElementPosition,
                                MiniDiscourse.DISCOURSEORDERCMP).get(0).
                    getAttributeValue(TAG_ATTRIBUTE));
        }
        attributes.put(POS_ATTRIBUTE, pos.deleteCharAt(0).toString()); 
        return attributes;
    }
}
