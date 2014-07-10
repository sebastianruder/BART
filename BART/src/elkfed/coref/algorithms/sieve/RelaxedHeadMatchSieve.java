package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

public class RelaxedHeadMatchSieve extends Sieve {

	public static final SieveUtilities s = new SieveUtilities();

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
			if (s.relaxedEntityHeadMatch(mention, potAnte)) {
				if (s.wordInclusion(mention, potAnte)) {
					if (s.NERAgreement(pair)) {
						if (!(s.IWithinI(mention, potAnte))) {
							if (!potAnte.getPronoun()) {
								ante_idx = idx;
							}
						}
					}

				}

			}
		}
		return ante_idx;
	}
}
