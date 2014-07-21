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

package elkfed.lang;

import java.util.regex.Pattern;

/** This is the repository for the constants used in generic
 *  linguistic processing.
 *
 * @author ponzetsp
 */
public class EnglishLinguisticConstants extends LinguisticConstants
{   
	/** Days of the week, months, years; shouldn't be matched with [Relaxed]StringMatch */
    public static final String DAYS_MONTHS_YEAR = "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|January|February|March|April|May|June|July|August|September|October|November|December|year)";
	
    /** Just a static repository... */
    public EnglishLinguisticConstants() {}
    
    /** Personal pronoun regexp (nominative case)*/
    public static final String PERSONAL_PRONOUN_NOM = "(i|you|he|she|it|we|you|they)";
    
    /** Personal pronoun regexp (nominative case)*/
    public static final String PERSONAL_PRONOUN_ACCUSATIVE = "(me|you|him|her|it|us|you|them)";
    
    /** Possessive pronoun regexp */
    public static final String REFLEXIVE_PRONOUN =
        "(myself|yourself|yourselves|himself|herself|itself|ourselves|themselves)";
    
    /** Possessive pronoun regexp */
    public static final String POSSESSIVE_PRONOUN = "(mine|yours|his|hers|its|ours|theirs)";
    
    /** Possessive pronoun regexp */
    public static final String POSSESSIVE_ADJECTIVE = "(my|your|his|her|its|our|their)";

    public static final Pattern FIRST_SECOND_PERSON_RE=
            Pattern.compile("(i|me|my|you|your|we|us|our)");

    public static final String FIRST_PERSON_SG_PRO=
             new StringBuffer().append("^").append("(i|me|my|mine|myself)").append("$").toString();

    public static final String FIRST_PERSON_PL_PRO=
             new StringBuffer().append("^").append("(we|us|our|ours|ourselves)").append("$").toString();

    public static final String SECOND_PERSON_PRO=
             new StringBuffer().append("^").append("(you|your|yours|yourself|yourselves)").append("$").toString();

    /** Pronoun regexp */
    public static final String PRONOUN = new StringBuffer().
            append(PERSONAL_PRONOUN_NOM).append("|").
            append(PERSONAL_PRONOUN_ACCUSATIVE).append("|").
            append(REFLEXIVE_PRONOUN).append("|").
            append(POSSESSIVE_PRONOUN).append("|").
            append(POSSESSIVE_ADJECTIVE).append("|").
            append(REFLEXIVE_PRONOUN).toString();
    
    /** Singular pronoun regexp */
    public static final String SINGULAR_PRONOUN_ADJ = new StringBuffer().
        append("(i|you|he|she|it|").append("me|you|him|her|it|").
        append("myself|yourself|himself|herself|itself|").
        append("mine|yours|his|hers|its|").
        append("my|your|his|her|its)").toString();
    
    /** Plural pronoun regexp */
    public static final String PLURAL_PRONOUN_ADJ = new StringBuffer().    
        append("(we|you|they|").append("us|you|them|").
        append("ourselves|yourselves|themselves|").
        append("ours|yours|theirs|").
        append("our|your|their)").toString();
            
    /** Male pronoun regexp */
    public static final String MALE_PRONOUN_ADJ = "(he|him|himself|his)";
    
    /** Female pronoun regexp */
    public static final String FEMALE_PRONOUN_ADJ = "(she|her|herself|hers|her)";
    
    /** Neutral pronoun regexp */
    public static final String NEUTRAL_PRONOUN_ADJ = "(it|itself|its)";
    
    /** Relative pronoun regexp */
    public static final String RELATIVE_PRONOUN = "(who|which|whom|whose|that)";
            
    /** The Saxon genitive */
    public static final String SAXON_GENITIVE = "'s";
 
    /** The definite article */
    public static final String DEF_ARTICLE = "the";
    
    /** The indefinite article */
    public static final String INDEF_ARTICLE = "(a|an)";
    
    /** The article regexp */
    public static final String ARTICLE = "(a|an|the)";

    /** A leading article */
    public static final String LEADING_ARTICLE =
        new StringBuffer().append("^").append(ARTICLE).toString();
    
    /** The demonstrative regexp */
    public static final String DEMONSTRATIVE = "(this|that|these|those)";
    
    /** The array of demonstratives */
    public static final String[] DEMONSTRATIVES =
        DEMONSTRATIVE.replaceAll("\\(|\\)","").split("\\|");    

    /** The array of posessive pronouns */
    public static final String[] POSSESSIVE_PRONOUNS =
        POSSESSIVE_PRONOUN.replaceAll("\\(|\\)","").split("\\|");    
    
    /** The Peen TB labels for common noun */
    public static final String COMMON_NOUN_POS = "(nn|nns)"; 
    
    /** The Peen TB labels for proper noun */
    public static final String PROPER_NOUN_POS = "(np|nps|nnp|nnps)";
    
    /** The Peen TB labels for noun */
    public static final String NOUN = new StringBuffer().
            append(PROPER_NOUN_POS).append("|").
            append(COMMON_NOUN_POS).toString();
    
    /** The Peen TB labels for cardinal numbers */
    public static final String CARDINAL_NUMBER_POS = "cd";
    
    /** The Peen TB labels for prepositions */
    public static final String PREPOSITION_POS = "in";
    
    /** The Peen TB labels for determiners */
    public static final String DETERMINER_POS = "dt";
    
    /** The adjective regexp */
    public static final String ADJECTIVE_POS = "(jj|jjr|jjs)";
    
    /** The quotation marks regexp */
    // DON'T NEED IT: USE PUNCTUATION_MARK
    // public static final String QUOTATION_MARK = "(``|'')";
    
    /** The punctuation marks regexp */
    public static final String PUNCTUATION_MARK = new StringBuffer().
            append("(`|``|'|''|\"|\\[|\\]|\\(|\\)|-|").
            append("\\.|,|:|;|!|\\?)").toString();
    
    /** A leading punctuation mark */
    public static final String LEADING_PUNCTUATION =
        new StringBuffer().append("^").append(PUNCTUATION_MARK).toString();
    
    /** A trailing punctuation mark */
    public static final String TRAILING_PUNCTUATION =
        new StringBuffer().append(PUNCTUATION_MARK).append("$").toString();
    
    /** The coordinatig conjuction tag */
    public static String COORDINATING_CONJUCTION_POS = "cc";
    
    /** The designator expression for males */
    public static final String MALE_DESIGNATOR = "(mr|messrs|dr)(\\.)?";

    /** The designator expression for females */
    public static final String FEMALE_DESIGNATOR = "(miss|mrs|ms)(\\.)?";
    
    /** The designator expression for companies */
    public static final String COMPANY_DESIGNATOR =
            "(assoc|bros|co|coop|corp|devel|inc|llc|ltd|s\\.?p\\.?a|soc|s\\.?r\\.?l|s\\.?n\\.?c)\\.?";
    /** The verbs used in copula construction */
/*    public static final String COPULA_VERB = "(am|are|is|was|were|be|been|'s|being|become|becomes|became|becoming)"; // this is ok for MUC, but not for ACE
*/
    public static final String COPULA_VERB = new StringBuffer().append("^").append("(am|are|is|been|'s|being)").append("$").toString();

    public static final String EXPL_VERB = new StringBuffer().append("^").append("(was|is|'s)").append("$").toString();
    /** Modal verbs (do not fire copula feature) */
    public static final String MODAL_VERB = new StringBuffer().append("^").append("(could|might|would)").append("$").toString();

/* nps to be completely excluded from the processing */
//    public static final String NONREF_NP = new StringBuffer().append("^").append("(there)").append("$").toString();

/* for conll-ontonotes */
/*
    public static final String NONREF_NP = new StringBuffer().append("^").append("(there|uh|ah|er|eh|um|etc|example|a lot|a lot .+|all|time|a year|year|the year|others|this time|this week|now|tomorrow|everything|anything|nothing|everyone|everybody|nobody|anybody|someone|somebody|some|something|some people|no one|.+ months|.+ days|.+ years|the first time|one|a little|a little bit|a few|www|the|www.+|http.+|http|.+\\.com|uh .+| ah .+|er .+|um .+)").append("$").toString();
*/
//m.b. add "this","today",tonight","this year" -- check the final results though
    private static final String NONREF_NP_STUPID = "uh|ah|er|eh|um|etc|uh .+| ah .+|er .+|um .+";

    private static final String NONREF_NP_MWE = "example|a lot|a lot .+|the first time|the other hand|one hand|the one hand|a little|a little bit|a few|instance|the contrary|addition|fact";

    private static final String NONREF_NP_WEB = "www|www.+|http.+|http|.+\\.com";

    private static final String NONREF_NP_NONREFPRO = "there|all|others|anything|nothing|nobody|anybody|no one|one";

    private static final String NONREF_NP_NONREFPRO_MISC = "everything|everyone|everybody|someone|somebody|some|something|some people|one .+|all .+|some of .+";

    private static final String NONREF_NP_NONREFTIME = "time|a year|year|the year|.+ months|.+ days|.+ years|now";

    private static final String NONREF_NP_POSSIBLE = "tomorrow|yesterday|today|tonight|this year|this week|this day|this time|this month"; //these days, those days -- cf. TIME

    private static final String NONREF_NP_THIS = "this|that";

    public static final String NONREF_NP = new StringBuffer().append("^(").
            append(NONREF_NP_STUPID).append("|").
            append(NONREF_NP_MWE).append("|").
            append(NONREF_NP_WEB).append("|").
            append(NONREF_NP_NONREFPRO).append("|").
//            append(NONREF_NP_NONREFPRO_MISC).append("|").
            append(NONREF_NP_NONREFTIME).append("|").
//            append(NONREF_NP_POSSIBLE).append("|").
             append(NONREF_NP_THIS).append("|").
            append(")$").toString();

/* ACE-specific: heads of nominals that corefer with NE-countries due to the strange annotation guidelines */
    public static final String ACE_COUNTRY = new StringBuffer().append("^").append("(nation|government|people)").append("$").toString();

}
