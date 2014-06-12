package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.mentions.Mention;
import elkfed.mmax.minidisc.Markable;

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
		
		FE_StringMatch fe_stringmatch = new FE_StringMatch();
		
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
		// sentences should be displayed somehow
		// Markable[] array = mention.getSentenceMarkables("sentence");
		for (int idx = 0; idx < mention_idx; idx++){

			/*
			 * existing StringMatch implementation of BART
			 * finds less matches than our method, though
			 * if (fe_stringmatch.getStringMatch(new PairInstance(mention, potentialAntecedents.get(idx))
			 */
			if (mention.toString().equals(potentialAntecedents.get(idx).toString())){
				/*
				 * articles are still matched; needs to be fixed
				 * refer to getMarkableString method in FE_Pronominal_StrMatch for fix
				 * 
				*/				
				if (!(mention.getPronoun())) {
					ante_idx = idx;
				}
			}
		}
		//System.out.println(potentialAntecedents.get(ante_idx));
		return ante_idx;
	}
	
	public void compareEntities(Mention mention, Mention potAnt){
		
	}

}
