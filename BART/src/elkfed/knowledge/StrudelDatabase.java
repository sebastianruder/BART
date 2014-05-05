/*
 * StrudelDatabase.java
 *
 * Created on Oct 13, 2009, 11:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.knowledge;

import elkfed.config.ConfigProperties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kepa
 */
public class StrudelDatabase {

    /** Creates a new instance of NameDataBase */
    public StrudelDatabase() {
        strudelDB = getStrudelDB();
    }
    /** The database of word pairs and symilarity */
    private final Map<Map<String, String>, Double> strudelDB;
    private static StrudelDatabase _instance;

    public static StrudelDatabase getInstance() {
        if (_instance == null) {
            _instance = new StrudelDatabase();
        }
        return _instance;
    }

    public double consultStrudelDB(String w1, String w2) {
        Double pairScore = null;
        Map<String, String> wordPair = new HashMap<String, String>();
        wordPair.put(w1, w2);
        if (strudelDB.containsKey(wordPair)) {
            pairScore = strudelDB.get(wordPair);
            System.out.println("DB-yes: " + w1 + " " + w2 + " " + pairScore);
        } else {
            pairScore = 0.0d;
            System.out.println("DB-no: " + w1 + " " + w2);
        }
        return pairScore;
    }

    public Map<Map<String, String>, Double> getStrudelDB() {

        double score;
        String word1;
        String word2;
        String scoreStr;

        final Map<Map<String, String>, Double> db = new HashMap<Map<String, String>, Double>();
        try {
            BufferedReader readFile =
                    new BufferedReader(new FileReader(ConfigProperties.getInstance().getStrudelDB()));
            while (readFile.ready()) {
                String line = readFile.readLine();
                word1 = line.split("\\t")[0];
                word2 = line.split("\\t")[1];
                scoreStr = line.split("\\t")[2];
                score = Double.valueOf(scoreStr).doubleValue();

                Map<String, String> words = new HashMap<String, String>();
                words.put(word1, word2);
                db.put(words, score);
            }
            readFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return db;
    }


    /** tests NameDataBase by running it on a small set of names */
    public static void main(String[] args) {
        StrudelDatabase ndb = getInstance();
        String[] test_strings = {};
        for (String s : test_strings) {
            //      System.out.format("%s -> %s\n", s, ndb.lookup(s));
        }
    }
}
