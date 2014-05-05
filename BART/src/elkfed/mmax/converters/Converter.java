/*
 * Converter.java
 *
 * Created on August 13, 2007, 1:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.converters;

import elkfed.mmax.MMAX2FilenameFilter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;

/** A generic converter
 *
 * @author ponzo
 */
public abstract class Converter
{
    protected static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private File outputDir;
    
    /** Dumps a Corpus */
    public void dump(File corpusDir)
    {           
        mkOutputDir(corpusDir);
        
        if (!corpusDir.isDirectory())
        { dumpDocument(corpusDir.getAbsolutePath()); }
        else
        {   
            for (File mmaxFile : corpusDir.listFiles(getFilter()))
            { dumpDocument(mmaxFile.getAbsolutePath()); }
        }
    }
    
    protected abstract FilenameFilter getFilter();
    
    protected abstract void dumpDocument(String doc);
    
    protected void mkOutputDir(File corpusDir)
    {
        this.outputDir = new File(corpusDir, getOutputBaseDir()); 
        if (!outputDir.exists())
        { outputDir.mkdirs(); }
    }
    
    public File getOutputDir()
    { return this.outputDir; }
    
    protected abstract String getOutputBaseDir();
    
    /**
     *  Given "/path/to/something.extension" returns
     *  /path/to/something
     *
     */
    protected String getBaseFilePath(String fileName)
    { 
        // we ASSUME the extension comes after the last DOT
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1)
        { return fileName; }
        else
        { return fileName.substring(0, dotIndex); }
    }
    
    /**
     *  Given "/path/to/something.extension" returns
     *  "something"
     *
     */
    protected String getBaseFileName(String fileName)
    { return new File(getBaseFilePath(fileName)).getName(); }
    
    /** The default output file */
    protected String getOutputFile(String fileName)
    { return new File(outputDir, getBaseFileName(fileName)+getOutputExtension()).getAbsolutePath(); }
    
    protected abstract String getOutputExtension(); 
}
