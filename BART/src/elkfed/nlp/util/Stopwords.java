/*
 * Stopwords.java
 *
 * Created on July 31, 2007, 10:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.nlp.util;

import elkfed.config.ConfigProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author yannick
 */
public class Stopwords {
    private static Stopwords _instance;
    private final Set<String> _stopwords;
    
    public static Stopwords getInstance()
    {
        if (_instance==null)
        {
            try {
                _instance=new Stopwords();
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("File not found",e);
            }
            catch (IOException e)
            {
                throw new RuntimeException("IOException",e);
            }            
        }
        return _instance;
    }
    /** Creates a new instance of Stopwords */
    private Stopwords()
        throws FileNotFoundException, IOException
    {
        BufferedReader br=ConfigProperties.getInstance().openStopList();
        _stopwords=new HashSet<String>();
        String line;
        while ((line=br.readLine())!=null)
        {
            _stopwords.add(line);
        }
    }
    
    public boolean contains(String str)
    {
        return _stopwords.contains(str);
    }
    
    public static void main(String[] args)
    {
        Stopwords inst=getInstance();
        String[] testWords={"about","enough","tezguino"};
        for (String w: testWords)
        {
            System.out.println(w+" -> "+inst.contains(w));
        }
    }
}
