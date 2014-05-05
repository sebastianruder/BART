/*
 * WNInterface.java
 *
 * Created on July 12, 2007, 6:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

import java.util.Map;

import edu.brandeis.cs.steele.wn.DictionaryDatabase;
import edu.brandeis.cs.steele.wn.FileBackedDictionary;
import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.Synset;
import edu.brandeis.cs.steele.wn.PointerTarget;
import edu.brandeis.cs.steele.wn.PointerType;
import edu.brandeis.cs.steele.wn.POS;
import elkfed.config.ConfigProperties;
import elkfed.knowledge.SemanticClass;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**Given a noun, uses WordNet to determine its semantic class.
 *
 * @author vae2101
 */
public class WNInterface {
    private static WNInterface _instance;
    
    /** The WN database used to determine the semantic class */
    private final DictionaryDatabase dictionary;
    
    /** The mapping between WN synsets and semantic classes */
    private final Map<PointerTarget,SemanticClass> semClassMapping; 
    /** Creates a new instance of WNInterface */
    private WNInterface() {
        dictionary = new FileBackedDictionary(
                new File(ConfigProperties.getInstance().getRoot(),
                    "wordnet").getAbsolutePath());
        semClassMapping = SemanticClass.getSemClassMapping(dictionary);
    }
    public static WNInterface getInstance()
    {
        if (_instance==null)
            _instance=new WNInterface();
        return _instance;
    }
    
    public DictionaryDatabase getDictionary() {
        return dictionary;
    }

    public IndexWord lookupNoun(String noun) {
        return dictionary.lookupIndexWord(POS.NOUN, noun);
    }

    /** Gets the semantic class of a noun */
    public SemanticClass getSemanticClass(final String noun)
    {
        // we start by looking at the lexical entry
        final IndexWord word = lookupNoun(noun);
        // if there is no word entry, return the "unknown" class
        if (word == null)
        { return SemanticClass.UNKNOWN; }
        else
        {
            // else the noun is mapped to its first WN sense
            final Synset sense = dictionary.lookupIndexWord(POS.NOUN, noun).getSenses()[0];
            // traverse the net until you find a synset one the
            // available semantic classes is mapped to
            return traverse(sense, PointerType.HYPERNYM);
        }
    }
    
        /** Traverses the net as long as one of the system's semantic
     *  classes is found.
     */
    private SemanticClass traverse(final PointerTarget sense, final PointerType hypernym)
    {   
        // we have a match! return the corresponding semantic class
        if (semClassMapping.containsKey(sense))
        { return semClassMapping.get(sense); }
        else
        {
            final PointerTarget[] parents = sense.getTargets(hypernym);
            if (parents == null)
            {
                // no more hypernyms, return "unknown"
                return SemanticClass.UNKNOWN;
            }
            else
            {
                // keep on traversing each parent recursively
                for (PointerTarget parent : sense.getTargets(hypernym))
                { return traverse(parent, hypernym); }
            }
        }
        // if we made it so far, return "unknown"
        return SemanticClass.UNKNOWN;
    }

    public boolean reachable(final PointerTarget sense, final PointerType hypernym,
            final Set<? extends PointerTarget> destination) {
        return reachable(sense,hypernym,destination,
                new HashSet<PointerTarget>());
    }

    public boolean reachable(final PointerTarget sense, final PointerType hypernym,
            final Set<? extends PointerTarget> destination, Set<PointerTarget> visited)
    {
        // we have a match! return the corresponding semantic class
        if (destination.contains(sense)) {
            return true;
        }
        else
        {
            final PointerTarget[] parents = sense.getTargets(hypernym);
            if (parents == null)
            {
                return false;
            }
            else
            {
                // keep on traversing each parent recursively
                for (PointerTarget parent : sense.getTargets(hypernym))
                {
                    if (!visited.contains(parent)) {
                        visited.add(parent);
                        if (reachable(parent,hypernym,destination,visited)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** tests WNInterface by running it on a small set of nouns */
    public static void main(String[] args)
    {
        WNInterface wn=getInstance();
        String[] test_strings={"lawyer","dog","company", "river","capital","city"};
        for (String s: test_strings)
        {
            System.out.format("%s -> %s\n", s, wn.getSemanticClass(s));
        }
    }
}
