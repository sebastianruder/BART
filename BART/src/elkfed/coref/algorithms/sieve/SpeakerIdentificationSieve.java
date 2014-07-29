package elkfed.coref.algorithms.sieve;

import java.util.List;


import elkfed.lang.GermanLinguisticConstants;
import elkfed.config.ConfigProperties;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.mentions.Mention;
import elkfed.lang.LanguagePlugin;

/**
 * This sieve matches speakers to compatible pronouns,
 *
 *
 * CONSTRAINTS TO BE IMPLEMENTED:
 * I assigned to the same speaker are coreferent.
 * you with the same speaker are coreferent.
 * The speaker and I in her text are coreferent.
 *
 * The speaker and a mention which is not I in the speaker’s utterance cannot be coreferent.
 * Two I (or two you, or two we) assigned to different speakers cannot be coreferent.
 * Two different person pronouns by the same speaker cannot be coreferent.
 * Nominal mentions cannot be coreferent with I, you, or we in the same turn or quotation.
 * In conversations, you can corefer only with the previous speaker.
 * The constraints result in causing [my] and [he] to not be coreferent in the earlier example (due to the third constraint).
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
			
			if (isVorfeldEs(mention) || isVorfeldEs(ante)){ 
				return ante_idx; 
			}
			if (numberAgreement(pair)) {
				if (FE_Speech.isMentionInSpeech(mention) && isSpeakerSpeechRight(ante)){
					if (mention.getPronoun() && !mention.getReflPronoun() && !mention.getRelPronoun()){
						ante_idx = idx;
					}
				}
				else if (FE_Speech.isMentionInSpeech(ante) && isSpeakerSpeechLeft(mention)){
					if (ante.getPronoun() && !ante.getReflPronoun() && !ante.getRelPronoun()){
						ante_idx = idx;
					}
				}
			}
		}
		return ante_idx;
	}
}
