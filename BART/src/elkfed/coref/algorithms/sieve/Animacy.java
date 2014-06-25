package elkfed.coref.algorithms.sieve;

import java.util.Arrays;
import java.util.Set;

import edu.stanford.nlp.util.Generics;

public class Animacy {
	
	public final Set<String> inanimatePronouns = Generics.newHashSet(Arrays.asList(new String[]{ "it", "itself", "its", "where", "when" }));
	public final Set<String> animatePronouns = Generics.newHashSet(Arrays.asList(new String[]{ "i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our", "you", "yourself", "yours", "your", "yourselves", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves", "who", "whom", "whose" }));

}