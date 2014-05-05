/*
 * StanfordNER.java
 *
 * Created on July 18, 2007, 11:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.pipeline.taggers;

/** A wrapper around the Stanford NER
 * <p>
 * This component requires chunk markables.  
 * {@link elkfed.mmax.pipeline.Chunker}
 * currently provides this data.
 *
 * Remark YV: is this still true? There's nothing in here
 *  that seems to look at chunks
 * @author ponzo
 */
import static elkfed.mmax.MarkableLevels.DEFAULT_ENAMEX_LEVEL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.util.CoreMap;
import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;

public class NER extends SequenceTagger
{
    /** The tagger model */
    private static final String DEFAULT_CLASSIFIER = "./models/ner/ner-eng-ie.crf-3-all2006-distsim.ser.gz";
    
    private AbstractSequenceClassifier classifier;
    
    /** Creates a new instance of NER */
    public NER()
    { this(null); }

    /** Creates a new instance of NER */
    public NER(String modelFile)
    { 
        super();
        
        try
        { 
            if (modelFile == null)
            { this.classifier = CRFClassifier.getClassifier(
                      new File(ConfigProperties.getInstance().getRoot(),
                      DEFAULT_CLASSIFIER).getAbsolutePath()); }
            else
            { this.classifier = CRFClassifier.getClassifier(modelFile); }
        }
        catch (Exception e)
        { e.printStackTrace(); }
    }
    
    /** Returns the markable level for entity names */
    public String getLevelName() {
        return DEFAULT_ENAMEX_LEVEL;
    }

    protected void tag()
    {
        final String[][] sentences = DiscourseUtils.getSentenceTokens(currentDocument);
        for (int sentence = 0; sentence < sentences.length; sentence++)
        {
            final List<Word> tokens = new ArrayList<Word>();
            for (int token = 0; token < sentences[sentence].length; token++)
            { tokens.add(new Word(sentences[sentence][token])); }
        
            final List<CoreMap> ner = tag(tokens);    
            for (CoreMap label : ner)
            { tags.add(label.get(CoreAnnotations.TextAnnotation.class)); }
        }
    }
    
    private List<CoreMap> tag(List<Word> tokens)
    { return classifier.classifySentence(tokens); }
    
    @Override
    protected void checkToken(int de, String tag, String nextTag)
    {
        if (!tag.equals("O"))
        {        
            // 1. check for start
            if (!tag.equals(currentChunk))
            {
                // we are at the beginning of a chunk
                currentChunk = tag;
                chunkBegin = de;
            }
            
            // 2. check for end
            if (nextTag == null || (!nextTag.equals(tag)) || nextTag.equals("O"))
            {
                // we are at the end of a chunk
                chunkEnd = de;
                
                // create the markable
                buffer.setLength(0);
                final HashMap<String,String> attributes = new HashMap<String,String>(levelAttributes); 
                attributes.put(TAG_ATTRIBUTE, currentChunk.toLowerCase());
                currentLevel.addMarkable(chunkBegin,chunkEnd,attributes);
                
                currentChunk = "";
            }
        }
    }
}