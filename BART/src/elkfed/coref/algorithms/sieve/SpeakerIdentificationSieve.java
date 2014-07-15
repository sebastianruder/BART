package elkfed.coref.algorithms.sieve;

import static elkfed.mmax.pipeline.MarkableCreator.SPEAKER_ATTRIBUTE;

import java.util.HashSet;
import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.features.pairs.FE_SpeakerAlias;


import elkfed.coref.mentions.Mention;

public class SpeakerIdentificationSieve extends Sieve {
	
	public SpeakerIdentificationSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "SpeakerIdentificationSieve";
	}

	@Override
	public int runSieve(Mention mention){	
		
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		

		for (int idx = 0; idx < mentions.size(); idx++){
			Mention potAnte = mentions.get(idx);
			PairInstance pair = new PairInstance(mention, potAnte);

		// mention and potAnte have to be 	
			
		if (FE_SentenceDistance.getSentDist(pair) < 2 && s.isSpeaker(mention) && FE_Speech.isMentionInSpeech(potAnte) && potAnte.getPersPronoun()){
			ante_idx = idx;
			
		}
		
		
			
		}
			
		
		
		
		return ante_idx;
	}
}
