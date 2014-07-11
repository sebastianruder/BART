package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

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
