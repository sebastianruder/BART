package elkfed.coref.algorithms.sieve;

import static elkfed.lang.GermanLinguisticConstants.PRONOUNS;

import java.util.List;

import elkfed.coref.features.pairs.FE_PronounLeftRight;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.ml.TriValued;

public class PronounMatchSieve extends Sieve {
	
	private List<Mention> mentions;
	
	

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
	}
		
		
	public int runSieve(Mention mention){
		
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			
			pair = new PairInstance(mention, mentions.get(idx));
			
			if ((isPronoun(pair.getAnaphor()) && !isPronoun(pair.getAntecedent())) || (isPronoun(pair.getAntecedent()) && !isPronoun(pair.getAnaphor()))){
		
				if (genderAgreement(pair) && sentenceDistance(pair) && numberAgreement(pair) && animacyAgreement(pair)){
				ante_idx = idx;
			}
			}	
			
		}
		return ante_idx;
		}
	

		/**
		Number – we assign number attributes based on: (a) a static list for
		pronouns; (b) NER labels: mentions marked as a named entity are
		considered singular with the exception of organizations, which can be
		both singular and plural; (c) part of speech tags: NN*S tags are plural and
		all other NN* tags are singular; and (d) a static dictionary from Bergsma
		and Lin (2006).
		**/
			
	public boolean numberAgreement(PairInstance pair){
		if (FE_Number.getNumber(pair)){
			return true;
		}
		return false;
		
	}
	
	/**
	 * Gender – we assign gender attributes from static lexicons from Bergsma
		and Lin (2006), and Ji and Lin (2009).
	 
	*/
	public boolean genderAgreement(PairInstance pair){
		
		if (FE_Gender.getGender(pair).equals(TriValued.TRUE)){
			return true;
		}
		return false;
		
	}
	
	
	
	/**
	 * 
	 * Person – we assign person attributes only to pronouns. We do not enforce
		this constraint when linking two pronouns, however, if one appears within
		quotes. This is a simple heuristic for speaker detection (e.g., I and she point
		to the same person in “[I] voted my conscience,” [she] said).
	 */
	
	public void personAgreement(PairInstance pair){
	// missing
		
	}
	
	public boolean isPronoun(Mention mention){
		String[] tokens = mention.getMarkable().getDiscourseElements();
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i].toLowerCase();
			if (t.matches(PRONOUNS)){
				return true;
			}
		}
				
			return false;

	}
	
	/**
	 * Animacy – we set animacy attributes using: (a) a static list for pronouns;
		(b) NER labels (e.g., PERSON is animate whereas LOCATION is not); and (c) a
		dictionary bootstrapped from the Web (Ji and Lin 2009).
		**/
	
	public boolean animacyAgreement(PairInstance pair){
		if (isAnimate(pair.getAnaphor()) == isAnimate(pair.getAntecedent())){
			return true;
		}
		return false;
		
	}
	
	/**
	 * NER label – from the Stanford NER.
	 * **/
	
	public boolean NERAgreement(PairInstance pair){
		return false;
		
		
	}
	

	
	
	/**
	 * Pronoun distance - sentence distance between a pronoun and its
		antecedent cannot be larger than 3.
	 * @param pair
	 * @return
	 */
	
	
	public boolean sentenceDistance(PairInstance pair){
		if(FE_SentenceDistance.getSentDist(pair) < 4){
			return true;
		}
		
		return false;

	}
	
	
	
	

}
