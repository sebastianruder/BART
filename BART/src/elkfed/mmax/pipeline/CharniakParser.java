/*
 * Copyright 2007 Project ELERFED
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

package elkfed.mmax.pipeline;

import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;


/**
 * The <code>CharniakParser</code> class uses 
 * <A HREF="http://www.cs.brown.edu/people/ec/">Eugene Charniak's parser</A> on
 * sentences from the current document.  Unlike most subclasses of 
 * PipelineComponent, it adds data for three markable levels: parsing, part of
 * speech tagging, and chunking.  Parsing is the default markable level, but
 * since part of speech tagging and chunking can be directly derived from the
 * parse trees, they are also added.
 * <p>
 * As of now, the location of the executable is hard coded, and this must be
 * changed.
 * <p>
 * This component requires sentence markables to be present for 
 * <code>currentDocument</code>.  {@link elkfed.mmax.pipeline.SentenceDetector}
 * currently provides this data.
 *
 * @author jason
 */
public class CharniakParser extends Parser {
    protected static final Logger _logger = Logger.getAnonymousLogger();
    
    /** The parser options */
    private static final String OPTIONS = "-K -l200 -T50";

    private File tmpFile;
    
    /** Creates a new instance of CharniakParser */
    public CharniakParser() {
        super();
    }
    
    /** Annotates the data found in the Document <code>currentDocument</code>
     * with parsing, part of speech, and chunking markables.*/
    protected void annotateDocument()
    {
        try
        {   
            
            tmpFile=File.createTempFile("parser",".txt");
            // write tmp file
            writeTmpFile();

            // run command
           Process parserProc=runCommand();

            // pipe results and return
            final BufferedReader readResult =
                    new BufferedReader(new InputStreamReader(parserProc.getInputStream()));

            String currentParse = null;
            while ((currentParse = readResult.readLine()) != null)    
            {
                // remove tabs and spaces           
                if (!currentParse.matches("\\s?"))
                { this.forest.add(currentParse); }
            }
           
            readResult.close();
            
            tmpFile.delete();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        
    }
    
    /** Write the temporary file to be parsed by the parser */
    private void writeTmpFile() throws IOException
    {
        final FileWriter writer = new FileWriter(tmpFile,false);
        String[][] sentences = null;
        try
        { sentences = DiscourseUtils.getSentenceTokens(currentDocument); }
        catch (Exception mmax2e)
        { mmax2e.printStackTrace(); }

        final StringBuffer sentenceBuffer = new StringBuffer();
        for (int sentence = 0; sentence < sentences.length; sentence++)
        {
            sentenceBuffer.setLength(0);
            sentenceBuffer.append("<s>");
            for (String token : sentences[sentence])
            { sentenceBuffer.append(" ").append(token); }
            writer.write(sentenceBuffer.append(" </s>\n").toString());

        }
        writer.flush();
        writer.close();
        _logger.log(Level.INFO,
                String.format("CharniakParser: wrote %d sentences to %s",
                       sentences.length, tmpFile.getName()));
    }

    private Process runCommand()
        throws IOException
    {
            /** The wrapper command to be spawn through the shell */
        final String[] command = {
                    "/bin/sh",
                    "-c",
                    new StringBuffer()
                        .append(new File(ConfigProperties.getInstance().getCharniakDir(),
                            "/parse.sh").getAbsolutePath())
                        .append(" ").append(OPTIONS).append(" ").append(tmpFile.getAbsolutePath()).toString()
        };
        _logger.log(Level.INFO,"CharniakParser: Running "+command[2]);
        return Runtime.getRuntime().exec(command);
    }
}
