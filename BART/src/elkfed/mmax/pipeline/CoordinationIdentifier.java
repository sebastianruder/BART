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


/** Extracts Nested NPs from coordinations.
 *
 * @author ponzetsp
 */
public class CoordinationIdentifier extends NestedNPIdentifier {
    
    /** Creates a new instance of CoordinationIdentifier */
    public CoordinationIdentifier() {
        super();
    }
    
     protected void nestedNPScan(
        final String[] tokens, final String[] posTags, final int[] discourseElementPositions)
    {
        // scan right to left, from last but one token
        for (int token = tokens.length-2; token > 0; token--)
        {
            // 0. check for coordination
            if (
                    
                        posTags[token].matches(COORDINATING_CONJUCTION_POS)
                    &&
                        !tokens[token].matches("&")
                    &&
                        (
                            posTags[token-1].matches(COMMON_NOUN_POS)
                         ||
                            posTags[token-1].matches(PROPER_NOUN_POS)
                        )
                )
            {
                final int[] beforeMarkablePosition = 
                    {discourseElementPositions[0],discourseElementPositions[token-1]};
                final int[] afterMarkablePosition = 
                    {discourseElementPositions[token+1],discourseElementPositions[discourseElementPositions.length-1]};
                embeddedNPSpans.add(beforeMarkablePosition);
                embeddedNPSpans.add(afterMarkablePosition);                
            }

        }
    }
    
    protected String isPrenominal()
    { return "false"; }
}
