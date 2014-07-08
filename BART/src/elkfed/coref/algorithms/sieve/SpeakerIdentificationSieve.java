package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class SpeakerIdentificationSieve extends Sieve {
	
	public SpeakerIdentificationSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "SpeakerIdentificationSieve";
	}

	@Override
	int runSieve(Mention mention) {
		return -1;
	}
}
