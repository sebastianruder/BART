package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.mentions.Mention;

/**
 * This sieve matches speakers to compatible pronouns,
 *
 *
 * CONSTRAINTS TO BE IMPLEMENTED:
 * I  assigned to the same speaker are coreferent.
 * you with the same speaker are coreferent.
 * The speaker and I in her text are coreferent.
 *
 * The speaker and a mention which is not I in the speaker’s utterance cannot be coreferent.
 * Two I (or two you, or two we) assigned to different speakers cannot be coreferent.
 * Two different person pronouns by the same speaker cannot be coreferent.
 * Nominal mentions cannot be coreferent with I, you, or we in the same turn or quotation.
 * In conversations, you can corefer only with the previous speaker.
 *The constraints result in causing [my] and [he] to not be coreferent in the earlier example (due to the third constraint).
 * 
 * @see SieveUtilities#isSpeaker(Mention)
 * @see FE_SentenceDistance#getSentDist(PairInstance)
 * @see FE_Speech#isMentionInSpeech(Mention)
 * @see Mention#getPersPronoun()
 * @see SieveUtilities#isVorfeldEs(Mention)
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
		int ante_idx = -1;

		for (int idx = 0; idx < mentions.size(); idx++){ // antecedences in speech can occur before and after a mention
			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(mention, ante);

		// antecedences have to be in the same or a neighbouring sentence
		// mention has to be a speaker
		// antecendent has to in speech AND a (personal) pronoun	
		// further constraints to be implemented (see header)	
			if (FE_SentenceDistance.getSentDist(pair) < 2 && s.isSpeaker(mention) && FE_Speech.isMentionInSpeech(ante) && ante.getPersPronoun() && !s.isVorfeldEs(ante)&& s.isAnimate(mention)){
			ante_idx = idx;
			
			}
		}
		return ante_idx;
	}
}
