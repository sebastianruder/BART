package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class StrictHeadMatchBSieve extends StrictHeadMatch {
	
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

	@Override
	public String getName() {
		return this.name;
	}

}
