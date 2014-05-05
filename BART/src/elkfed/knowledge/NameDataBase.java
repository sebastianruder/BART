/*
 * NameDataBase.java
 *
 * Created on July 12, 2007, 6:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import elkfed.config.ConfigProperties;
import elkfed.mmax.minidisc.Markable;
import elkfed.nlp.util.Gender;

/**
 *
 * @author vae2101
 */
public class NameDataBase
{
    
    /** Creates a new instance of NameDataBase */
    private NameDataBase() {
        nameDataBase = getNameDB();
        wikiNameDB = getWikiDB();
    }

    /** The database of first names */
    private final Map<String,Gender> nameDataBase;
    private final Map<String,Gender> wikiNameDB;

    private static NameDataBase _instance;
    
    public static NameDataBase getInstance(){
    if(_instance == null)
        _instance = new NameDataBase();
        
        return _instance;
    }

    /** return the gender associated with the given (first) name
     *  or <i>null</i> if it's not in the map.
     */
    public Gender lookup(Markable markable) {
        Gender g = null;
        String mStr = "";
        for (String token : markable.getDiscourseElements()) {
            token = token.toLowerCase();
            mStr += token + "_";
        }
        mStr = mStr.replaceAll("_$", "");
        
        for (String token : markable.getDiscourseElements()) {
            if (Character.isUpperCase(token.charAt(0))) {
                g=nameDataBase.get(token.toLowerCase());
                if (g!=null) {
                    //System.out.println("SIMPLE: " + mStr + " " + token + " " +g.toString());
                    return g;
                }
            }
        }
        SemanticClass semclass = null;
        if (markable.getAttributeValue("type").equals("enamex")) {
            semclass=SemanticClass.getFromString(
                    markable.getAttributeValue("label"));
        }
        if (semclass == SemanticClass.PERSON)
        {
            g = wikiNameDB.get(mStr);
        }



        
        if (!ConfigProperties.getInstance().getDbgPrint()) return g;

        if (g != null)
        {
            System.out.println("WIKI: " + mStr + " " + g.toString());
        }
        else
        {
            //System.out.println("WIKI: " + mStr + " U");
        }
        return g;
    }
        
    /** return the gender associated with the given (first) name
     *  or <i>null</i> if it's not in the map.
     */
    public Gender lookup(String tok)
    {
        return nameDataBase.get(tok);
    }

    /** Loads the database of common first names 
     *
     */
    private Map<String,Gender> getNameDB()
    {
        final Map<String,Gender> db = new HashMap<String,Gender>();
        try
        {
            // the MALE db file reading
            BufferedReader readFile =
                    ConfigProperties.getInstance().openMaleNamesDB();
            while (readFile.ready())
            {
                db.put(readFile.readLine().split("\\s")[0].toLowerCase(), Gender.MALE);
            }
            readFile.close();
            // the FEMALE db file reading
            readFile =
                    ConfigProperties.getInstance().openFemaleNamesDB();
            while (readFile.ready())
            {
                db.put(readFile.readLine().split("\\s")[0].toLowerCase(), Gender.FEMALE);
            }
            readFile.close();

        }
        catch (IOException ioe) { ioe.printStackTrace(); }
        return db;
    }
    
    /*
     * This uses a name database extracted from Wikipedia
     */
    private Map<String,Gender> getWikiDB()
    {  
//        WikiSimilarityFactory.getInstance().setCaching(true);
        final Map<String,Gender> db = new HashMap<String,Gender>();
        try
        {
            BufferedReader readFile =
                    ConfigProperties.getInstance().openBergsmaGenderDB();
            while (readFile.ready())
            {
                String line = readFile.readLine().toLowerCase();
                int tabIndex=line.indexOf('\t');
                String name = line.substring(0,tabIndex);
                String genderTag = line.substring(tabIndex+1);
                
                if (genderTag.equals("m"))
                {
                    db.put(name, Gender.MALE);
                }
                else if (genderTag.equals("f"))
                {
                    db.put(name, Gender.FEMALE);
                }
//                else if (genderTag.equals("n"))
//                {
//                    //System.out.println(name + " " + genderTag);
//                    db.put(name, Gender.NEUTRAL);
//                }
//                else if (genderTag.equals("p"))
//                {
//                    //System.out.println(name + " " + genderTag);
//                    db.put(name, Gender.PLURAL);
//                }

            }
            readFile.close();
            //out.close();
        }
        catch (IOException ioe) { ioe.printStackTrace(); }
        return db;
    }
    
    /** tests NameDataBase by running it on a small set of names */
    public static void main(String[] args)
    {
        NameDataBase ndb=getInstance();
        String[] test_strings={"Peter","Sally","John","Emily","Zaphod"};
        for (String s: test_strings)
        {
            System.out.format("%s -> %s\n", s, ndb.lookup(s));
        }
    }
}
