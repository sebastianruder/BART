package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
/**
 * This Sieve links a Mention to an antecedent, if they both are named entitities
 * with the same semantic class and the mentions head word matches any word 
 * in the antecedents Discourse Entity. 
 * Additionally it needs to meet the word inclusion and the not I-within-I requirements.
 * 
 * 
 * @see SieveUtilities#relaxedEntityHeadMatch(Mention, Mention) 
 * @see SieveUtilities#NERAgreement(PairInstance)
 * @see SieveUtilities#wordInclusion(Mention, Mention) 
 * @see SieveUtilities#IWithinI(Mention, Mention)
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
			Mention potAnte = mentions.get(idx);
			PairInstance pair = new PairInstance(mention, potAnte);
			if (relaxedEntityHeadMatch(mention, potAnte)) {
				if (wordInclusion(mention, potAnte)) {
					if (properNameAgreement(mention, potAnte)) {
						if (!(IWithinI(mention, potAnte))) {

							ante_idx = idx;

						}
					}

				}

			}
		}
		return ante_idx;
	}
}
