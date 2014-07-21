package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;

/**
* This model links two mentions only if they contain exactly the same extent text, including modifiers and determiners
*
* @author Xenia, Sebastian
* 
*/

public class StringMatchSieve extends Sieve {
	
	StringMatchSieve(List<Mention> mentions){
		this.mentions = mentions;
		this.name = "StringMatchSieve";
	}
	
	public int runSieve(Mention mention){
		int ante_idx = -1;
		SemanticClass semclass = mention.getSemanticClass();
		
		if (contains_day_month_year(mention)) {
			return ante_idx;
		}
		int mention_idx = mentions.indexOf(mention);
		for (int idx = 0; idx < mention_idx; idx++){
			Mention ante = mentions.get(idx);
			if (mention.getMarkable().toString().equals(ante.getMarkable().toString())) {
				if (!(mention.getPronoun())) {
					if (contains_article(mention) || SemanticClass.isaPerson(semclass) || SemanticClass.isaObject(semclass)) {
						ante_idx = idx;
					}
				}
			}
		}
		return ante_idx;
	}
}
