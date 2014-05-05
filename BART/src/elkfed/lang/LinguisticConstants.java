package elkfed.lang;

import java.util.regex.Pattern;

/** This is the repository for generic constants used in
 *  linguistic processing.
 *
 * @author samuel
 */
public class LinguisticConstants
{   
    /** Just a static repository... */
    public LinguisticConstants() {}
    
    /** The punctuation marks regexp */
    public static final String PUNCTUATION_MARK = new StringBuffer().
            append("(`|``|'|''|\"|\\[|\\]|\\(|\\)|-|").
            append("\\.|,|:|;|!|\\?)").toString();
}
