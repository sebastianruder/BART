package elkfed.lang;

/** This is the repository for the constants used in generic
 *  linguistic processing.
 *
 * @author samuel
 */
public class GermanLinguisticConstants extends LinguisticConstants
{   
    /** Just a static repository... */
    public GermanLinguisticConstants() {}
    
    /** Days of the week, months, years; shouldn't be matched with [Relaxed]StringMatch */
    public static final String DAYS_MONTHS_YEAR = "(Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonntag|Januar|Februar|M�rz|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember|Jahr)";
    
    /** The Saxon genitive */
    public static final String SAXON_GENITIVE = "'s";

    /** The Saxon genitive */
    public static final String EXPLITIVE = "es";

    /** The definite article */
    public static final String DEF_ARTICLE = "(der|die|das|des|dem|den)";
    
    /** The indefinite article */
    public static final String INDEF_ARTICLE = "(ein|eine|einem|einer|eines|einen)";

    /** The article regexp */
    public static final String ARTICLE = "(" + DEF_ARTICLE + "|" + INDEF_ARTICLE + ")";

    /** A leading article */
    public static final String LEADING_ARTICLE =
        new StringBuffer().append("^").append(ARTICLE).toString();
    
    /** Relative pronoun regexp */
    public static final String RELATIVE_PRONOUN = "(der|die|das|dessen|deren|dem|den|denen|welcher|welche|welches)";
 

    public static final String FIRSTPERSON_SG_PRONOUNS = "(ich|mich|mir|mein|meine|meiner|meinen|meinem|meines)";
    		
    public static final String SECONDPERSON_SG_PRONOUNS = "(du|dich|dir|dein|deine|deiner|deinen|deinem|deines)";
    
    public static final String THIRDPERSON_SG_PRONOUNS = "(sich|er|sie|es|ihn|sie|es|ihm|ihr|es|sein|seine|seiner|seinen|seinem|seines|ihr|ihre|ihren|ihrem|ihres)";
    
    public static final String FIRSTPERSON_PL_PRONOUNS = "(wir|uns|unser|unsere|unserer|unseren|unserem|unseres)";
    
    public static final String SECONDPERSON_PL_PRONOUNS = "(ihr|euch|euer|eure|eurer|euren|eurem|eures)";
    
    public static final String THIRDPERSON_PL_PRONOUNS = "(sie|ihnen|ihre|ihrer|ihren|ihrem|ihres|sich)";
    
    public static final String AMBIGOUS_SG_PL_PRONOUNS = "(sie|sich|ihre|ihrer|ihren|ihrem|ihr)";
    
    
   
    /** The STTS labels for common noun */
    public static final String COMMON_NOUN_POS = "NN"; 
    
    /** The STTS labels for proper noun */
    public static final String PROPER_NOUN_POS = "NE";
    
    /** The STTS labels for noun */
    public static final String NOUN = new StringBuffer().
            append(PROPER_NOUN_POS).append("|").
            append(COMMON_NOUN_POS).toString();
    
    /** The STTS labels for cardinal numbers */
    public static final String CARDINAL_NUMBER_POS = "CARD";
    
    /** The STTS labels for prepositions */
    public static final String PREPOSITION_POS = "PREP";

    /** The STTS labels for demonstratives */
    public static final String DEMONSTRATIVE_POS = "PDS|PDAT";
    
    /** The STTS labels for determiners */
    public static final String DETERMINER_POS = "ART";
    
    /** The coordinatig conjuction tag */
    public static String COORDINATING_CONJUCTION_POS = "KON|CJ";
    
    /** The designator expression for males */
    public static final String MALE_DESIGNATOR = "(Herr|Hr\\.)?";

    /** The designator expression for females */
    public static final String FEMALE_DESIGNATOR = "(Frau|Fr\\.)?";

	
}
