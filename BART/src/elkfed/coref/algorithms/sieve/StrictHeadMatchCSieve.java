package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

/**
 * This Sieve links a Mention to an antecedent,
 * if its head word matches any head word of the antecedents Discourse Entity.  
 * Compared to {@link StrictHeadMatchASieve} it drops the word inclusion requirement,
 * but retains the compatible modifiers and I-within-I requirements
 * 
 * @see #entityHeadMatch(PairInstance)
 * @see #compatibleModifiers(PairInstance)
 * @see #IWithinI(PairInstance)
 * 
 * @author Julian
 *
 */
public class StrictHeadMatchCSieve extends Sieve {
		
	public StrictHeadMatchCSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "StrictHeadMatchCSieve";
	}
	
	int runSieve(Mention mention) {
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;

		for (int idx = 0; idx < mention_idx; idx++) {
			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(mention, ante);
			if (entityHeadMatch(pair) && compatibleModifiers(pair)
					&& !(IWithinI(pair))) {
				ante_idx = idx;
			}
		}
		return ante_idx;
	}
}
