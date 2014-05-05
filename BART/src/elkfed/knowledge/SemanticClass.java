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

package elkfed.knowledge;

import java.util.Map;
import java.util.HashMap;

import edu.brandeis.cs.steele.wn.DictionaryDatabase;
import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.PointerTarget;

import edu.brandeis.cs.steele.wn.POS;

/** The system's semantic classes. From Soon et al. (2001): In our system,
 *  we defined the following semantic classes: "female," "male," "person,"
 *  "organization," "location," "date," "time," "money, percent," and "object."
 *  These semantic classes are arranged in a simple ISA hierarchy. Each of the
 *  "female" and "semantic classes is a subclass of the semantic class "person,"
 *  while each of the semantic classes "organization," "location," "date," "time,"
 *  "money," and "percent" is a subclass of the semantic class "object".
 *
 * @author ponzo
 */
public enum SemanticClass
{
    PERSON, MALE, FEMALE, OBJECT, ORGANIZATION, LOCATION, DATE, TIME, MONEY, PERCENT, GPE, UNKNOWN, EVENT;
        /** Returns the string to be used for you pleasure */        
    public String getName() { return name().toLowerCase(); }

    /** Returns the Map mapping WN synset to semantic classes
     */
    public static Map<PointerTarget,SemanticClass> getSemClassMapping()
    {
        final DictionaryDatabase dictionary = WNInterface.getInstance().getDictionary();
        return getSemClassMapping(dictionary);
    }
    
    /** Returns the Map mapping WN synset to semantic classes
     * this method gets the dictionary passed in since it's called from the
     * constructor of WNInterface.
     */
    public static Map<PointerTarget,SemanticClass> getSemClassMapping(DictionaryDatabase dictionary)
    {
        final Map<PointerTarget,SemanticClass> mapping =
                new HashMap<PointerTarget,SemanticClass>();
        
        // run WN
        for (SemanticClass semClass : values())
        {
            final IndexWord word = dictionary.lookupIndexWord(POS.NOUN, semClass.getName());
            switch (semClass)
            {
                // PERSON --------------------> 1st sense
                case PERSON: mapping.put((PointerTarget) word.getSenses()[0], PERSON); break;
                // MALE ----------------------> 2nd sense
                case MALE: mapping.put((PointerTarget) word.getSenses()[1], MALE); break;
                // FEMALE --------------------> 2nd sense
                case FEMALE: mapping.put((PointerTarget) word.getSenses()[1], FEMALE); break;
                // OBJECT --------------------> 1st sense
                case OBJECT: mapping.put((PointerTarget) word.getSenses()[0], OBJECT); break;
                // ORGANIZATION --------------> 1st sense
                case ORGANIZATION: mapping.put((PointerTarget) word.getSenses()[0], ORGANIZATION); break;
                // LOCATION ------------------> 1st sense
                case LOCATION: mapping.put((PointerTarget) word.getSenses()[0], LOCATION); break;
                // DATE ----------------------> 1st sense
                case DATE : mapping.put((PointerTarget) word.getSenses()[0], PERSON); break;
                // TIME ----------------------> 1st sense
                case TIME : mapping.put((PointerTarget) word.getSenses()[0], TIME); break;
                // MONEY ---------------------> 1st sense
                case MONEY : mapping.put((PointerTarget) word.getSenses()[0], MONEY); break;
                // PERCENT ---------------------> 1st sense
                case PERCENT : mapping.put((PointerTarget) word.getSenses()[0], PERCENT); break;             
            }
        }
        return mapping;
    }
    
    /** Checks whether a given SemanticClass ISA person */
    public static boolean isaPerson(SemanticClass semclass)
    {
        switch (semclass)
        {
            case PERSON:
            case MALE:
            case FEMALE: return true;
            default : return false;
        }
    }
    
    /** Checks whether a given SemanticClass ISA object */
    public static boolean isaObject(SemanticClass semclass)
    {
        switch (semclass)
        {
            case OBJECT:
            case ORGANIZATION:
            case LOCATION:
            case DATE:
            case TIME:
            case MONEY:
            case PERCENT: return true;
            default : return false;
        }
    }

    public static boolean isaNumeric(SemanticClass semclass)
    {
        switch (semclass)
        {
            case MONEY:
            case PERCENT: return true;
            default : return false;
        }
    }
   
    /** Gets the semantic class given a string ID */
    public static SemanticClass getFromString(String semclassString)
    {
        for (SemanticClass semclass : values())
        {
            if (semclassString.toLowerCase().equals(semclass.getName()))
            { return semclass; }    
            
        }
 
        /** additional ACE mention types are mapped to object semantic class
         */
        if(semclassString.toLowerCase().equals("veh")
        ||
           semclassString.toLowerCase().equals("wea")     
        ||
          semclassString.toLowerCase().equals("fac")       
        ){
        return SemanticClass.OBJECT;
        }
        
        return null;
    }
}
