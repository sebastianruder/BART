/*
 * APFFilenameFilter.java
 *
 * Created on August 13, 2007, 3:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.converters;

import java.io.File;
import java.io.FilenameFilter;

/** 
 *  A filenamefilter for apf files
 *
 * @author ponzo
 */
public class APFFilenameFilter implements FilenameFilter {
    
    public static final String APF_EXT = ".apf";   
    
    public static final APFFilenameFilter FILTER_INSTANCE= new APFFilenameFilter();
    
    public boolean accept(File directory, String name)
    { return name.endsWith(APF_EXT); }
}
