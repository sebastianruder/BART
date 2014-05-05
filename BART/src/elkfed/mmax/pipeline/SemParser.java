/*
 * Copyright 2007 Project ELERFED
 * Copyright 2008 Simone Paolo Ponzetto/EML Research
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




import elkfed.mmax.DiscourseUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

import elkfed.mmax.minidisc.MiniDiscourse;
import static elkfed.mmax.MarkableLevels.DEFAULT_MORPH_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SENTENCE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SEMROLE_LEVEL;



/**

 * The SemParser class is a wrapper for different semantic parsers

 * 

 * (Adapted for ELKFED from SemParserWrapper)

 *

 * @author massimo

 */

public abstract class SemParser extends PipelineComponent {    

    protected static final Logger LOGGER = Logger.getAnonymousLogger();    

    /** For debugging */
    protected static final boolean DEBUG = false;

   
   /** The Map holding the parsed sentences as returned by the parser.
    *  It maps sentence ids (int) to arraylist of strings containing
    *  parses.
    */
    protected Map<Integer,ArrayList<String>> semparses;    

    /** The predicate target we are currently processing */
    protected Markable currentTarget;    

    /** The predicate target we are currently processing */
    protected String currentTargetLemmata;
    
    public SemParser() {
        // copied from SemParserWrapper

        //super(MarkableLevels.DEFAULT_SEMROLE_LEVEL.getName());
        this.semparses = new HashMap<Integer,ArrayList<String>>();
    }

    /** Returns the markable level for semantic parsing data */

    public String getLevelName() {

        return DEFAULT_SEMROLE_LEVEL;

    }

    

    /** Add semantic role markables to the document 

     *  was: addMarkableLevel()

     */

    public void addMarkables()

    {

        List<Markable> sentences = 
                DiscourseUtils.getMarkables(currentDocument,DEFAULT_SENTENCE_LEVEL);

        // iterate per sentence and annotate

        for (int sentence = 0; sentence < sentences.size(); sentence++)

        {

            if (semparses.containsKey(sentence))

            { addSentenceParses(sentences.get(sentence), semparses.get(sentence)); }

        }

    }

    

    /** Add parses to a given sentence */

    protected abstract void addSentenceParses(Markable sentence, List<String> parses);

    

    /** Set the lemmata of the target */

    protected void setTargetLemmata()

    {

        final StringBuffer lemmata = new StringBuffer();

        final MarkableLevel morphLevel = currentDocument
                .getMarkableLevelByName(DEFAULT_MORPH_LEVEL);

        for (String id : currentTarget.getDiscourseElementIDs())

        {
            lemmata.append(" ").append(

            	    morphLevel.getMarkablesAtDiscourseElementID(
                        id,
                        MiniDiscourse.DISCOURSEORDERCMP)
                .get(0).getAttributeValue("lemma"));
        }

        this.currentTargetLemmata = lemmata.deleteCharAt(0).toString();

    }

}

