package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class StrictHeadMatchBSieve extends Sieve {

	private static final SieveUtilities s = new SieveUtilities();

	private List<Mention> potentialAntecedents;
	private String name;

	public StrictHeadMatchBSieve(List<Mention> potentialAntecedents) {
		this.potentialAntecedents = potentialAntecedents;
		this.name = "StrictHeadMatchBSieve";

	}

	@Override
	int runSieve(Mention mention) {
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;

		for (int idx = 0; idx < mention_idx; idx++) {
			Mention potAnte = potentialAntecedents.get(idx);

			if (s.entityHeadMatch(mention, potAnte)) {
				if (s.wordInclusion(mention, potAnte)) {
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

	@Override
	public String getName() {
		return this.name;
	}

}
