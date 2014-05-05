/*
 * SequenceTagger.java
 *
 * Created on July 18, 2007, 12:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.pipeline.taggers;

import java.util.HashMap;

/** A generic wrapper around sequence taggers (e.g. chunkers and NERs)
 *
 * @author ponzo
 */
public abstract class SequenceTagger extends Tagger
{    
    protected int chunkBegin;
    protected int chunkEnd;            
    protected String currentChunk;

    @Override
    protected void clearBuffers()
    {        
        super.clearBuffers();
        
        this.chunkBegin = -1;
        this.chunkEnd = -1;
        this.currentChunk = "";
    }
    
    /** Add markables to the document: assumes IOB representation */
    protected void addMarkables()
    {
        int docSize=
                currentDocument.getDiscourseElementCount();
        
        for (int token = 0; token < docSize-1; token++)
        {
            checkToken(token, tags.get(token), tags.get(token+1));
        }
        checkToken(docSize-1, tags.get(tags.size()-1), null);
    }
    
    /** Add markables to the document: assumes IOB representation */
    protected void checkToken(int de, String tag, String nextTag)
    {
        if (!tag.equals("O"))
        {        
            // 1. check for start
            if (tag.startsWith("B-"))
            {
                // we are at the beginning of a chunk
                currentChunk = tag.substring(2).toLowerCase();
                chunkBegin = de;
            }
            
            // 2. check for end
            if (nextTag == null || nextTag.startsWith("B-") || nextTag.equals("O"))
            {
                // we are at the end of a chunk
                chunkEnd = de;
                
                // create the markable
                buffer.setLength(0);
                final HashMap<String,String> attributes = new HashMap<String,String>(levelAttributes); 
                attributes.put(TAG_ATTRIBUTE, currentChunk);
                currentLevel.addMarkable(chunkBegin,chunkEnd,attributes);
            }
        }
    }
}
