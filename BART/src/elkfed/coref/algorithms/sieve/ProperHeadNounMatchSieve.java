package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

public class ProperHeadNounMatchSieve extends Sieve {

	private static final SieveUtilities s = new SieveUtilities();
	private List<Mention> mentions;

	public ProperHeadNounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "ProperHeadNounMatchSieve";
	}
	
	public int runSieve(Mention mention){
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			pair = new PairInstance(mention, mentions.get(idx));
			
			if (pair.getAnaphor().getHeadPOS().equals("NE") && pair.getAntecedent().getHeadPOS().equals("NE") && (pair.getAnaphor().getDiscourseEntity().getHeadsString().equals(pair.getAntecedent().getDiscourseEntity().getHeadsString()))){
				if (!s.IWithinI(mention, mentions.get(idx)) && s.noNumericMismatch(pair)){
					ante_idx = idx;
				}
			}
		}	
		return ante_idx;
	}
}
