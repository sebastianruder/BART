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
import static elkfed.mmax.MarkableLevels.DEFAULT_ENAMEX_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;
import elkfed.mmax.util.Markables;
import java.util.ArrayList;

import java.util.HashMap;
import elkfed.mmax.minidisc.Markable;

/**
 *
 * @author yannick
 */
public class CarafeMerger extends Merger {
    
        /** Add coreference candidate markables to a document */
    protected void addMarkables()
    {
        // we leave alone the NPs that don't match Carafe's mentions
        for (Markable markable : enamexes)
        { addMarkable(markable, DEFAULT_ENAMEX_LEVEL); }
    }

    /** Add base attributes of a markable to the attribute hashmap */
    protected HashMap<String,String> addBaseAttributes(
            final Markable markable, final HashMap<String,String> attributes, final String type)
    {
        // is a prenominal or modifier?
        attributes.put(ISPRENOMINAL_ATTRIBUTE, isPrenominal());
        // is it a np chunk or enamex?
        //TODO: use information from the Carafe markable
        // and put CHUNK_LEVEL for nominals and ENAMEX_LEVEL for NEs
       
        if(markable.getAttributeValue("mtype").equals("NAM"))
            
        {
               attributes.put(TYPE_ATTRIBUTE,DEFAULT_ENAMEX_LEVEL); 
        }
        else if(markable.getAttributeValue("mtype").equals("NOM") || markable.getAttributeValue("mtype").equals("PRO"))
        {
             attributes.put(TYPE_ATTRIBUTE,DEFAULT_CHUNK_LEVEL); 
        }
       
        
       // attributes.put(TYPE_ATTRIBUTE, type);
        // np, org, loc, etc.
        attributes.put(LABEL_ATTRIBUTE, markable.getAttributeValue(TAG_ATTRIBUTE));
        return attributes;
    }
        
    protected void runComponent()
    {
        this.enamexes = new ArrayList<Markable>();
        // 0. clean up namely remove leading and trailing genitives and punctuation
        super.cleanUp();
        
        /*  1. Fix the candidate markable boundaries following Soon et al.:
         *  "both the noun phrases determined by the noun phrase identification
         *  module and the named entities are merged in such a way that if the
         *  noun phrase overlaps with a named entity, the noun phrase boundaries
         *  will be adjusted to subsume the named entity." 
         *
         *  2. Added embedded enamex are examded to full NP bounday
         *  [president [Clinton]_enamex]_np -----> [president Clinton]_enamex
         *
         */
        
        this.nps = getNPs();
        
        ForEachEnamex:
        for (Markable enamex : getEnamex())
        {
            // ForEachNamedEntity:
            for (Markable np : getNPs())
            {
                // get the two markable spans
                final int[] npSpan = Markables.getInstance().getCorrectBoundaries(np);
                final int[] enamexSpan = Markables.getInstance().getCorrectBoundaries(enamex);
                // if they have the same span, remove the NP    
                // same thing if NP right embed enamex
                if (Markables.getInstance().haveSameSpan(npSpan, enamexSpan))
                { 
                    this.enamexes.add(enamex);
                    continue ForEachEnamex; 
                }
            }
        }
    }
}
