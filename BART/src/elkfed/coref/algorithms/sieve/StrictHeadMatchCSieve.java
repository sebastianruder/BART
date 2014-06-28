package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class StrictHeadMatchCSieve extends StrictHeadMatch {

	private List<Mention> potentialAntecedents;

	public StrictHeadMatchCSieve(List<Mention> potentialAntecedents) {
		this.potentialAntecedents = potentialAntecedents;
	}
	
	@Override
	int runSieve(Mention mention) {
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			Mention potAnte = potentialAntecedents.get(idx);
			
			if (entityHeadMatch(mention, potAnte)) {				
				//missing: i within i
				ante_idx = idx;
			}
		}
		//System.out.println(potentialAntecedents.get(ante_idx));
		return ante_idx;
	}

}
