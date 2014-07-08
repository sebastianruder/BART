package elkfed.coref.algorithms.sieve;

import elkfed.coref.mentions.Mention;

/**
*
* @author xkuehling
* 
*/

public abstract class Sieve {
	
	String name;
	
	int runSieve(Mention mention){
		return -1;
	}
	
	void compareEntities() {	
	}
	
	String getName() {
		return this.name; 
	}
}
