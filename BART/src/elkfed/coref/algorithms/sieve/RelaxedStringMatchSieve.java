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
 * IMPORTANT: Mentions identified by BART DON'T contain
 * such post-modifying text.
 * 
 * Relaxed String Match -- in this respect -- is the same
 * as Exact String Match. Ignoring pre-modifiers -- in turn --
 * would make it equal to Strict Head Match.
 * Possibilities:
 * 	- omit sieve
 * 	
 * Mention has parameter _postmodifiers; is always empty, though.
 * 
 * Ask Yannick about this.
 * 
 * @author Sebastian
 *
 */

public class RelaxedStringMatchSieve extends Sieve {

	private List<Mention> potentialAntecedents;
	private String name;
	
	public RelaxedStringMatchSieve(List<Mention> mentions) {
		this.potentialAntecedents = mentions;
		this.name = "RelaxedDtringMatchSieve";
		
	}
		
	public int runSieve(Mention mention){
		int mention_idx = potentialAntecedents.indexOf(mention);
		int ante_idx = -1;
		// sentences should be displayed somehow
		// Markable[] array = mention.getSentenceMarkables("sentence");
		for (int idx = 0; idx < mention_idx; idx++){

			if (mention.toString().equals(potentialAntecedents.get(idx).toString())){
				// articles are still matched; needs to be fixed
				if (!(mention.getPronoun())) {
					ante_idx = idx;
				}
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
