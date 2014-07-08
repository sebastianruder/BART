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
	 * Abstract method that uses the particular rules of a sieve
	 * to look for an antecedent for mention in the list of mentions
	 * that was provided to the sieve. 
	 * @param mention the mention whose antecedent is sought
	 * @return index of antecedent; -1 if no antecedent is found
	 */
	abstract int runSieve(Mention mention);
	
	/**
	 * Returns name of sieve
	 * @return name of the sieve
	 */
	String getName() {
		return this.name; 
	}
}
