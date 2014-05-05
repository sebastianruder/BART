/*
 * FE_MentionAlias.java
 *
 * Created on July 11, 2007, 5:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.mentions.Mention;
import elkfed.util.DateParser;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import elkfed.coref.*;
import elkfed.ml.*;
import static elkfed.lang.EnglishLinguisticConstants.*;


/**
 * Feature used to determine whether two markables refer to the same entity 
 * using different notation (acronyms, shorthands,etc.)
 * 
 * Either T/F
 * 
 * @author vae2101
 */
public class FE_Alias implements PairFeatureExtractor
{
    public static final FeatureDescription<Boolean> FD_IS_ALIAS=
        new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Alias");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_ALIAS);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_ALIAS,getAlias(inst));
    }
 
    public static boolean getAlias(PairInstance inst)
    {        
        final Date antDate = 
                DateParser.getInstance().parse(inst.getAntecedent().getMarkableString());
        final Date anaDate =
                DateParser.getInstance().parse(inst.getAnaphor().getMarkableString());
        
        // starts with dates
        if ((antDate != null) && (anaDate != null))
                return compareDate(antDate, anaDate);

        // if both markables are not a named entities, set false
        // right away...
        if (!inst.getAntecedent().isEnamex()) return false;
        if (!inst.getAnaphor().isEnamex()) return false;

        // else if they are not of the same named entity type, again
        // set false straight away...
        if (!(inst.getAntecedent().getEnamexType().equals(
               inst.getAnaphor().getEnamexType())))
            return false; 

        // ok, we made it so far, now we have to check class by class
        final String enamexClass = inst.getAntecedent().getEnamexType(); 
/*
            final String ne1=inst.getAntecedent().getMarkableString();
            final String ne2=inst.getAnaphor().getMarkableString();
            final String enamexClass2 = inst.getAnaphor().getEnamexType(); 

System.err.println("Compare 2 Nes: " + ne1 + " ("+enamexClass+"), " + ne2 +
" (" + enamexClass2 +")");
*/

            if (enamexClass.startsWith("per"))
                return compareName(inst);
            if (enamexClass.startsWith("org"))
                return compareOrg(inst);
            if (enamexClass.startsWith("loc")|| 
                enamexClass.startsWith("gpe") ||
                enamexClass.startsWith("gsp"))
                return compareLoc(inst);

   // for the others just compare the strings
            return inst.getAntecedent().getMarkableString().toLowerCase().
                equalsIgnoreCase(inst.getAnaphor().getMarkableString());

    }    
    /** Uses a date parser to normalize, extract and compare two date String */
    private static boolean compareDate(Date date1, Date date2)
    {
        final GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTime(date1);
        final GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTime(date2);
        
        return
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
         &&
           (
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
            ||
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
        );
    }
    
    /** Uses a date parser to normalize, extract and compare two date String */
    private static boolean compareName(PairInstance inst)
    {

     if (inst.getAntecedent().getMarkableString().toLowerCase().equalsIgnoreCase(inst.getAnaphor().getMarkableString()))
                    return true;                 

     if (inst.getAntecedent().isCoord()) return false;
     if (inst.getAnaphor().isCoord()) return false;

     final String[] antecedentTokens = inst.getAntecedent().getMarkable().getDiscourseElements();
     final String[] anaphoraTokens = inst.getAnaphor().getMarkable().getDiscourseElements();
     if (antecedentTokens[antecedentTokens.length-1].
         equalsIgnoreCase(anaphoraTokens[anaphoraTokens.length-1]))
            return true; 
     return false; 
    }
    
    /** Just an alias */
    private static boolean compareOrg(PairInstance inst)
    {
        // first check for an abbreviation (incl. direct match)
        if (isAbbreviation(inst))        
          return true; 

     if (inst.getAntecedent().isCoord()) return false;
     if (inst.getAnaphor().isCoord()) return false;

     if (orgStartsWith(
               inst.getAntecedent().getMarkableString().toLowerCase(),
               inst.getAnaphor().getMarkableString().toLowerCase()))
          return true; 
        return false;
    }
    
    /** From Soon et Al.(2001): For organization names, the alias function also
     *  checks for acronym match such as IBM and International Business Machines
     *  Corp. In this case, the longer string is chosen to be the one that is
     *  converted into the acronym form. The first step is to remove all
     *  postmodifiers such as Corp. and Ltd. Then, the acronym function
     *  considers each word in turn, and if the first letter is capitalized, it
     *  is used to form the acronym. Two variations of the acronyms are
     *  produced: one with a period after each letter, and one without.
     *
     *  @return whether a match was found
     */
    private static boolean isAbbreviation(PairInstance inst)
    {

        // first check they differ just by periods --- i.e. "IBM" and "I.B.M."
        if (    
            (inst.getAntecedent().getMarkableString().replaceAll("\\.", "").
                        equalsIgnoreCase(inst.getAnaphor().getMarkableString())
                 ||
             inst.getAnaphor().getMarkableString().replaceAll("\\.", "").
                   equalsIgnoreCase(inst.getAntecedent().getMarkableString())))
         
            return true; 
        
        return MatchAcronyms(inst.getAntecedent(),inst.getAnaphor());
}
private static boolean MatchAcronyms(final Mention ante, final Mention ana) { 

// ante should be shorter than ana, swap otherwise
  if (ante.getMarkableString().length()>ana.getMarkableString().length()) 
     return MatchAcronyms(ana,ante);

   final String antestr=ante.getMarkableString();
   final String[] acronyms = getAcronym(ana);

    for (int i=0; i<acronyms.length;i++) {
      if (acronyms[i].equalsIgnoreCase(antestr)) return true;
    }
    return false; 
}

    /** For a location to be an alias of another, either one is an abbreviation of
     *  the other --- i.e. "NJ" and "N.J." --- or one starts with the other 
     *  --- i.e. "California" and "Calif." or "Washington" and "Washington, D.C.".
     */
    private static boolean compareLoc(PairInstance inst)
    {  
/*
String ne1=inst.getAntecedent().getMarkableString().toLowerCase();
String ne2=inst.getAnaphor().getMarkableString().toLowerCase();
System.err.println("Compareloc: " + ne1 + " -- " + ne2);
*/
        if (isAbbreviation(inst))
          return true; 
//System.err.println("isabbrev failed");
        // no luck: check whether one starts with the other
        if (startsWith(
               inst.getAntecedent().getMarkableString().toLowerCase(),
               inst.getAnaphor().getMarkableString().toLowerCase()))
           return true; 
// this one is for Italian and is temporary (matches "New York" - "York")
//System.err.println("startswith failed");
        if (endsWithLoc(
               inst.getAntecedent().getMarkableString().toLowerCase(),
               inst.getAnaphor().getMarkableString().toLowerCase()))
           return true; 
//System.err.println("endswith failed");

        return false;
    }

    /** Check whether one LOC NE starts with the other */

    private static boolean startsWith(String ne1, String ne2)
    { return startsWith(ne1, ne2, "\\."); }
    
    /** Check whether one ORG NE starts with the other */
    private static boolean orgStartsWith(String ne1, String ne2)
    { 

       return startsWith(ne1, ne2, " " + COMPANY_DESIGNATOR + "$"); 
    }
    
    /** Check whether one ORG NE starts with the other */
    private static boolean startsWith(String ne1, String ne2, String toRemove)
    {
       ne1=ne1.replaceAll(toRemove,"");
       ne2=ne2.replaceAll(toRemove,"");
       return ne1.startsWith(ne2) || ne2.startsWith(ne1);
    }
// NB: Italian!
    private static boolean endsWithLoc(String ne1, String ne2)
    {
       ne1=ne1.replaceAll("\\.","");
       ne2=ne2.replaceAll("\\.","");
       return ne1.endsWith(" di "+ne2) || ne2.endsWith(" di "+ne1);
    }
    
    /** Creates 4 acronyms:
     *
     *  1. all tokens, w/o company designator --- i.e. Home Depot Inc. / Home Depot
     *  2. all caps w/o period --- Intelligent Business Machines / IBM
     *  3. all caps w period --- Intelligent Business Machines / I.B.M.
     *  4. all tokens, w/o company designator -- for Italian mainly
     */
    private static String[] getAcronym(final Mention mention)
    {
        final StringBuffer firstAcronym = new StringBuffer();
        final StringBuffer secondAcronym = new StringBuffer();
        final StringBuffer thirdAcronym = new StringBuffer();
        final StringBuffer fourthAcronym = new StringBuffer();
        
        final String[] tokens = mention.getMarkableString().split(" ");
        
        if (tokens.length == 0)
		throw new RuntimeException("Tokens empty:"+mention.getMarkableString()+" "+mention.toString());
	for (int token = 0; token < tokens.length; token++)
        {
            if (!tokens[token].toLowerCase().matches(COMPANY_DESIGNATOR))
            {
                firstAcronym.append(tokens[token]).append(" ");
                fourthAcronym.append(tokens[token].substring(0, 1));
                if (Character.isUpperCase(tokens[token].charAt(0)))
                {
                    secondAcronym.append(tokens[token].substring(0, 1));
                    thirdAcronym.append(tokens[token].substring(0, 1)).append(".");
                }
                // handle ampersand...
                else if (tokens[token].equals("&"))
                {
                    secondAcronym.append(" & ");
                    thirdAcronym.append(" & ");
                }
            }
        }
	if (firstAcronym.length()==0)
 		System.err.println("firstAcronym empty:"+mention.getMarkableString()+" "+mention.toString());
	else
                firstAcronym.deleteCharAt(firstAcronym.length()-1);
        final String[] acronyms = {
                                    firstAcronym.toString(),
                                    secondAcronym.toString(),
                                    thirdAcronym.toString(),
                                    fourthAcronym.toString()
                                  };
        return acronyms;
    }
}
