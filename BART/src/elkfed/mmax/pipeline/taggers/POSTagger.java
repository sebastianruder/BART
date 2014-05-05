package elkfed.mmax.pipeline.taggers;


import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;
import elkfed.util.TypeUtil;

/** Part of speech tagger using Stanford's MaxentTagger.
 * <p>
 * This component requires sentence markables.  
 * {@link elkfed.mmax.pipeline.SentenceDetector}
 * currently provides this data.
 */
public class POSTagger extends WordTagger
{
    
    /** The tagger's model */
    private static final String MODEL =
     	"./models/pos/wsj3t0-18-bidirectional/train-wsj-0-18.holder";
   
    /** The stanford pos tagger */
    private MaxentTagger tagger;

    /** Creates a new instance of POSTagger using the default model */
    public POSTagger() {
        this(null);
    }
    
    /** Creates a new instance of POSTagger */
    public POSTagger(String modelFile) {
        
        super();
        
        try
        { 
            if (modelFile == null)
            { this.tagger = new MaxentTagger(
                      new File(ConfigProperties.getInstance().getRoot(),
                      MODEL).getCanonicalPath()); }
            else
            { this.tagger = new MaxentTagger(modelFile);  }
        }
        catch (Exception e)
        { e.printStackTrace(); }
    }
    
    /* Returns the markable level for part of speech data */
    public String getLevelName() {
        return DEFAULT_POS_LEVEL;
    }    
    
    protected void tag() 
    {    
        final String[][] sentences = DiscourseUtils.getSentenceTokens(currentDocument);
        for (String[] tokens : sentences)
        {   
            List<Word> sentence = new ArrayList<Word>();
            for (String word : tokens)
            { sentence.add(new Word(word)); }
            List<TaggedWord> taggedSentence = tagSentence(sentence);

            for (TaggedWord word : taggedSentence)
            { tags.add(word.tag()); }
        }
    }
    
    public List<TaggedWord> tagSentence(List<Word> sentence)
    { return tagger.tagSentence(sentence); }
	
}
