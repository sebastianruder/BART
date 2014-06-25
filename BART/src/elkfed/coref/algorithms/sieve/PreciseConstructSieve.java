package elkfed.coref.algorithms.sieve;

import static elkfed.lang.EnglishLinguisticConstants.ARTICLE;
import static elkfed.lang.EnglishLinguisticConstants.DEMONSTRATIVE;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

import java.util.List;

import elkfed.config.ConfigProperties;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Appositive;
import elkfed.coref.features.pairs.FE_AppositiveParse;
import elkfed.coref.features.pairs.FE_Copula;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * This sieve links two mentions if any of the following
 * conditions are satisified:
 * 1. 	Appositive: the two nominal mentions are in an appositive
 * 		construction (e.g., [Israel’s Deputy Defense Minister],
 * 		[Ephraim Sneh], said . . . ).
 * 		Appositives: third children of a parent NP whose expansion
 * 		begins with (NP , NP), when there is not a conjunction
 * 		in the expansion.
 * 2. 	Predicate nominative – the two mentions (nominal or
 * 		pronominal) are in a copulative subject–object relation
 * 		(e.g., [The New York-based College Board] is [a nonprofit
 * 		organization that administers the SATs and promotes
 * 		higher education]).
 * 3.	Role appositive – the candidate antecedent is headed by
 * 		a noun and appears as a modifier in an NP whose head is
 * 		the current mention (e.g., [[actress] Rebecca Schaeffer]).
 * 		This feature matches only if: (a) the mention is labeled
 * 		as a person, (b) the antecedent is animate, and (c) the
 * 		antecedent’s gender is not neutral.
 * 4. 	Relative pronoun – the mention is a relative pronoun that
 * 		modifies the head of the antecedent NP (e.g., [the finance
 * 		street [which] has already formed in the Waitan district]).
 * 5.	Acronym – both mentions are tagged as NNP and one of them
 * 		is an acronym of the other (e.g., [Agence France Presse]...
 * 		[AFP]). A mention is an acronym of another mention if its
 * 		text equals the sequence of upper case characters in the
 * 		other mention.
 * 6. 	Demonym – one of the mentions is a demonym of the other
 * 		(e.g., [Israel]...[Israeli]). For demonym detection we use
 * 		a static list of countries and their gentilic forms from
 * 		Wikipedia.
 * 
 * @author Sebastian
 *
 */

public class PreciseConstructSieve extends Sieve {
	
	// list of antecedents/potential coreferents
	private List<Mention> mentions;
	
	public PreciseConstructSieve(List<Mention> mentions) {
		this.mentions = mentions;
	}
	
	public int runSieve(Mention mention){
		
		PairInstance pair;
		int mention_idx = mentions.indexOf(mention);
		int ante_idx = -1;
		
		for (int idx = 0; idx < mention_idx; idx++){
			
			pair = new PairInstance(mention, mentions.get(idx));
			if (isAppositive(pair) || isPredicateNominative(pair) || isRoleAppositive(pair) || isRelativePronoun(pair)
					|| isAcronym(pair) || isDemonym(pair)){
				// articles are still matched; needs to be fixed
				if (!(mention.getPronoun())) {
					ante_idx = idx;
				}
			}
		}
		return ante_idx;
	}

	private boolean isAppositive(PairInstance pair) {
		/*
		 * there is FE_AppositiveParse.getAppositivePrs(pair) as well
		 * might have to investigate how they differ
		 * 
		 * appositive constructions as one NP in TüBa-D/Z
		 * switch on and off
		 */
		FE_AppositiveParse.getAppositivePrs(pair); // use this one
		if (FE_Appositive.getAppositive(pair)) {
			return true;
		}
		return false;
	}

	private boolean isPredicateNominative(PairInstance pair) {
		if (FE_Copula.getCopula(pair)){
			return true;
		}
		return false;
	}		
	
	private boolean isRoleAppositive(PairInstance pair) {
		/*
		 * there is FE_AnimacyAgree
		 * FE_AnimacyAgree() doesn't seem to be implemented, though
		 */
		if (pair.getAnaphor().getProperName() && // check if person
				isAnimate(pair.getAntecedent()) && // check if animate 
				!isNeutral(pair.getAntecedent())) { // check if neutral
			return true;
		}
		return false;
	}

	private boolean isAnimate(Mention mention) {
		/**
		 * Animacy is set using:
		 * (a) a static list for pronouns;
		 * (b) NER labels (e.g., PERSON is animate whereas LOCATION is not); and
		 * (c) a dictionary bootstrapped from the Web (Ji and Lin 2009)
		 */
		// (a) check with pronoun list
		// not necessary for isRoleAppositive, maybe for other applications
		String[] tokens = mention.getMarkable().getDiscourseElements();
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i].toLowerCase();
			if (t.matches(MALE_PRONOUN_ADJ) || t.matches(FEMALE_PRONOUN_ADJ) ||
					t.matches(FIRST_PERSON_SG_PRO) || t.matches(FIRST_PERSON_PL_PRO) ||
					t.matches(SECOND_PERSON_PRO)) {
				return true;
			}
		}
		// (b) check with NER labels
		if (SemanticClass.isaPerson(mention.getSemanticClass())) {
			return true;
		}
		else if (SemanticClass.isaObject(mention.getSemanticClass()) ||
				SemanticClass.isaNumeric(mention.getSemanticClass())) {
			return false;
		}
		// (c) check with bootstrapped dictionary
		// get dictionary from Stanford GitHub repo
		
		return false;
	}
	
	private boolean isNeutral(Mention mention) {
		
		if (SemanticClass.isaPerson(mention.getSemanticClass())) {
			return false;
		}
		String[] tokens = mention.getMarkable().getDiscourseElements();
				
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i];
			
			if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
				
			}
			
			/*
			 * check with language plugin; check rules for when a noun
			 * has a different gender in different languages
			 */
			
			/* if (t.isCapital() && t.endsWith("in")) {
				
			}
			
			Genus of markable
			
			for German: nouns ending on "in" are female;
			for English: enumerate list of female/male nouns 
			
			/* 
			 * check if a (modified) noun, e.g., the marvelous actress
			 * is neutral
			 * create list with male / female / neutral nouns (in English/German)
			 */
		}
		return false;
	}

	private boolean isRelativePronoun(PairInstance pair) {
		
		String[] tokens = pair.getAnaphor().getMarkable().getDiscourseElements();
		if (tokens.length == 1 && tokens[0].matches(RELATIVE_PRONOUN)) {
			// check if modifies head of antecedent NP
		}
		return false;
	}
	
	private boolean isAcronym(PairInstance pair) {
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		// check if mention or antecedent only contain upper letters
		
		
		String mention_initials = "";
		String antecedent_initials = "";
		for (String word : mention.split(" ")) {
			mention_initials += word.substring(0,1).toUpperCase();
		}
		for (String word : antecedent.split(" ")) {
			antecedent_initials += word.substring(0,1).toUpperCase();
		}
		
		
		return false;
	}

	private boolean isDemonym(PairInstance pair) {
		/**
		 * Check if one expression is a demonym of the other using
		 * a static list of countries and their gentilic forms from
		 * Wikipedia
		 */
		
		/*
		 * extract demonym list from Wikpedia
		 * localize in different languages
		 * http://en.wikipedia.org/wiki/Demonym
		 * http://de.wikipedia.org/wiki/Volksbezeichnung
		 * http://it.wikipedia.org/wiki/Etnico_(onomastica)
		 * how to check for language plugin?
		 */
		
	    public EnglishLanguagePlugin() {
	        readMapping(TableName.AdjMap, "adj_map_en.txt");
	    }
		
		if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
			
		}
		
		return false;
	}	

}
