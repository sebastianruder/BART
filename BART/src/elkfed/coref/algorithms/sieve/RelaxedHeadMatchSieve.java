package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class RelaxedHeadMatchSieve extends Sieve {
	
	private String name;
	
	public RelaxedHeadMatchSieve(List<Mention> mentions) {
		this.name = "RelaxedHeadMatchSieve";
	}

	@Override
	public String getName() {
		return this.name;
	}

}
