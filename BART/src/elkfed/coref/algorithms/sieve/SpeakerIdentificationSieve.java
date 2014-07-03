package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class SpeakerIdentificationSieve extends Sieve {
	
	private String name;
	
	public SpeakerIdentificationSieve(List<Mention> mentions) {
		this.name = "SpeakerIdentificationSieve";
	}
	
	@Override
	public String getName() {
		return this.name;
	}

}
