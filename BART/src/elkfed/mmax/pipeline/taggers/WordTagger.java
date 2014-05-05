/*
 * WordTagger.java
 *
 * Created on July 25, 2007, 5:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.pipeline.taggers;

import java.util.HashMap;

/** A generic wrapper around word taggers (e.g. POS taggers and lemmatizer)
 *
 * @author ponzo
 */
public abstract class WordTagger extends Tagger
{
    protected void addMarkables()
    {
        final String[] discourseElements =
                currentDocument.getTokens();
        
        for (int token = 0; token < discourseElements.length; token++) {
            
            final HashMap<String,String> attributes =
                    new HashMap<String,String>(levelAttributes);
            attributes.put(getWordAttribute(), tags.get(token).toLowerCase().replaceAll("&","&amp;"));
            currentLevel.addMarkable(token,token,attributes);
        }
    }
    
    protected String getWordAttribute()
    { return TAG_ATTRIBUTE; }
}
