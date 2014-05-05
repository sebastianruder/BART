/*
 * Copyright 2007 EML Research
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import elkfed.mmax.minidisc.DiscourseOrderMarkableComparator;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

import elkfed.mmax.minidisc.MarkableQuery;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.minidisc.MarkableHelper;
import static elkfed.mmax.MarkableLevels.*;

import static elkfed.mmax.pipeline.SentenceDetector.ORDER_ID_ATTRIBUTE;
import static elkfed.mmax.pipeline.taggers.MorphoAnalyser.LEMMA_ATTRIBUTE;
import static elkfed.lang.EnglishLinguisticConstants.NONREF_NP;

/** Any pipeline components who creates markables for
 *  coreference
 *
 * @author ponzo
 */
public abstract class MarkableCreator extends PipelineComponent
{
    /** Attribute field constants */
    public static final String POS_ATTRIBUTE = "pos";
    public static final String LEMMATA_ATTRIBUTE = "lemmata";
    public static final String SENTENCE_ID_ATTRIBUTE = "sentenceid";
    public static final String ISPRENOMINAL_ATTRIBUTE = "isprenominal";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String LABEL_ATTRIBUTE = "label";
    public static final String WIKI1_ATTRIBUTE = "wiki1";
    public static final String WIKI2_ATTRIBUTE = "wiki2";
    public static final String MAXSPAN_ATTRIBUTE = "maxspan";
    public static final String SPEAKER_ATTRIBUTE = "speaker";
    public static final String MINIDS_ATTRIBUTE = "min_ids";


    /* The relevant levels of the processed document */
    protected MarkableLevel currentSentenceLevel;
    protected MarkableLevel currentMorphLevel;
    protected MarkableLevel currentPosLevel;
    protected MarkableLevel currentChunkLevel;
    protected MarkableLevel currentEnamexLevel;
    
    /** A reusable list of nps */
    protected List<Markable> nps;
    
    /** Creates a new instance of MarkableCreator */
    public MarkableCreator() {
        this.nps = new ArrayList<Markable>();
    }
    
    /** Returns the default markable level */
    public String getLevelName() {
        return DEFAULT_MARKABLE_LEVEL;
    }
    
    /** Merges NP and enamex chunks into a single markable level */ 
    protected void annotateDocument()
    {
        init();
        runComponent();
    }
    
    /** Initialisation routines */
    private void init()
    {
        this.nps.clear();
        initializeLevels();
    }
    
    protected abstract void runComponent();
    
    /** Initialises the markable levels */
    private void initializeLevels()
    {
        this.currentSentenceLevel =
            currentDocument.getMarkableLevelByName(DEFAULT_SENTENCE_LEVEL);

        this.currentMorphLevel = 
            currentDocument.getMarkableLevelByName(DEFAULT_MORPH_LEVEL);

        this.currentPosLevel = 
            currentDocument.getMarkableLevelByName(DEFAULT_POS_LEVEL);

        this.currentChunkLevel = 
            currentDocument.getMarkableLevelByName(DEFAULT_CHUNK_LEVEL);

        this.currentEnamexLevel = 
            currentDocument.getMarkableLevelByName(DEFAULT_ENAMEX_LEVEL);
    }
    
    /** Gets the NPs */
    protected List<Markable> getNPs()
    {
        MarkableLevel chunkLevel=currentDocument
                    .getMarkableLevelByName(DEFAULT_CHUNK_LEVEL);
        MarkableQuery q=new MarkableQuery(chunkLevel);
        q.addAttCondition("tag", "np", MarkableQuery.OP_EQ);
        return q.execute(chunkLevel,MiniDiscourse.DISCOURSEORDERCMP);
    }
    
    /** Gets the named entities */
    protected List<Markable> getEnamex()
    { return DiscourseUtils.getMarkables(currentDocument,
              DEFAULT_ENAMEX_LEVEL); }
    
    /** Add coreference candidate markables to the document */
    protected void addMarkable(final Markable markable, final String type)
    {
        int start=markable.getLeftmostDiscoursePosition();
        int end=markable.getRightmostDiscoursePosition();


//we do not use markable.toString() here because of the brackets :(
        StringBuffer buf=new StringBuffer("");
        int ii=0;
        for (String s: markable.getDiscourseElements()) {
            if (ii>0) buf.append(" ");
            buf.append(s);
            ii++;
        }

//System.out.println("Adding -" + markable.toString() + "-");
if (buf.toString().toLowerCase().matches(NONREF_NP)) {
//  System.out.println("  discarded");
  return;
}


        /* attributes!
         * ------------------------------ > NOTE < ------------------------------
         * we add "sentenceid", "lemmas", "pos" for computing later the features
         */
        HashMap<String,String> attributes = new HashMap<String,String>(levelAttributes);

//if maxspan is present -- add min_ids and change span
//do this before the rest -- to get correct pos, lemmata etc

        if (markable.getAttributeValue(MAXSPAN_ATTRIBUTE)!=null) {
          start++;
          end++;
          attributes.put(MINIDS_ATTRIBUTE,"word_" + start +"..word_"+end);
          String[] spans = MarkableHelper.parseRanges(markable.getAttributeValue(MAXSPAN_ATTRIBUTE));
          start=currentDocument.getDiscoursePositionFromDiscourseElementID(spans[0]);
          end=currentDocument.getDiscoursePositionFromDiscourseElementID(spans[1]);
//no discontinuous maxspan possible
        }


        // basic attributes
        attributes = addBaseAttributes(markable, attributes, type);
        // which sentence does it belong to?
        attributes =
            addSentenceAttribute(attributes, start);
        // the lemmata of each token
        attributes =
            addLemmataAttribute(attributes, start, end);
        // the pos of each token
        attributes =
            addPoSAttribute(attributes, start, end);   


        currentLevel.addMarkable(start,end,attributes);
    }
    
    /** Add base attributes of a markable to the attribute hashmap */
    protected HashMap<String,String> addBaseAttributes(
            final Markable markable, final HashMap<String,String> attributes, final String type)
    {
        // is a prenominal or modifier?
        attributes.put(ISPRENOMINAL_ATTRIBUTE, isPrenominal());
        // is it a np chunk or enamex?
        attributes.put(TYPE_ATTRIBUTE, type);
        // np, org, loc, etc.
        attributes.put(LABEL_ATTRIBUTE, markable.getAttributeValue(TAG_ATTRIBUTE));
        return attributes;
    }
    
    protected abstract String isPrenominal();
    
    /** Add sentenceid attribute of a markable to the attribute hashmap */
    private HashMap<String,String> addSentenceAttribute(
            final HashMap<String,String> attributes, final int leftMostDiscoursePosition)
    {
        attributes.put(SENTENCE_ID_ATTRIBUTE,
                currentSentenceLevel.getMarkablesAtDiscoursePosition(leftMostDiscoursePosition,
                           new DiscourseOrderMarkableComparator()).get(0).
                getAttributeValue(ORDER_ID_ATTRIBUTE));
        return attributes;     
    }
    
    /** Add lemmata attribute of a markable to the attribute hashmap */
    private HashMap<String,String> addLemmataAttribute(
            final HashMap<String,String> attributes, int start, int end)
    {
        final StringBuffer lemmata = new StringBuffer();
        for (int i=start; i<=end; i++)
        {
            lemmata.append(" ").append(
            	currentMorphLevel.getMarkablesAtDiscoursePosition(i)
                     .get(0).getAttributeValue(LEMMA_ATTRIBUTE));
        }
        attributes.put(LEMMATA_ATTRIBUTE, lemmata.deleteCharAt(0).toString()); 
        return attributes;
    }
    
    /** Add pos attribute of a markable to the attribute hashmap */
    private HashMap<String,String> addPoSAttribute(
            final HashMap<String,String> attributes, int start, int end)
    {
        final StringBuffer pos = new StringBuffer();
        for (int i=start; i<=end; i++)
        {
            pos.append(" ").append(
                    currentPosLevel.getMarkablesAtDiscoursePosition(i).
                get(0).getAttributeValue(TAG_ATTRIBUTE));
        }
        attributes.put(POS_ATTRIBUTE, pos.deleteCharAt(0).toString());
        return attributes;
    }
}
