/*
 * Copyright 2007 EML Research
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

import static elkfed.lang.EnglishLinguisticConstants.*;

/** Extracts Nested NPs that are modifier nouns (or prenominals).
 *
 * @author ponzetsp
 */
public class CompoundIdentifier extends NestedNPIdentifier {
    
    /** Creates a new instance of CompoundIdentifier */
    public CompoundIdentifier() {
        super();
    }
    
    protected void nestedNPScan(
        final String[] tokens, final String[] posTags, final int[] discourseElementPositions)
    {
        // scan right to left, from last but one token
        for (int token = tokens.length-2; token > -1; token--)
        {
            // we don't want any puctuantion junk
            if (tokens[token].matches(PUNCTUATION_MARK))
            { break; }
            
            if (
                   (
                        posTags[token].matches(COMMON_NOUN_POS)
                    ||
                        posTags[token].matches(PROPER_NOUN_POS)
                    
                    // || uncomment to get cardinal premodifiers...
                    //    posTags[token].matches(CARDINAL_NUMBER_POS)
                   )
                &&
                    // we take of coordinated structures in another place
                    !isCoordinated(posTags)
                &&
                    !posTags[token+1].matches(PROPER_NOUN_POS)
                &&  
                    !posTags[token+1].matches(CARDINAL_NUMBER_POS)
               )
            {
                int startingToken = 0;
                while (
                        // we don't want this stuff into the prenominal!
                        (
                            tokens[startingToken].toLowerCase().matches(POSSESSIVE_PRONOUN)
                         ||
                            tokens[startingToken].toLowerCase().matches(POSSESSIVE_ADJECTIVE)
                         ||
                            tokens[startingToken].toLowerCase().matches(ARTICLE)
                         ||
                            tokens[startingToken].toLowerCase().matches(PUNCTUATION_MARK)
                         ||
                            tokens[startingToken].toLowerCase().matches(MALE_DESIGNATOR)
                         ||
                            tokens[startingToken].toLowerCase().matches(FEMALE_DESIGNATOR)                            
                         ||
                            tokens[startingToken].toLowerCase().matches(RELATIVE_PRONOUN)                            
                         ||
                            posTags[startingToken].toLowerCase().matches(CARDINAL_NUMBER_POS)
                         ||
                            posTags[startingToken].toLowerCase().matches(DETERMINER_POS)
                         ||
                            posTags[startingToken].toLowerCase().matches(ADJECTIVE_POS)
                        )
                       &&
                        startingToken < token
                    )
                { startingToken++; }
                final int[] newMarkablePosition =
                {discourseElementPositions[startingToken],discourseElementPositions[token]};
                embeddedNPSpans.add(newMarkablePosition);
            }
        }
    }
    
    /** Checks whether an array of pos tags contains a coordinated conjuction */
    private boolean isCoordinated(final String[] posTags)
    {
        for (int token = posTags.length-2; token > 0; token--)
        {
            if (
                    
                        posTags[token].matches(COORDINATING_CONJUCTION_POS)
                    &&
                        (
                            posTags[token-1].matches(COMMON_NOUN_POS)
                         ||
                            posTags[token-1].matches(PROPER_NOUN_POS)
                        )
                )
            { return true; }
        }
        return false;
    }
    
    protected String isPrenominal()
    { return "true"; }
}
