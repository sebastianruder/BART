package elkfed.coref.algorithms.sieve;

import java.util.List;

/**
 * This sieve marks two mentions headed by proper nouns as coreferent if they have the same head word and satisfy the following constraints:
 * Not i-within-i
 * No location mismatches - the modifiers of two mentions cannot contain different location named entities, other proper nouns, or spatial modifiers.
 * No numeric mismatches - the second mention cannot have a number that does not appear in the antecedent, e.g., [people] and [around 200 people] are not coreferent.
 *  
 * @author Xenia
 *  
 */

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

public class ProperHeadNounMatchSieve extends Sieve {

	public ProperHeadNounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "ProperHeadNounMatchSieve";
	}
	
	public int runSieve(Mention mention){
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			pair = new PairInstance(mention, mentions.get(idx));
			
			if (	pair.getAnaphor().getHeadPOS().equalsIgnoreCase("ne") &&  // head words must be proper nouns
					pair.getAntecedent().getHeadPOS().equalsIgnoreCase("ne") && 
					// mention and antecedent's head word must be the same
					pair.getAnaphor().getDiscourseEntity().getHeadsString().equals(pair.getAntecedent().getDiscourseEntity().getHeadsString())){
				if (	!IWithinI(pair) &&  		// check if i-iwithin-i
						noNumericMismatch(pair) && 	// check if numeric mismatch
						noLocationMismatch(pair)){ 	// check if location mismatch
							ante_idx = idx;
				}
			}
		}	
		return ante_idx;
	}
}
