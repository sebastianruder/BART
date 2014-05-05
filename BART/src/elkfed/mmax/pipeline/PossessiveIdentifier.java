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

/**
 * Extracts possessives from nested NPs.
 *
 * @author jason
 */
public class PossessiveIdentifier extends NestedNPIdentifier {
    
    /** Creates a new instance of PossessiveIdentifier */
    public PossessiveIdentifier() {
        super();
    }
    
    protected void nestedNPScan(
        final String[] tokens, final String[] posTags, final int[] discourseElementPositions)
    {
        for (int token = tokens.length-2; token > -1; token--)
        {
            if (
                    tokens[token].toLowerCase().matches(POSSESSIVE_PRONOUN)
                ||
                    tokens[token].toLowerCase().matches(POSSESSIVE_ADJECTIVE)
               )
            {
                final int[] newMarkablePosition = 
                    {discourseElementPositions[token],discourseElementPositions[token]};
                embeddedNPSpans.add(newMarkablePosition);
            }
        }
    }
    
    protected String isPrenominal()
    { return "true"; }
    
}
