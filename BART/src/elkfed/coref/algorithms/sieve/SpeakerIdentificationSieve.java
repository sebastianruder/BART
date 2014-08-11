package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.mentions.Mention;

/**
 * This sieve matches speakers to compatible pronouns
 * 
 * A speaker can either be to the left or right of a speech part
 * Speaker and compatible pronoun in speech have to have the same number and cannot be more than one sentence apart
 * 
 * A: "..."
 * A speech_verb (:) "..."
 * 
 * "...", so A
 * "...", speech_verb A
 * 
 * @author Xenia
 */
public class SpeakerIdentificationSieve extends Sieve {
	
	public SpeakerIdentificationSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "SpeakerIdentificationSieve";
	}

	@Override
	public int runSieve(Mention mention){
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++) {			
			pair = new PairInstance(mention, mentions.get(idx));
			Mention ante = pair.getAntecedent();
		
			if (langPlugin.isExpletiveRB(mention) || langPlugin.isExpletiveRB(ante)){ 
				return ante_idx; 
			}
			if (numberAgreement(pair) && !(FE_SentenceDistance.getSentDist(pair) > 1)) {
				// mention is in speech, antecedent is speaker
				if (FE_Speech.isMentionInSpeech(mention) && isSpeakerSpeechRight(ante)){
					// only pronoun- speaker matching
					if (mention.getPronoun() && !mention.getReflPronoun() && !mention.getRelPronoun()){
						ante_idx = idx;
					}
				}
				// antecedent is in speech, mention is speaker
				else if (FE_Speech.isMentionInSpeech(ante) && isSpeakerSpeechLeft(mention)){
					// only pronoun- speaker matching
					if (ante.getPronoun() && !ante.getReflPronoun() && !ante.getRelPronoun()){
						ante_idx = idx;
					}
				}
			}
		}
		return ante_idx;
	}
}
