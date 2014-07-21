package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.List;

import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.nlp.util.Gender;
import elkfed.nlp.util.Number;

/**
 * 
 * 
 * @author Xenia
 *
 */

public class PronounMatchSieve extends Sieve {

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PronounMatchSieve";
	}
		
	// sieve still misses pronoun-pronoun-linking ( = one pronoun in speech)
		
	public int runSieve(Mention mention){
		PairInstance pair;
	
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
			for (int idx = mention_idx; idx > 0; idx--){
				
				pair = new PairInstance(mention, mentions.get(idx));
				
				// anaphor has to be a pronoun but not a relative pronoun (those are covered in precise constructs)
				
				if (mention.getPronoun() && !mention.getRelPronoun()){
				
				
				// anaphor and antecedent cannot be more than 3 sentences apart
					
				if (FE_SentenceDistance.getSentDist(pair) > 3){
					return ante_idx;
				}
				
				// vorfeld-es can be neither anaphor nor antecedent
				
				if (isVorfeldEs(pair.getAnaphor()) || isVorfeldEs(pair.getAntecedent())){
					return ante_idx;
				}
				
				
				// reflexive pronoun's antecedent has to be in the same sentence
				
				if ((pair.getAnaphor().getReflPronoun() || pair.getAntecedent().getReflPronoun()) && FE_SentenceDistance.getSentDist(pair) > 0){
					return ante_idx;
				}
				
				// reflexive pronouns can only be matched with animate things
			
				
				if ((pair.getAnaphor().getReflPronoun() && !isAnimate(pair.getAntecedent())) || (pair.getAntecedent().getReflPronoun() && !isAnimate(pair.getAnaphor()))){
					return ante_idx;
				}
				
				// anaphor in speech and its antecedent not and vice versa should be taken care of by SpeakerIdentificationSieve
				
				if ((FE_Speech.isMentionInSpeech(pair.getAntecedent()) && !FE_Speech.isMentionInSpeech(pair.getAnaphor())) ||
					(!FE_Speech.isMentionInSpeech(pair.getAntecedent()) && FE_Speech.isMentionInSpeech(pair.getAnaphor()))){
					return ante_idx;
				}
				
				// gender agreement and number agreement as constraints
				// problem: several pronouns either don't have a gender assigned or have a clear gender, which results in Gender.UNKNOWN
				// if Gender.UNKNOWN is allowed for the anaphor (as a wildcard --> see stanford paper), too many other wrong choices are included
				
				if (genderAgreement(pair) &&  numberAgreement(pair) ){
								
									ante_idx = idx;
								}
					}
	
		}
		
		return ante_idx;
	}
}
		
		
		
		
		/**
		 * 	else {
				if (FE_SentenceDistance.getSentDist(pair) == 0){
					currentSentencePairs.add(pair);
				}
				
				if (FE_SentenceDistance.getSentDist(pair) > 0 && sentenceDistance(pair)){
					previousSentencePairs.add(pair);
					
				}
			}
			// sieve uses Hobb's algorithm to find antecedents
			// starts in list of anaphor/antecedent-pairs in same sentence
			// checks for number + gender-agreement at mention nearest (markable_id difference) to pronoun 
			// stanford also uses ner-label and animacy-constraints, should check if any improvement (especially ner-label)
			
			for (PairInstance p: currentSentencePairs){
				for (int i = 1; i < 20; i++){
					if (getMarkableDistance(p) == i && genderAgreement(p) && numberAgreement(p)){
						ante_idx = mentions.indexOf(p.getAntecedent());
					}
				}
				
				// if no mention found, checks with anaphor/antecedent-pairs in previous sentences
				// starts at one mention quite distant, should be most distant mention possible
				if (ante_idx == -1){
					for (PairInstance p2: previousSentencePairs){
						for (int j = 100 ;j > 0; j--){
							if (getMarkableDistance(p2) == j && genderAgreement(p2) && numberAgreement(p2)){
								ante_idx = mentions.indexOf(p2.getAntecedent());
							}
						}
					}
				}
			}}	
		}		
		 * 
		 * 
		 * 
		 * 
		 * **/
		
