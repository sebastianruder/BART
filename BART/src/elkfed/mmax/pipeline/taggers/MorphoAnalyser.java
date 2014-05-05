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

import java.util.List;
import elkfed.mmax.minidisc.Markable;

import edu.stanford.nlp.process.Morphology;

import elkfed.mmax.DiscourseUtils;
import static elkfed.mmax.MarkableLevels.DEFAULT_MORPH_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

/** Uses Morpha to perform morphological analysis on a document.
 *
 * @author ponzetsp
 */
public class MorphoAnalyser extends WordTagger
{    
    /** The lemma attribute */
    public static String LEMMA_ATTRIBUTE = "lemma";

    /** Returns the markable level for morphological data */
    public String getLevelName() {
        return DEFAULT_MORPH_LEVEL;
    }
    
    /** Tags a document using Morpha */
    protected void tag()
    {   
        final String[] tokens = currentDocument.getTokens();
        final List<Markable> posLevel = 
                DiscourseUtils.getMarkables(currentDocument,DEFAULT_POS_LEVEL);
        
        for (int token = 0; token < tokens.length; token++)
        { tags.add(lemmatize(tokens[token], posLevel.get(token).getAttributeValue(TAG_ATTRIBUTE)));}        
    }
    
    protected String getWordAttribute()
    { return LEMMA_ATTRIBUTE; }
    
    public static String lemmatize(String word, String tag)
    { return Morphology.stemStatic(word, tag.toUpperCase()).word(); }
}
