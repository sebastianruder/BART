package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

/**
*
* @author xkuehling
* 
*/

public abstract class Sieve {
	
	// sieve utility class
	protected static final SieveUtilities s = new SieveUtilities();
	protected String name; // name of sub class
	// list of antecedents/potential coreferents
	protected List<Mention> mentions;
	
	/**
	 * Looks 
	 * @param mention
	 * @return
	 */
	int runSieve(Mention mention){
		return -1;
	}
	
	String getName() {
		return this.name; 
	}
}
