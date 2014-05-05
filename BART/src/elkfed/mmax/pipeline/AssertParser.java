/*
 * 
 * AssertParser.java
 *
 * Created on April 4, 2008, 14:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


import elkfed.config.ConfigProperties;

import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Uses Sameer's ASSERT parser to produce the
 *  semantic role chunks in a MMAX2Discourse document.
 * 
 * @author ponzetsp
 */

public final class AssertParser extends SemParser
{ 
    protected static final Logger _logger = Logger.getAnonymousLogger();
    
    /** The parser options */
    private static final String OPTIONS = "";

    private File tmpFile;
    
    private String currentArg;
    
    private StringBuffer currentArgSpan;
    
    /** Creates a new instance of AssertParser */
    public AssertParser() {
        super();
        this.currentArgSpan = new StringBuffer();
    }
    
    /** 
     *  Annotates the data found in the Document <code>currentDocument</code>
     *  with semantic parsing markables.
     */
    protected void annotateDocument() {
        try
        {   
            
            tmpFile=File.createTempFile("assert",".txt");
            // write tmp file
            writeTmpFile();
        
            // run command
           Process parserProc=runCommand();
           
           // pipe results and return
           final BufferedReader readResult =
                new BufferedReader(new InputStreamReader(parserProc.getInputStream()));
            
            String currentParseLine = "";
            while ((currentParseLine = readResult.readLine()) != null)    
            {
                // keep the sane parses only
                if (currentParseLine.matches("^[0-9]+:.*"))
                {
                    final int sentenceID =
                            Integer.parseInt(currentParseLine.split(" ")[0].replaceAll(":", ""));
                    if (!semparses.containsKey(sentenceID))
                    { semparses.put(sentenceID, new ArrayList<String>()); }
                    
                    String semParse = normalizeSemanticParse(currentParseLine);
                    if (semParse != null) 
                    { semparses.get(sentenceID).add(semParse); }
                }
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
        String[][] sentences = DiscourseUtils.getSentenceTokens(currentDocument);

        final StringBuffer sentenceBuffer = new StringBuffer();
        for (int sentence = 0; sentence < sentences.length; sentence++)

        {
            sentenceBuffer.setLength(0);

            for (String token : sentences[sentence])
            { sentenceBuffer.append(" ").append(token); }

            writer.write(sentenceBuffer.append("\n").deleteCharAt(0).toString());
        }
        writer.flush();
        writer.close();
    }
    
    private Process runCommand()
        throws IOException
    {
            /** The wrapper command to be spawn through the shell */
        final String[] command = {
                    "/bin/sh",
                    "-c",
                    new StringBuffer()
                        .append(new File(ConfigProperties.getInstance().getAssertDir(),
                            "scripts/assert").getAbsolutePath())
                        .append(" ").append(OPTIONS).append(" ").append(tmpFile.getAbsolutePath()).toString()
        };
        _logger.log(Level.INFO,"AssertParser: Running "+command[2]);
        return Runtime.getRuntime().exec(command);
    }
    
    /** Normalizes the output of ASSERT to:
     * 
     *  1. removing line initial proposition-ids, e.g. "10: The official Iraqi 
     *     News Agency ..."
     *  2. shifting the parenthesis to remove extra spaces with their eclosing
     *     arguments: "[ARG0 Ritter] [TARGET heads ]" becomes 
     *     "[ARG0 Ritter] [TARGET heads]"
     *  3. checks that parenthesis are balanced, i.e. to avoid buggy output
     *     such as "[R-ARG0 which] Ritter heads] [ARG0 [TARGET made ] ..."
     * 
     *  Returns <b>null</b> if the output is not sane.
     * 
     */
    private String normalizeSemanticParse(String origParse) {
        // step (1) and (2)
        origParse.replaceAll("\\s+\\]", "]").replaceAll("^[0-9]+: ", "");
        boolean openedParenthesis = false;
        for (char character : origParse.toCharArray()) {
            switch (character) {
                case '[':
                    if (openedParenthesis) {
                        _logger.log(Level.WARNING,"Unbalanced parenthesis "+origParse);
                        return null;
                    } else {
                        openedParenthesis = true; break;
                    }
                case ']': openedParenthesis = false; break;
            }
        }
        return origParse;
    }    
    
    /** Add parses to a given sentence. Add parses to document by tagging
     *  arguments with ARG labels & TARGET pointers
     */
    protected void addSentenceParses(Markable sentence, List<String> parses)
    {
        // for each produced parse of the given sentence
        for (String sentenceParse : parses)
        {
            Map<Integer, String> tokens2discourseElements = 
                    getParseAlignmentMap(sentence, sentenceParse);
            // 1. first add TARGET
            addTarget(sentenceParse, tokens2discourseElements);
            // 2. then remaining arguments
            addArguments(sentenceParse, tokens2discourseElements);
        }
    }
    
    /** Returns the map aligning parse token ids with discourse element ids */
    private Map<Integer,String> getParseAlignmentMap(Markable sentence, String parse)
    {   
        Map<Integer,String> alignmentMap = new HashMap<Integer, String>();
        final String[] sentenceTokens = sentence.getDiscourseElements();
        final String[] sentenceTokenIDs = sentence.getDiscourseElementIDs();
        final String[] parseTokens = parse.split(" ");
     
        int sentenceToken = 0;
        for (int parseToken = 0; parseToken < parseTokens.length; parseToken++)
        {
            if (!parseTokens[parseToken].startsWith("["))
            {
                if (parseTokens[parseToken].endsWith("]"))
                {
                    parseTokens[parseToken] =
                        parseTokens[parseToken].replaceAll("\\]", "");
                }
                int currentSentenceToken = sentenceToken;
                try
                {
                    while (!parseTokens[parseToken].equalsIgnoreCase(sentenceTokens[sentenceToken]))
                    { sentenceToken++; }
                }
                catch (IndexOutOfBoundsException iobe)
                {
                    // tokens non alignables: reset index on sentence tokens
                    sentenceToken = currentSentenceToken;
                }
                alignmentMap.put(parseToken, sentenceTokenIDs[sentenceToken]);
            }
        }
        /* FOR ALIGNMENT CHECKING
        for (Integer id : alignmentMap.keySet())
        {
            System.out.println(
                                parseTokens[id] +
                                "--------------->" +
                                document.getDiscourseElementByID(alignmentMap.get(id)).getString()
            );
        }
        */
        return alignmentMap;
    }
    
    /** Adds the target to a proposition */
    private void addTarget(String sentenceParse, Map<Integer, String> alignmentMap)
    { addArguments(sentenceParse, alignmentMap, true); }

    /** Add the arguments to a proposition */
    private void addArguments(String sentenceParse, Map<Integer, String> alignmentMap)
    { addArguments(sentenceParse, alignmentMap, false); }
    
    /** Add the targets to a proposition */
    private void addArguments(
            String sentenceParse, Map<Integer, String> alignmentMap, boolean tagTarget)
    {   
        currentArg = "";
        currentArgSpan.setLength(0);
        final String[] parseTokens = sentenceParse.split(" ");
        
        for (int parseToken = 0; parseToken < parseTokens.length; parseToken++)
        {
            if (parseTokens[parseToken].startsWith("["))
            {
                // we are at the beginning of a new argument chunk
                currentArg = parseTokens[parseToken].replaceAll("\\[",  "");
                currentArgSpan.setLength(0);
                currentArgSpan.append(alignmentMap.get(parseToken+1)).append("..");
            }
            else
            {
                if (parseTokens[parseToken].endsWith("]"))
                {
                    // we are at the end of an argument chunk: add the markable
                    currentArgSpan.append(alignmentMap.get(parseToken));
                    final HashMap<String,String> attributes =
                            new HashMap<String,String>(levelAttributes); 
                    attributes.put(TAG_ATTRIBUTE, currentArg);
                    
                    // is it a target or an argument?
                    if (currentArg.equals("TARGET") && tagTarget)
                    {
                        // we tag a target
                        String[] range=MarkableHelper.parseRange(currentArgSpan.toString());
                        this.currentTarget = currentLevel.addMarkable(
                                currentLevel.getDocument()
                                    .getDiscoursePositionFromDiscourseElementID(range[0]),
                                currentLevel.getDocument()
                                    .getDiscoursePositionFromDiscourseElementID(range[1]),
                            attributes);
                        setTargetLemmata();
                        return;
                    }
                    else if (!currentArg.equals("TARGET") && !tagTarget)
                    {
                        // we tag other arguments
                        String[] range=MarkableHelper.parseRange(currentArgSpan.toString());
                        Markable argument = currentLevel.addMarkable(
                                currentLevel.getDocument()
                                    .getDiscoursePositionFromDiscourseElementID(range[0]),
                                currentLevel.getDocument()
                                    .getDiscoursePositionFromDiscourseElementID(range[1]),
                            attributes);
                        argument.setAttributeValue("target_lemmata", currentTargetLemmata);
                        argument.setAttributeValue("target", currentTarget.getID());
                    }
                }
            }
        }
    }
}
