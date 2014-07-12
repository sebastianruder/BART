package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;
/**
 * This Sieve links a Mention to an antecedent,
 * if its head word matches any head word of the antecedents Discourse Entity.  
 * Compared to {@link StrictHeadMatchASieve} it drops the word inclusion requirement,
 * but retains the compatible modifiers and I-within-I requirements
 * 
 * @see SieveUtilities#entityHeadMatch(Mention, Mention)  
 * @see SieveUtilities#wordInclusion(Mention, Mention) 
 * @see SieveUtilities#IWithinI(Mention, Mention)
 * 
 * @author Julian
 *
 */
public class StrictHeadMatchCSieve extends Sieve {
		
	public StrictHeadMatchCSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "StrictHeadMatchCSieve";
	}
	
	@Override
	int runSieve(Mention mention) {
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;

		for (int idx = 0; idx < mention_idx; idx++) {
			Mention potAnte = mentions.get(idx);

			if (s.entityHeadMatch(mention, potAnte)) {
				if (s.compatibleModifiers(mention, potAnte)) {
					if (!(s.IWithinI(mention, potAnte))) {
						if (!potAnte.getPronoun()) {
							ante_idx = idx;
						}
					}
				}
			}
		}
		
		return ante_idx;
	}
}
