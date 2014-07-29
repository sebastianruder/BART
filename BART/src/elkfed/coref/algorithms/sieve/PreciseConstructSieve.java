package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

/**
 * This sieve links two mentions if any of the following
 * conditions are satisified:
 * 1. 	Appositive: the two nominal mentions are in an appositive
 * 		construction (e.g., [Israeli Deputy Defense Minister],
 * 		[Ephraim Sneh], said . . . ).
 * 		Appositives: third children of a parent NP whose expansion
 * 		begins with (NP , NP), when there is not a conjunction
 * 		in the expansion.
 * 2. 	Predicate nominative: the two mentions (nominal or
 * 		pronominal) are in a copulative subject-object relation
 * 		(e.g., [The New York-based College Board] is [a nonprofit
 * 		organization that administers the SATs and promotes
 * 		higher education]).
 * 3.	Role appositive: the candidate antecedent is headed by
 * 		a noun and appears as a modifier in an NP whose head is
 * 		the current mention (e.g., [[actress] Rebecca Schaeffer]).
 * 		This feature matches only if: (a) the mention is labeled
 * 		as a person, (b) the antecedent is animate, and (c) the
 * 		antecedent's gender is not neutral.
 * 4. 	Relative pronoun: the mention is a relative pronoun that
 * 		modifies the head of the antecedent NP (e.g., [the finance
 * 		street [which] has already formed in the Waitan district]).
 * 5.	Acronym: both mentions are tagged as NNP and one of them
 * 		is an acronym of the other (e.g., [Agence France Presse]...
 * 		[AFP]). A mention is an acronym of another mention if its
 * 		text equals the sequence of upper case characters in the
 * 		other mention.
 * 6. 	Demonym: one of the mentions is a demonym of the other
 * 		(e.g., [Israel]...[Israeli]). For demonym detection we use
 * 		a static list of countries and their gentilic forms from
 * 		Wikipedia.
 * 
 * @author Sebastian
 *
 */
public class PreciseConstructSieve extends Sieve {
	
	// constructor
	public PreciseConstructSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PreciseConstructSieve";
	}
	
	public int runSieve(Mention mention){
		
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++) {			
			pair = new PairInstance(mention, mentions.get(idx));
			if (isRelativePronoun(pair) || isAcronym(pair) || isDemonym(pair) || isRoleAppositive(pair)
					// || isAppositive(pair)
					// || isPredicateNominative(pair) // no tagged copula constructions in TüBa-D/Z
					){
				ante_idx = idx;
			}
		}
		return ante_idx;
	}
}
