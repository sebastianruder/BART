package elkfed.coref.algorithms.sieve;



import java.util.List;

import elkfed.coref.features.pairs.FE_PronounLeftRight;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.ml.TriValued;

public class PronounMatchSieve extends Sieve {
	
	private static final SieveUtilities s = new SieveUtilities();
	private String name;
	private List<Mention> mentions;
	
	

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PronounMatchSieve";
	}
		
		
	public int runSieve(Mention mention){
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			
			pair = new PairInstance(mention, mentions.get(idx));
			
			//if ( s.isAnaphoricPronoun(pair))
			
		
			if ((pair.getAnaphor().getPronoun() && !pair.getAntecedent().getPronoun()) || ((!pair.getAnaphor().getPronoun() && pair.getAntecedent().getPronoun())))
				{
				if (s.genderAgreement(pair) && s.sentenceDistance(pair) && s.numberAgreement(pair) && s.animacyAgreement(pair)){
					ante_idx = idx;
				}
			}	
			//if ((pair.getAnaphor().getPronoun()) && (pair.getAntecedent().getPronoun())){
				//if (s.personAgreement(pair)){
					//ante_idx = idx;
				//}
			//}
			
		}
		return ante_idx;
		}


	@Override
	String getName() {
		return this.name;
	}	

}
