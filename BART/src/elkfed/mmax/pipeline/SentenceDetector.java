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

import static elkfed.mmax.MarkableLevels.DEFAULT_SECTION_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SENTENCE_LEVEL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;

/**
 * The <code>SentenceDetector</code> class generates sentence markables for a
 * corpus.  It requires no prerequisite markable levels.
 */
public class SentenceDetector extends PipelineComponent {
     
    /** The sentence order id */
    static final String ORDER_ID_ATTRIBUTE = "orderid";
    
    /** The sentence detection model */
    private static final String MODEL = "./models/opennlp/EnglishSD.bin.gz";
    
    /** The sentence detector */
    private SentenceDetectorME detector;
    
    /** The ArrayList holding the discourse element ID onsets */
    private List<Integer> onsets;
    
    /** The ArrayList holding the discourse element ID offset */
    private List<Integer> offsets;

    /** Creates a new instance of SentenceDetector using the default model */
    public SentenceDetector()
    { this(null); }
    
    /** Creates a new instance of SentenceDetector using the model provided in
     * <code>modelFile</code>.
     * 
     * @param modelFile location of the sentence detection model
     *
     */
    public SentenceDetector(String modelFile) {
        
        super();
        this.onsets = new ArrayList<Integer>();
        this.offsets = new ArrayList<Integer>();
        
        try
        { 
            if (modelFile == null)
            { this.detector = new SentenceDetectorME(
                 new SentenceModel(new FileInputStream(
                		 new File(ConfigProperties.getInstance().getRoot(),
                				 MODEL).getCanonicalPath()))); }
            else
            { this.detector = new SentenceDetectorME(
            		new SentenceModel(new FileInputStream(modelFile))); }
        }
        catch (IOException ioe)
        { throw new RuntimeException("Cannot load sentence splitter",ioe); }
    }
    
    /** Returns the markable level for sentence data */
    public String getLevelName() {
        return DEFAULT_SENTENCE_LEVEL;
    }
    
    protected void annotateDocument() {
        
        // at each run we clean up the List holding the onset/offset
        onsets.clear();
        offsets.clear();
        
        // get basic onset/offset
        getOnsetOffset(currentDocument);

        // if we have sections fix the boundaries
        List<Markable> sections = DiscourseUtils.getMarkables(currentDocument,DEFAULT_SECTION_LEVEL);
        if (!sections.isEmpty())
        { fixBoundaries(sections); }
    }

    /** Annotates the document iterating through sections */
    private void fixBoundaries(List<Markable> sections)
    {  
        int sentenceAdded = 0;
        ForEachSection:
        for (Markable section : sections)
        {
            final int[] sectionOff = new int[]{
                section.getLeftmostDiscoursePosition(),
                section.getRightmostDiscoursePosition()
            };

        
            ForEachSentence:
            for (int sentence = 0; sentence < onsets.size(); sentence++)
            {
                final int[] sentenceOff = new int[]{
                    onsets.get(sentence),
                    offsets.get(sentence)
                };
                
                if (sentenceOff[0] > sectionOff[1])
                { break ForEachSentence; }
                
                if (
                    sentenceOff[0] < sectionOff[0]
                &&
                    sentenceOff[1] > sectionOff[1] 
                     
                )
                {
                    onsets.add(
                            sentence+1,
                            sectionOff[0]);
                    onsets.add(
                            sentence+2,
                            sectionOff[1]+1);
                    offsets.add(
                            sentence, 
                            sectionOff[1]);
                    offsets.add(
                            sentence, 
                            sectionOff[0]-1);


break ForEachSentence;
                }



                if (
                    sentenceOff[0] < sectionOff[0]
                &&
                    sentenceOff[1] > sectionOff[0] //should be ">=" but in fact doesn't make ANY difference (corrected by the previous section, unless one-word
                &&
                    sentenceOff[1] < sectionOff[1]

                )
                {
                    // sentence overlaps section LEFT
                    offsets.add(
                            sentence, 
                            sectionOff[0]-1);
                    onsets.add(
                            sentence+1,
                            sectionOff[0]);
                    break ForEachSentence;
                }
                else if (
                    sentenceOff[0] <= sectionOff[1]
                 &&
                    sentenceOff[1] > sectionOff[1]
                  &&
                    sentenceOff[0] > sectionOff[0]
                )
                {   
                    // sentence overlaps section RIGHT
                    offsets.add(
                            sentence+1, 
//                            sectionOff[1]-1);
                            sectionOff[1]);
                    onsets.add(
                            sentence,
//                            sectionOff[1]);
                            sectionOff[1]+1);
                    break ForEachSentence;
                }
            }
        }
    }
    
    /** Gets onset and offset of a text chunk */
    private void getOnsetOffset(MiniDiscourse discourse)
    {
        // get the integer array containing the positions of the end index of
        // every sentence
        final String text=DiscourseUtils.getText(discourse);
        final Span[] positions = detector.sentPosDetect(text);
        
        //System.out.println("#tokens:"+discourse.getDiscourseElementCount());
        //System.out.println("#words in text:"+text.split(" ").length);
        onsets.add(0);
        for (int pos = 0; pos < positions.length; pos++)
        {
            final int wordsSpanned = text.substring(0, positions[pos].length()).split(" ").length;
            final int start = 
                    wordsSpanned;
            final int end =
                    start-1;
            
            if (start < discourse.getDiscourseElementCount())
            { onsets.add(start); }
            if (end >=0)
            { offsets.add(end); }
        }
        if (offsets.isEmpty() ||
            offsets.get(offsets.size()-1)!=
                discourse.getDiscourseElementCount()-1)
        {
            offsets.add(discourse.getDiscourseElementCount()-1);
        }
        if (onsets.size() != offsets.size())
        { throw new RuntimeException("Sentence Detector failed"); }
    }
    
    /** Add sentence markables to the document */
    protected void addMarkables()
    {
        final StringBuffer markableBuffer = new StringBuffer();
        for (int sentence = 0; sentence < onsets.size(); sentence++)
        {   
            markableBuffer.setLength(0);          
            final HashMap<String,String> attributes = new HashMap<String,String>(levelAttributes);
            attributes.put(ORDER_ID_ATTRIBUTE, Integer.toString(sentence));
            
            currentLevel.addMarkable(onsets.get(sentence),
                offsets.get(sentence),
                attributes);
        }
    }
}
