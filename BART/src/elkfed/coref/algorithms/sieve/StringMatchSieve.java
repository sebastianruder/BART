package elkfed.coref.algorithms.sieve;




import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.mentions.Mention;
import elkfed.mmax.minidisc.Markable;
import elkfed.coref.features.pairs.FE_DistanceSentence;
import elkfed.coref.features.pairs.FE_Pronominal_StrMatch;



/**
*
* @author xkuehling
* 
*/

public class StringMatchSieve extends Sieve {
	
	private List<Mention> potentialAntecedents;
	private String name;
	
	StringMatchSieve(List<Mention> potentialAntecedents){
		this.potentialAntecedents = potentialAntecedents;	
		this.name = "StringMatchSieve";
	}
	
	public int runSieve(Mention mention){
		
		FE_StringMatch fe_stringmatch = new FE_StringMatch();
		/** getMarkableString() aus FE_Pronominal_StrMatch macht letztendlich nichts anderes als getMarkableString() aus FE_StringMatch, 
		 * funktioniert aber nicht auf Anhieb (wahrscheinlich TuebaDZ-Problem)
		 * die getStringMatch-Methode aus FE_StringMatch macht das gleiche wie die vorherige StringMatch- Methode, nutzt allerdings getMarkableString() um Artikel etc zu entfernen
		 *  
		 */
		
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
			
		for (int idx = 0; idx < mention_idx; idx++){
			Mention ante = potentialAntecedents.get(idx);
			if (mention.getMarkable().toString().equals(ante.getMarkable().toString())) {
				if (!(mention.getPronoun())) {
					ante_idx = idx;
				}
//			 if (fe_stringmatch.getStringMatch(new PairInstance(mention, potentialAntecedents.get(idx)))){
//	
//				if (!(mention.getPronoun())) {
//					ante_idx = idx;
//				}
			}
		}
		return ante_idx;
	}
	
	public void compareEntities(Mention mention, Mention potAnt){
		
	}

	@Override
	public String getName() {
		return this.name;
	}

}
