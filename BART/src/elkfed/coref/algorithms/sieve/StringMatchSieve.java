package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.Mention;

/**
*
* @author xkuehling
* 
*/

public class StringMatchSieve extends Sieve {
	
	private List<Mention> potentialAntecedents;
	
	
	
	StringMatchSieve(List<Mention> potentialAntecedents){
		this.potentialAntecedents = potentialAntecedents;			
	}
	
	
	public int runSieve(Mention mention){
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = potentialAntecedents.size();
		for (int idx = 0; idx < potentialAntecedents.size(); idx++){
			if (mention.toString().equals(potentialAntecedents.get(idx).toString()) && mention_idx < idx && idx < ante_idx){
				// articles are still matched; needs to be fixed
				if (!(mention.getPronoun())) {
					ante_idx = idx;
				}
			}
		}
		if (ante_idx == potentialAntecedents.size()){
			return -1;
			}
		else {		
			//System.out.println(potentialAntecedents.get(ante_idx));
			return ante_idx;
		}

	}
	
	public void compareEntities(Mention mention, Mention potAnt){
		
	}

}
