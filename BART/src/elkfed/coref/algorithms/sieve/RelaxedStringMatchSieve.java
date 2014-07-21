package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import static elkfed.lang.EnglishLinguisticConstants.DEF_ARTICLE;
import static elkfed.lang.EnglishLinguisticConstants.INDEF_ARTICLE;
import static elkfed.lang.EnglishLinguisticConstants.DAYS_MONTHS_YEAR;

/**
 * This sieve considers two nominal mentions as coreferent,
 * if the strings obtained by dropping the text following
 * their head words (such as relative clauses and PP and
 * participial postmodifiers) are identical (e.g., [Clinton]
 * and [Clinton, whose term ends in January]).
 * 
 * @author Sebastian
 *
 */

public class RelaxedStringMatchSieve extends Sieve {
	
	public RelaxedStringMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "RelaxedStringMatchSieve";
	}
		
	public int runSieve(Mention mention){
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		SemanticClass semclass = mention.getSemanticClass();
		
		if (contains_day_month_year(mention) || (!(contains_article(mention) || SemanticClass.isaPerson(semclass) || SemanticClass.isaObject(semclass)))) {
			return ante_idx;
		}
		
		for (int idx = 0; idx < mention_idx; idx++){
			PairInstance pair = new PairInstance(mention, mentions.get(idx));
			if (mention.toString().equals(mentions.get(idx).toString())){
				if (!(mention.getPronoun()) && noNumericMismatch(pair) && noLocationMismatch(pair) && antecedent_is_more_specific(pair)) {
					ante_idx = idx;
				}
			}
		}
		return ante_idx;
	}

}
