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



import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Markable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import elkfed.mmax.minidisc.MarkableLevel;
//import org.eml.MMAX2.discourse.MMAX2DiscourseElement;

import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.Set;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;

/**
 * The Parser class is a wrapper for different parsers.
 * All parsers also add POS and chunk data, and they are all expected to return
 * parses as strings.
 *
 * @author jrsmith
 */
public abstract class Parser extends PipelineComponent {
            
    /** The ArrayList holding the parse trees as returned by the parser */
    protected List<String> forest;
    
    /** The attributes of the default markable level*/
    protected Map<String,String> markableLevelAttributes;
    
    /** The ArrayList holding the PoS tags */
    protected List<String> posTags;
    
    /** Markable levels for the part of speech tags */
    protected MarkableLevel posLevel;
    
    /** Markable levels for the chunking tags */
    protected MarkableLevel chunkLevel;
    
    /** The attributes of the markable level for part of speech tagging */
    protected final Map<String,String> posAttributes;
        
    /** The attributes of the markable level for chunking */
    protected final Map<String,String> chunkAttributes;
    
    /** Creates a new instance of Parser */
    public Parser() {
        
        this.forest = new ArrayList<String>();
        this.posTags = new ArrayList<String>();
        
        this.posAttributes = new HashMap<String,String>();
        this.posAttributes.put("mmax_level", DEFAULT_POS_LEVEL);
                
        this.chunkAttributes = new HashMap<String,String>();
        this.chunkAttributes.put("mmax_level", DEFAULT_CHUNK_LEVEL);
    }
        
    /*
     * Annotates the corpus given in <code>data</code> by calling
     * annotateDocument() and addMarkables() for each document in the corpus.
     * 
     * @param data  the corpus to be annotated
     */
    @Override
    public void annotate(MiniDiscourse doc)
    {
            this.currentDocument = doc;
            this.currentLevel = currentDocument.getMarkableLevelByName(levelName);
            this.posLevel = currentDocument.getMarkableLevelByName(DEFAULT_POS_LEVEL);
            this.chunkLevel = currentDocument.getMarkableLevelByName(DEFAULT_CHUNK_LEVEL);
            
            // reset forest field
            this.forest.clear();
            // reset tags field
            this.posTags.clear();
            
            annotateDocument();
            addMarkables();
            
            currentLevel.saveMarkables();
            posLevel.saveMarkables();
            chunkLevel.saveMarkables();
    }
    
    /** Parses the sentences in the current document
     * forest should be populated here
     */
    protected abstract void annotateDocument();
    
    /** Add parser, part of speech, and chunk markables */
    protected void addMarkables() {
        
        final StringBuffer markableBuffer = new StringBuffer();
        List<Markable> sentences = null;
        try
        { sentences = DiscourseUtils.getSentences(currentDocument); }
        catch (Exception mmax2e)
        { mmax2e.printStackTrace(); }
        
        for (int sentence = 0; sentence < sentences.size(); sentence++)
        {
            /** Add the parse tree markables */
            final Map<String,String> attributes =
                    new HashMap<String,String>(levelAttributes);
            attributes.put(TAG_ATTRIBUTE, forest.get(sentence).replaceAll("&", "&amp;"));
            markableBuffer.setLength(0);
            Markable sent_m=sentences.get(sentence);
            int start=sent_m.getLeftmostDiscoursePosition();
            int end=sent_m.getRightmostDiscoursePosition();
            currentLevel.addMarkable(start,end, attributes);
            
            /** Retrieve chunk tags from the parse tree and add chunk markables */
            boolean inNP = false;
            int startNP = -1;
            int wordLoc = 0;
            int depth = 0;
            for(String tok : forest.get(sentence).replaceAll("\\)", ") ").split("\\s+"))
            {
                if (tok.matches("\\(NP"))
                {
                    inNP = true;
                    startNP = wordLoc; 
                    depth = 0;
                }
                
                if ((inNP) && (tok.matches(".*\\)")))
                {
                    depth--; 
                }
                if ((inNP) && (tok.matches("\\(.*")))
                {
                    depth++;
                }
                
                if (tok.matches(".+\\)"))
                {
                    wordLoc++;
                }
                
                if ((depth == 0) && (inNP))
                {
                    inNP = false;
                    final Map<String,String> cAttributes =
                        new HashMap<String,String>(chunkAttributes);
                    markableBuffer.setLength(0);
                    cAttributes.put(TAG_ATTRIBUTE, "np");
                    //TODO: check if it's not start+wordLoc-1 ?
                    chunkLevel.addMarkable(
                        start+startNP,
                        start+wordLoc-1,
                        cAttributes);
                }

            }
            
            /** Create a tree object from the current sentence */
            Tree currentTree = new LabeledScoredTreeNode();
            // System.err.println("processing sentence: "+forest.get(sentence));
			currentTree = (LabeledScoredTreeNode) Tree.valueOf(forest.get(sentence));
            
            /** Retrieve POS tags from the parse tree */
            List<Label> taggedSent = new ArrayList<Label>(currentTree.preTerminalYield());
            for (int i = 0; i < taggedSent.size(); i++)
            { posTags.add(taggedSent.get(i).value()); }
        }
        
        /** Add POS tag markables */
        for (int pos = 0; pos < posTags.size(); pos++)
        { 
            final HashMap<String,String> attributes =
                    new HashMap<String,String>(posAttributes);
            attributes.put(TAG_ATTRIBUTE, posTags.get(pos).toLowerCase());    
            posLevel.addMarkable(pos,pos,attributes); 
        }
    }
           
    /** Returns the markable level for parsing data */
    public String getLevelName() {
        return DEFAULT_PARSE_LEVEL;
    }
    
    @Override
    public void checkLevels(Set<String> processedLevels,
            Set<String> goldLevels) {
        processedLevels.add(DEFAULT_POS_LEVEL);
        processedLevels.add(DEFAULT_CHUNK_LEVEL);
        processedLevels.add(DEFAULT_PARSE_LEVEL);
    }
}
