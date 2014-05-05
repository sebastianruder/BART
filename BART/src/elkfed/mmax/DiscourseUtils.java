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

package elkfed.mmax;

import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_RESPONSE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SENTENCE_LEVEL;
import static elkfed.mmax.pipeline.PipelineComponent.TAG_ATTRIBUTE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import elkfed.config.ConfigProperties;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.Pipeline;
import elkfed.util.TypeUtil;

/** A wrapper around a MMAX2Discourse object
 *
 * @author ponzetto
 */
public class DiscourseUtils
{   
    private static final TypeUtil<List<String>> STRING_LIST = null;
        
    /** Gets the full text of a MMAX2Discourse */
    public static String getText(MiniDiscourse document)
    {
        final StringBuffer text = new StringBuffer();
        
        for (String s: document.getTokens()) {
            text.append(s).append(" ");
        }
        return text.deleteCharAt(text.length()-1).toString();
    }

    
    /** Gets the tokens of a MMAX2Discourse grouped by sentence */
    public static String[][] getSentenceTokens(MiniDiscourse doc)
    {
        final List<Markable> sentences = getSentences(doc);
        String[][] sentenceTokens = new String[sentences.size()][];
        
        for (int sentence = 0; sentence < sentences.size(); sentence++)
        { sentenceTokens[sentence] = sentences.get(sentence).getDiscourseElements(); }
        return sentenceTokens;
    }
    
    /** Gets the token ids of a MMAX2Discourse grouped by sentence */
    public static String[][] getSentenceTokenIDs(MiniDiscourse doc)
    {
        final List<Markable> sentences = getSentences(doc);
        String[][] sentenceTokenIDs = new String[sentences.size()][];
        
        for (int sentence = 0; sentence < sentences.size(); sentence++)
        { sentenceTokenIDs[sentence] = sentences.get(sentence).getDiscourseElementIDs(); }
        return sentenceTokenIDs;
    }
    
    /** Gets the sentence markables */
    public static List<Markable> getSentences(MiniDiscourse doc)
    { 
        return doc.getMarkableLevelByName(DEFAULT_SENTENCE_LEVEL)
                .getMarkables();
    }
    
    /** Gets a List of Markable(s) from a MMAX2 document */
    public static List<Markable> getMarkables(MiniDiscourse doc, String markableLevel) {
        return doc.getMarkableLevelByName(markableLevel).getMarkables();
    }
    
    /** Gets the PoS tags of a Markable */
    public static String[] getPosTags(MiniDiscourse doc, Markable markable)
    { return getTags(doc, markable, DEFAULT_POS_LEVEL); }
    
    /** Gets tags of a Markable */
    public static String[] getTags(MiniDiscourse doc,
            Markable markable, String markableLevel)
    {
        final MarkableLevel level = doc.getMarkableLevelByName(markableLevel);
        final String[] discourseElementIDs = markable.getDiscourseElementIDs();
        final String[] tags = new String[discourseElementIDs.length];
        
        for (int tag = 0; tag < tags.length; tag++)
        {
            tags[tag] = (
                         (Markable)
                            level.getMarkablesAtDiscourseElementID(discourseElementIDs[tag]).get(0)
                        ).
                getAttributeValue(TAG_ATTRIBUTE);
        }
        return tags;
    }
    
    /** Deletes all specified markable levels */
    public static void deleteMarkableLevels(MiniDiscourse document,
            Pipeline pipeline)
    {
        Set<String> processedLevels=new HashSet<String>();
        Set<String> goldLevels=ConfigProperties.getInstance().getGoldLevels();
        pipeline.checkLevels(processedLevels,goldLevels);
        for (String s: processedLevels) {
            if (goldLevels.contains(s)) {
                throw new RuntimeException("Won't delete level '"+s+
                        "': it's a gold level");
            } else {
                document.getMarkableLevelByName(s).deleteAllMarkables();
            }
        }
    }
    
    /** Deletes the responses from a document */
    public static void deleteResponses(MiniDiscourse document)
    {
        MarkableLevel lvl=document.getMarkableLevelByName(DEFAULT_RESPONSE_LEVEL);
        if (lvl!=null)
        { lvl.deleteAllMarkables(); }
    }
  
    /** Gets discourse element positions of a Markable */
    public static int[] getDiscourseElementPositions(final Markable markable)
    {
        int start=markable.getLeftmostDiscoursePosition();
        int end=markable.getRightmostDiscoursePosition();
        final int[] discourseElementPositions = new int[end-start+1];
        for (int position = start; position <= end; position++)
        {
            discourseElementPositions[position-start] = position;
        }
        return discourseElementPositions;
    }
            
}
