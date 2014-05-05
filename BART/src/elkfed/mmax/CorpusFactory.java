/*
 * Copyright 2007 EML Research
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

package elkfed.mmax;

import elkfed.config.ConfigProperties;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.File;
import java.util.Arrays;


/** A factory for creating {@link Corpus} objects.
 *
 * @author ponzetsp
 */
public class CorpusFactory
{   
    private static CorpusFactory singleton;

    public static synchronized CorpusFactory getInstance()
    {
            if (singleton == null)
            { singleton = new CorpusFactory(); }
            return singleton;
    }

    /** Create a new corpus from a specified file or directory.
     * 
     * @param dirMMaxFiles The location of an MMAX file or a directory of MMAX files.
     */
    public Corpus createCorpus(final File dirMMaxFiles)
    { return createCorpus(dirMMaxFiles, ConfigProperties.DEFAULT_DATA_ID); }
    
    /** Create a new corpus from a specified file or directory with a specific corpus ID
     * 
     * @param dirMMaxFiles The location of an MMAX file or a directory of MMAX files.
     * @param id A corpus ID.
     *
     * */
    public Corpus createCorpus(final File dirMMaxFiles, final String id)
    {
        if (dirMMaxFiles.isDirectory())
        { 
            File[] files=dirMMaxFiles.listFiles(new MMAX2FilenameFilter());
            Arrays.sort(files);
            return createCorpus(files,id);
        }
        else
        { return createCorpus(new File[]{dirMMaxFiles}, id); }
    }

    public static MiniDiscourse docFromFile(File file) {
        String fname=file.getName();
        if (fname.endsWith(".mmax")) fname=fname.substring(0,fname.length()-5);
        return(MiniDiscourse.load(file.getParentFile(),
                                  fname));
    }

    /** Create a new corpus from a list of files and a specific corpus ID
     * @param mmaxFiles An array of MMAX files. 
     * @param id A corpus ID.
     */
    public Corpus createCorpus(final File[] mmaxFiles, final String id)
    {
    	final Corpus data = new Corpus();
        for (File file : mmaxFiles)
        {   
            data.add(docFromFile(file));
        }
        data.setId(id);
        return data;
    }
}
