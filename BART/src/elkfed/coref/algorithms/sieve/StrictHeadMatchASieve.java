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

public class StrictHeadMatchASieve extends Sieve {
	
	private static final SieveUtilities s = new SieveUtilities();
	
	private List<Mention> potentialAntecedents;
	private String name;
	
	public StrictHeadMatchASieve(List<Mention> potentialAntecedents) {
		this.potentialAntecedents = potentialAntecedents;
		this.name = "StrictHeadMatchASieve";
	}

public int runSieve(Mention mention){		
		
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			Mention potAnte = potentialAntecedents.get(idx);
			
			if (s.entityHeadMatch(mention, potAnte)) {
				
				if(s.wordInclusion(mention, potAnte)) {
					if (s.compatibleModifiers(mention, potAnte)) {
						if (!(s.IWithinI(mention, potAnte))) {
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
