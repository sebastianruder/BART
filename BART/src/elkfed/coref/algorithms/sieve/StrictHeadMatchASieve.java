package elkfed.coref.algorithms.sieve;

import java.util.List;

/**
 * @author julianbaumann
 */


/*
 * ToDo:  Proper WordInclusion with removed StopWords, modificator match,  i within i
 */
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.mentions.Mention;

public class StrictHeadMatchASieve extends StrictHeadMatch {
	
	private List<Mention> potentialAntecedents;
	
	public StrictHeadMatchASieve(List<Mention> potentialAntecedents) {
		this.potentialAntecedents = potentialAntecedents;
	}

public int runSieve(Mention mention){		
		
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			Mention potAnte = potentialAntecedents.get(idx);
			
			if (entityHeadMatch(mention, potAnte)) {
				//wordInclusion doesn't work as it should
				if(wordInclusion(mention, potAnte)) {
					if (compatibleModifiers(mention, potAnte)) {
						ante_idx = idx;
					}
				}
				
				
			}
		}
		//System.out.println(potentialAntecedents.get(ante_idx));
		return ante_idx;
	}
}
