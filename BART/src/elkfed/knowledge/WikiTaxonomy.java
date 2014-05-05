/*
 * WikiTaxonomy.java
 *
 * Created on August 16, 2007, 5:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

import elkfed.config.ConfigProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

//import edu.mit.jwi.dict.Dictionary;
//import edu.mit.jwi.dict.IDictionary;
//import edu.mit.jwi.item.IIndexWord;
//import edu.mit.jwi.item.IWord;
//import edu.mit.jwi.item.PartOfSpeech;

/** Taxonomy isa pairs from our AAAI'07 paperz
 *
 * @author ponzo
 */
public class WikiTaxonomy extends HashMap<String,Set<String>>
{
    public WikiTaxonomy(File... dataFiles)
    { loadFile(dataFiles); }

    private void loadFile(File... dataFiles)
    {
        for (File data : dataFiles)
        {
            try
            {
                final BufferedReader readFile = new BufferedReader(new FileReader(data));
                while (readFile.ready())
                {
                    final String[] isaPair = forceSingular(readFile.readLine().split(" --- "));
                    final String key = isaPair[0];
                    final String value = isaPair[1];
                    if (!containsKey(key))
                    { put(key, new HashSet<String>()); }
                    if (!key.equalsIgnoreCase(value))
                    { get(key).add(value); }
                }
                readFile.close();
            }
            catch (IOException ioe)
            { ioe.printStackTrace(); }
        }
    }

    public boolean contains(String term1, String term2)
    {
        return
            isa(term1,term2) || isa(term2,term1);
    }

    public boolean isa(String term1, String term2)
    {
        if (containsKey(term1))
        { return setContains(get(term1),term2); }
        return false;
    }
    
    private boolean setContains(Set<String> set, String string)
    {
        for (String elem : set)
        {
            if (elem.equalsIgnoreCase(string))
            { return true; }
        }
        return false;
    }
    
    private String[] forceSingular(String[] pair)
    {
        String term1 = pair[0].trim();
        String term2 = pair[1].trim();
        if (
                (!term1.toLowerCase().contains("people"))
            &&
                term2.equalsIgnoreCase("people")
            &&
                (!term1.endsWith("s"))
        )
        { return new String[]{term1, "person"}; }
        else
        { return new String[]{term1, term2}; }
    }
    
//    public void doWordNetExpansion()
//    {        
//        Logger logger = Logger.getAnonymousLogger();
//        logger.info("Expanding the Wikipedia taxonomy pairs, please wait ...");
//        URL url = null; 
//        try{ url = new URL("file", null, ConfigProperties.getInstance().getWNHome()); }  
//        catch(MalformedURLException e){ e.printStackTrace(); } 
//        if(url == null) return; 
//        logger.info("loading WN from " + url);
//        
//        IDictionary dict = new Dictionary(url); 
//        dict.open();
//        
//        final Map<String,Set<String>> tmpCache = new HashMap<String,Set<String>>();
//        
//        for (String key : keySet())
//        {
//            for (String isa : get(key))
//            {
//                if (!tmpCache.containsKey(isa))
//                { 
//                    Set<String> expansion = new HashSet<String>();
//                    final IIndexWord idxWord = dict.getIndexWord(isa, PartOfSpeech.NOUN);
//                    if (idxWord != null)
//                    {
//                        final IWord[] wordID = 
//                            dict.getSynset(idxWord.getWordIDs()[0].getSynsetID()).getWords();
//                        for (IWord w : wordID) {
//                            expansion.add(w.getLemma().replaceAll("_", " "));
//                        }
//                    }
//                    tmpCache.put(isa,expansion);
//                }
//            }
//        }
//        
//        for (String key : keySet())
//        {
//            Set<String> expansions = new HashSet<String>();
//            for (String isa : get(key))
//            { expansions.addAll(tmpCache.get(isa)); }
//            get(key).addAll(expansions);
//        }
//        logger.info("Wikipedia taxonomy pairs successfully expanded");
//    }
}
