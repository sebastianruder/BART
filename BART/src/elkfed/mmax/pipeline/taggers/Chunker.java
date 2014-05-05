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

package elkfed.mmax.pipeline.taggers;

import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

import java.util.Arrays;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;

/**
 * Uses Chunker shallow parser to chunk a MMAX2Discourse document.
 *
 * Relies on the YamCha parser found <A HREF="http://chasen.org/~taku/software/yamcha/">here</A>.
 *
 * In order for this module to work, there must be a <code>tmp</code> sub-directory in the main
 * project directory and the YamCha directory must be placed in the main project
 * directory.
 * <p>
 * This component requires sentence and part of speech markables.  
 * {@link elkfed.mmax.pipeline.SentenceDetector} and {@link elkfed.mmax.pipeline.POSTagger}
 * currently provide this data.
 * 
 * @author ponzetsp
 */
public class Chunker extends SequenceTagger
{    
    /** The tagger model */
    private static final String DEFAULT_MODEL = "models/yamcha/model/chunker.model";
    
    private File tmp_file=null;
    
    protected String model_file;

    /**
     * Creates a new instance of Chunker
     */
    public Chunker()
    { this(null); }
    
    /**
     * Creates a new instance of Chunker
     */
    public Chunker(String modelFile)
    { 
        super();
        
        if (modelFile == null)
        {
            model_file=new File(ConfigProperties.getInstance().getRoot(),
                      DEFAULT_MODEL).getAbsolutePath();
        }
        else
        {
            model_file=modelFile;
        }
    }
    
    /** Returns the markable level for chunking data */
    public String getLevelName() {
        return DEFAULT_CHUNK_LEVEL;
    }

    protected void tag()
    {
        writeTmpFile();
        getResults();
    }
    
    private void writeTmpFile()
    {
        final String[][] sentences = DiscourseUtils.getSentenceTokens(currentDocument);
        final String[][] sentenceIDs =
                DiscourseUtils.getSentenceTokenIDs(currentDocument);
        try
        {
            tmp_file=File.createTempFile("yamcha", ".txt");
            final FileWriter writer = new FileWriter(tmp_file, false);
            final MarkableLevel posLevel =
                    currentDocument.getMarkableLevelByName(DEFAULT_POS_LEVEL);
            
            for (int sentence = 0; sentence < sentences.length; sentence++)
            {
                for (int token = 0; token < sentences[sentence].length; token++)
                {
                    final String posTag = ((Markable)
                        posLevel.getMarkablesAtDiscourseElementID(
                            sentenceIDs[sentence][token], null).get(0)).
                                getAttributeValue(TAG_ATTRIBUTE, "").toUpperCase();
                    writer.write(new StringBuffer().
                            append(sentences[sentence][token]).
                            append("\t").append(posTag).append("\n").toString());
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();            
        }
        catch (IOException ioe)
        { ioe.printStackTrace(); }
    }

    private void getResults()
    {
        try
        {
           String[] command = new String[]{
                    "/bin/sh",
                    "-c",
                    new StringBuffer().
                    append(ConfigProperties.getInstance().getYamcha()).append(" -m ").
                    append(model_file).
                    append(" < ").append(tmp_file.getAbsolutePath()).toString()
           };
           System.err.println("Running: "+Arrays.asList(command));
            // run command
            final Process taggerProc = Runtime.getRuntime().exec(command);

            // pipe results and return
            final BufferedReader readResult =
                    new BufferedReader(new InputStreamReader(taggerProc.getInputStream()));

            String currentTag = "";
            while ((currentTag = readResult.readLine()) != null)    
            { 
                if (!currentTag.equals(""))
                { tags.add(currentTag.split("\\s+")[2]); }
            }
            readResult.close();
            //tmp_file.delete();
        }
        catch (IOException ioe)
        { ioe.printStackTrace(); }
    }
}
