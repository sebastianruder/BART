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

package elkfed.mmax;

/** This is a collection of markable levels. Level names are mapped
 *  to their gold-check status. To be used by pipeline and wrapper components
 *
 * Change 2008-03-17: use ConfigProperties.getGoldLevels() to get the set of
 * levels that are to be considered gold levels. This property is corpus-dependent
 * since some corpora include sentence boundary and/or parse information. --yv
 * @author ponzetsp
 */
public class MarkableLevels
{       
	private static final long serialVersionUID = -985926892412583442L;

    /** The default section level */
    public static final String DEFAULT_SECTION_LEVEL = DefaultLevels.SECTION.toString();
    
    /** The default sentence level */
    public static final String DEFAULT_SENTENCE_LEVEL = DefaultLevels.SENTENCE.toString();

    /** The default morph level */
    public static final String DEFAULT_MORPH_LEVEL = DefaultLevels.MORPH.toString();

    /** The default lex level */
    public static final String DEFAULT_LEX_LEVEL = DefaultLevels.LEX.toString();    
    
    /** The default pos level */
    public static final String DEFAULT_POS_LEVEL = DefaultLevels.POS.toString();
    
    /** The default pos level */
    public static final String DEFAULT_DEPREL_LEVEL = DefaultLevels.DEPREL.toString();

    /** The default CHUNK level */
    public static final String DEFAULT_CHUNK_LEVEL = DefaultLevels.CHUNK.toString();
    
    /** The default enamex level */
    public static final String DEFAULT_ENAMEX_LEVEL = DefaultLevels.ENAMEX.toString();
    
    /** The default markable level */
    public static final String DEFAULT_MARKABLE_LEVEL = DefaultLevels.MARKABLE.toString();

    /** The default coreference level */
    public static final String DEFAULT_COREF_LEVEL = DefaultLevels.COREF.toString();
    
    /** The default response level */
    public static final String DEFAULT_RESPONSE_LEVEL = DefaultLevels.RESPONSE.toString();
    
    /** The default parse level */
    public static final String DEFAULT_PARSE_LEVEL = DefaultLevels.PARSE.toString();

    /** The default semrole  level */
    public static final String DEFAULT_SEMROLE_LEVEL = DefaultLevels.SEMROLE.toString();
    
    /** The default semantic parse level */
    //public static final String DEFAULT_SEMPARSE_LEVEL = DefaultLevels.SEMPARSE.toString();
    
    /** The default yago  level */
    public static final String DEFAULT_YAGO_LEVEL = DefaultLevels.YAGO.toString();
    
    /** The coreference set attribute */
    public static final String COREF_SET_ATTRIBUTE = "coref_set";

    /** The coreference set attribute */
    public static final String DIRECT_ANT_ATTRIBUTE = "dir_antecedent";
    
    /** The status attribute for coreference markables */
    public static final String STATUS_ATTRIBUTE = "status";
    
    /** The optional status for coreference markables */
    public static final String STATUS_OPTIONAL = "opt";
    
    private enum DefaultLevels
    {
                // sample override
        SECTION,
        LEX,
        SENTENCE, 
        POS,
        CHUNK,
        ENAMEX,
        PARSE, 
        SEMROLE,      
        //SEMPARSE,        
        MARKABLE,        
        COREF,        
        MORPH,        
        RESPONSE,
        DEPREL,
        YAGO;
        
        @Override
        public String toString()
        { return super.name().toLowerCase(); }
    }
    
}
