package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;
/**
 * This Sieve links a Mention to an antecedent,
 * if its head word matches any head word of the antecedents Discourse Entity.  
 * Compared to {@link StrictHeadMatchASieve} it drops the compatible modifiers requirement,
 * but retains the word inclusion and I-within-I requirements
 * 
 * @see SieveUtilities#entityHeadMatch(Mention, Mention)  
 * @see SieveUtilities#compatibleModifiers(Mention, Mention) 
 * @see SieveUtilities#IWithinI(Mention, Mention)
 * 
 * @author Julian
 *
 */
public class StrictHeadMatchBSieve extends Sieve {

	public StrictHeadMatchBSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "StrictHeadMatchBSieve";
	}

	@Override
	int runSieve(Mention mention) {
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;

		for (int idx = 0; idx < mention_idx; idx++) {
			Mention potAnte = mentions.get(idx);

			if (s.entityHeadMatch(mention, potAnte)) {
				if (s.wordInclusion(mention, potAnte)) {
					if (!(s.IWithinI(mention, potAnte))) {
						
							ante_idx = idx;
						
					}
				}
			}
		}
		
		return ante_idx;
	}
}
