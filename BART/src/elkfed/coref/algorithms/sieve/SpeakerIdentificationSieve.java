package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class SpeakerIdentificationSieve extends Sieve {
	
	public SpeakerIdentificationSieve(List<Mention> mentions) {
		this.name = "SpeakerIdentificationSieve";
	}
}
