/*
 * MentionFilenameFilter.java
 *
 * Created on August 13, 2007, 1:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.converters;

import java.io.File;
import java.io.FilenameFilter;

/** A filenamefilter for mention files
 *
 * @author ponzo
 */
public class MentionFilenameFilter implements FilenameFilter
{    
    public static final String MENTION_EXT = ".mention";   
    
    public static final MentionFilenameFilter FILTER_INSTANCE= new MentionFilenameFilter();
    
    public boolean accept(File directory, String name)
    { return name.endsWith(MENTION_EXT); }
}
