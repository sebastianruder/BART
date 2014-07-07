package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

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

	private List<Mention> antecedents;
	private String name;
	
	public RelaxedStringMatchSieve(List<Mention> mentions) {
		this.antecedents = mentions;
		this.name = "RelaxedStringMatchSieve";
	}
		
	public int runSieve(Mention mention){
		int mention_idx = antecedents.indexOf(mention);
		int ante_idx = -1;
		for (int idx = 0; idx < mention_idx; idx++){
			if (mention.toString().equals(antecedents.get(idx).toString())){
				if (!(mention.getPronoun())) {
					ante_idx = idx;
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