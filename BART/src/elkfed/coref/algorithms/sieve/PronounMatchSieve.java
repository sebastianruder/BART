package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.List;

import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

public class PronounMatchSieve extends Sieve {
	
	private static final SieveUtilities s = new SieveUtilities();
	private List<Mention> mentions;

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PronounMatchSieve";
	}
		
	// sieve still misses pronoun-pronoun-linking ( = one pronoun in speech)
		
	public int runSieve(Mention mention){
		PairInstance pair;
		ArrayList<PairInstance> currentSentencePairs;
		ArrayList<PairInstance> previousSentencePairs;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		if (mention.getPronoun()){ // mention has to be a pronoun
			
			currentSentencePairs = new ArrayList<PairInstance>();
			previousSentencePairs = new ArrayList<PairInstance>();
			
			for (int idx = 0; idx < mention_idx; idx++){
				
				// puts PairInstances into 2 lists: Anaphor and Antecedent are either in the same sentence or not
				
				pair = new PairInstance(mention, mentions.get(idx));
				if (FE_SentenceDistance.getSentDist(pair) == 0){
					currentSentencePairs.add(pair);
				}
				
				if (FE_SentenceDistance.getSentDist(pair) > 0 && s.sentenceDistance(pair)){
					previousSentencePairs.add(pair);
					
				}
			}
			// sieve uses Hobb's algorithm to find antecedents
			// starts in list of anaphor/antecedent-pairs in same sentence
			// checks for number + gender-agreement at mention nearest (markable_id difference) to pronoun 
			// stanford also uses ner-label and animacy-constraints, should check if any improvement (especially ner-label)
			
			for (PairInstance p: currentSentencePairs){
				for (int i = 1; i < 20; i++){
					if (s.getMarkableDistance(p) == i && s.genderAgreement(p) && s.numberAgreement(p)){
						ante_idx = mentions.indexOf(p.getAntecedent());
					}
				}
				
				// if no mention found, checks with anaphor/antecedent-pairs in previous sentences
				// starts at one mention quite distant, should be most distant mention possible
				if (ante_idx == -1){
					for (PairInstance p2: previousSentencePairs){
						for (int j = 100 ;j > 0; j--){
							if (s.getMarkableDistance(p2) == j && s.genderAgreement(p2) && s.numberAgreement(p2)){
								ante_idx = mentions.indexOf(p2.getAntecedent());
							}
						}
					}
				}
			}	
		}		
		
		return ante_idx;
	}
}
