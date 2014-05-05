package elkfed.util;

import java.util.regex.Pattern;

/** Util methods for Strings
 *
 * @author ponzo
 */
public class Strings {
    
    /** The default pad */
    private static final int DEFAULT_PAD = 15;
    
    /** Creates a new instance of Strings */
    private Strings() { }
        
    public static String toPaddedString(final Object obj)
    { return toPaddedString(obj, DEFAULT_PAD); }
    
    public static String toPaddedString(final Object obj, final int padding)
    {
        if (obj.toString().length() >= padding)
        { return obj.toString().substring(0, padding); }
        else
        {
            final StringBuffer buffer = new StringBuffer();
            int pad = obj.toString().length();
            while (pad++ < padding)
            { buffer.append(" "); } 
            buffer.append(obj.toString());
            return buffer.toString();
        }
    }
    
    public static String getNTimes(final char c, final int times)
    {
        int timesSoFar = 0;
        final StringBuffer buffer = new StringBuffer(times);
        while (timesSoFar++ < times)
        { buffer.append(c); }
        return buffer.toString();
    }
    
    
    static Pattern boring=Pattern.compile("^[\\w\\s]*$");
    public static String sanitize_unicode(String s) {
    	if (boring.matcher(s).matches()) {
    		return s;
    	}
    	StringBuffer buf=new StringBuffer();
    	for (int i=0; i<s.length(); i++) {
    		char c=s.charAt(i);
    		if (c>='\0' && c<='\u00ff') {
    			buf.append(c);
    		} else {
    			switch(c) {
    			case '\u2010':
    			case '\u2012':
    			case '\u2013':
    			case '\u2014':
    			case '\u2015':
    				buf.append('-');
    				break;
    			case '\u2018':
    			case '\u2019':
    			case '\u201a':
    			case '\u2032':
    			case '\u02b9':
    			case '\u2039':
    			case '\u203a':
    				buf.append('\'');
    				break;
    			case '\u201c':
    			case '\u201d':
    			case '\u201e':
    			case '\u2033':
    			case '\u02ba':
    				buf.append('"');
    				break;
    			default:
    				int tp=Character.getType(c);
    				if (tp==Character.UPPERCASE_LETTER) {
    					buf.append('X');
    				} else if (tp==Character.LOWERCASE_LETTER) {
    					buf.append('x');
    				} else if (tp==Character.CONNECTOR_PUNCTUATION) {
    					buf.append('_');
    				} else if (tp==Character.SPACE_SEPARATOR) {
    					buf.append(' ');
    				} else {
    					buf.append('*');
    				}
    			}
    		}
    		
    	}
    	s=s.replace('\u2012', '-');
    	s=s.replace('\u2013', '-');
    	return s;
    }
}
