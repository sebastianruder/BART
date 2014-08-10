package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

/**
 * This Sieve links a Mention to an antecedent, if they both are named entities
 * with the same semantic class and the mentions head word matches any word in
 * the antecedents Discourse Entity. Additionally it needs to meet the word
 * inclusion and the not I-within-I requirements.
 * 
 * 
 * @see #relaxedEntityHeadMatch(PairInstance)
 * @see #properNameAgreement(PairInstance)
 * @see #wordInclusion(PairInstance)
 * @see #IWithinI(PairInstance)
 * 
 * @author Julian
 * 
 */
public class RelaxedHeadMatchSieve extends Sieve {

	public RelaxedHeadMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "RelaxedHeadMatchSieve";
	}

	@Override
	int runSieve(Mention mention) {
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;

		for (int idx = 0; idx < mention_idx; idx++) {
			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(mention, ante);

			if (relaxedEntityHeadMatch(pair) && wordInclusion(pair)
					&& properNameAgreement(pair) && !(IWithinI(pair))) {
				ante_idx = idx;
			}
		}
		return ante_idx;
	}
}
