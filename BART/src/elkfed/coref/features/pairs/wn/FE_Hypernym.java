/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs.wn;

import elkfed.coref.PairFeatureExtractor;
import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.PointerTarget;
import edu.brandeis.cs.steele.wn.PointerType;
import edu.brandeis.cs.steele.wn.Synset;
import elkfed.coref.PairInstance;
import elkfed.knowledge.WNInterface;
import elkfed.ml.FeatureDescription;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 
 *
 * @author kepa.rodriguez
 */
public class FE_Hypernym {

    public static final int NOT_FOUND = 0;
    public static final int IS_SYNONYM = 1;
    public static final int IS_HYPERNYM = 2;

    public static int isSynonymHypernym(String head1, String head2) {
        WNInterface wn = WNInterface.getInstance();
        IndexWord word1 = wn.lookupNoun(head1);
        IndexWord word2 = wn.lookupNoun(head2);
        Set<Synset> synsets1 = new HashSet<Synset>();

        if (word1 == null || word2 == null){
            return NOT_FOUND;
        } else {
        
        for (Synset s : word1.getSenses()) {
            synsets1.add(s);
        }

        
        Set<Synset> synsets2 = new HashSet<Synset>();
        for (Synset s : word2.getSenses()) {
            if (synsets1.contains(s)) {
                return IS_SYNONYM;
            }
            synsets2.add(s);
        }
        HashSet<PointerTarget> visited = new HashSet<PointerTarget>();
        for (Synset s : word2.getSenses()) {
            if (wn.reachable(s, PointerType.HYPERNYM, synsets1, visited)) {
                return IS_HYPERNYM;
            }
        }
        }
        return NOT_FOUND;
    }

    public static void main(String[] args) {
        String[] words1 = {"human", "man", "woman", "animal", "xmixrw"};
        String[] words2 = {"worker", "cow", "tree", "beast"};
        for (String word1 : words1) {
            for (String word2 : words2) {
                System.out.format("%s - %s: %s\n",
                        word1, word2, isSynonymHypernym(word1, word2));
                System.out.format("%s - %s: %s\n",
                        word2, word1, isSynonymHypernym(word2, word1));
            }
        }
    }
}

