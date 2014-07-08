package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class RelaxedHeadMatchSieve extends Sieve {
	
	public RelaxedHeadMatchSieve(List<Mention> mentions) {
		this.name = "RelaxedHeadMatchSieve";
	}
}
