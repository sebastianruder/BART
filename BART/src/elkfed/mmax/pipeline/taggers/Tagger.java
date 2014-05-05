/*
 * Tagger.java
 *
 * Created on July 25, 2007, 4:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.pipeline.taggers;

import elkfed.mmax.pipeline.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A generic wrapper around taggers
 *
 * @author ponzo
 */
public abstract class Tagger extends PipelineComponent
{    
    protected final List<String> tags;
    protected final List<String> tagAttributes;
    
    /** Creates a new instance of Tagger */
    public Tagger()
    {
        super();
        this.tags = new ArrayList<String>();
        this.tagAttributes = new ArrayList<String>();
    }
    
    protected void annotateDocument() 
    {
        clearBuffers();
        tag();
        checkOutput();
    }
    
    protected void clearBuffers()
    { this.tags.clear(); 
        this.tagAttributes.clear();
    }
    
    /** Tags a document */
    protected abstract void tag();
    
    /** Check the output for the document is sane 
     *  by default we simply check we have as many tags
     *  as words
     */
    protected void checkOutput()
    {
        System.out.println("DE " + 
                currentDocument.getDiscourseElementCount()+ " " +
                tags.size() );
        if (currentDocument.getDiscourseElementCount()
                        != tags.size())
        { 
            throw new RuntimeException(
                    "Tagger failed\nDE" +
                    Arrays.asList(currentDocument.getTokens()).toString() + 
                    "\nTAGS" + tags.toString()
            ); 
        }
    }
}
