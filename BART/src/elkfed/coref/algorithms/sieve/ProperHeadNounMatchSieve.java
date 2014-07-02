package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class ProperHeadNounMatchSieve extends Sieve {
	private String name;
	
	public ProperHeadNounMatchSieve(List<Mention> mentions) {
		this.name = "ProperHeadNounMatchSieve";
	}

	@Override
	public String getName() {
		return this.name;
	}

}
