package elkfed.coref.algorithms.sieve;

import static elkfed.lang.EnglishLinguisticConstants.FEMALE_PRONOUN_ADJ;
import static elkfed.lang.EnglishLinguisticConstants.FIRST_PERSON_PL_PRO;
import static elkfed.lang.EnglishLinguisticConstants.FIRST_PERSON_SG_PRO;
import static elkfed.lang.EnglishLinguisticConstants.MALE_PRONOUN_ADJ;
import static elkfed.lang.EnglishLinguisticConstants.RELATIVE_PRONOUN;
import static elkfed.lang.EnglishLinguisticConstants.SECOND_PERSON_PRO;
import static elkfed.lang.GermanLinguisticConstants.ANAPHORIC_PRONOUN;


import static elkfed.mmax.MarkableLevels.DEFAULT_MARKABLE_LEVEL;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;
import elkfed.config.ConfigProperties;
import elkfed.coref.PairInstance;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.features.pairs.FE_AppositiveParse;
import elkfed.coref.features.pairs.FE_Copula;
import elkfed.coref.features.pairs.FE_DistanceWord;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;
import elkfed.ml.TriValued;
import elkfed.mmax.minidisc.Markable;

/**
 * Utilitiy class for sieves
 * Sieves using these methods:
 * 	- PreciseConstructsSieve
 * 
 * @author Sebastian
 *
 */

public class SieveUtilities {
	
	private static final LanguagePlugin langPlugin = ConfigProperties.getInstance().getLanguagePlugin();
	
	boolean isAppositive(PairInstance pair) {
		/*
		 * there is FE_AppositiveParse.getAppositivePrs(pair) as well
		 * might have to investigate how they differ
		 * 
		 * appositive constructions as one NP in Tï¿½Ba-D/Z
		 * switch on and off
		 * 
		 * there is also FE_Appositive.getAppositive(pair)
		 */
		if (FE_AppositiveParse.getAppositivePrs(pair)) {
			System.out.println("APPOSITIVE");
			return true;
		}
		return false;
	}

	boolean isPredicateNominative(PairInstance pair) {
		if (FE_Copula.getCopula(pair)){
			System.out.println("PREDICATE NOMINATIVE");
			return true;
		}
		return false;
	}		
	
	boolean isRoleAppositive(PairInstance pair) {
		Mention mention = pair.getAnaphor();
		Mention antecedent = pair.getAntecedent();
		
		if (mention.getProperName() && // check if person
				isAnimate(antecedent) && // check if animate 
				!isNeutral(antecedent) && // check if neutral
				mention.embeds(antecedent)) { // check if antecedent is contained in mention
			System.out.println("ROLE APPOSITIVE");
			return true;
		}
		return false;
	}

	boolean isAnimate(Mention mention) {
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
		// (c) check with animate and inanimate lists
		for (int i = 0; i < tokens.length; i++) {
			/* at the moment, one token suffices to be animate or inanimate to
			 * arrive at a decision; maybe only consider the head word? 
			 */
			if (langPlugin.isInAnimateList(tokens[i])) {
				return true;
			}
			else if (langPlugin.isInInanimateList(tokens[i])) {
				return false;
			}
		}		
		return false;
	}
	
	boolean isNeutral(Mention mention) {
		
		if (SemanticClass.isaPerson(mention.getSemanticClass())) {
			return false;
		}
		String[] tokens = mention.getMarkable().getDiscourseElements();
			
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i];
			// same comment as loop above
			if (langPlugin.isInNeutralList(t)) {
				return true;
			}
			else if (langPlugin.isInMaleList(t) || langPlugin.isInFemaleList(t)) {
				return false;
			}
		}
		return false;
	}

	boolean isRelativePronoun(PairInstance pair) {
		Mention mention = pair.getAnaphor();
		Mention antecedent = pair.getAntecedent();
		
		String[] tokens = antecedent.getMarkable().getDiscourseElements();
		if (tokens.length == 1 && tokens[0].matches(RELATIVE_PRONOUN)) {
			// mention is contained in antecedent
			if (antecedent.embeds(mention)) {
				System.out.println("RELATIVE PRONOUN");
				return true;	
			}
		}
		return false;
	}
	
	boolean isAcronym(PairInstance pair) {
		/**
		 * Checks if one mention is an acronym of the other mention
		 * and vice versa.
		 */
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		return checkOneWayAcronym(mention, antecedent) || checkOneWayAcronym(antecedent, mention);
	}
	
	boolean checkOneWayAcronym(String acronym, String expression) {
		/**
		 * Checks if one string is an acronym of the other string
		 */
		if (acronym.toUpperCase().equals(acronym)) {
			String initials = "";
			for (String word : expression.split(" ")) {
				initials += word.substring(0,1).toUpperCase();
			}
			if (acronym.equals(initials)) {
				System.out.println("ACRONYM");
				return true;
			}
		}
		return false;
	}

	boolean isDemonym(PairInstance pair) {
		/**
		 * Check if one expression is a demonym of the other using
		 * a static list of countries and their gentilic forms from
		 * Wikipedia
		 */
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		String mention_lookup = langPlugin.lookupAlias(mention, TableName.DemonymMap);
		String antecedent_lookup = langPlugin.lookupAlias(antecedent, TableName.DemonymMap);
		
		if ((mention_lookup != null && mention_lookup.equals(antecedent)) ||
				(antecedent_lookup != null && antecedent_lookup.equals(mention))) {
			System.out.println("DEMONYM");
			return true;
		}
		return false;		
	}		

	
	boolean sentenceDistance(PairInstance pair){
		//FE_SentenceDistance sd = new FE_SentenceDistance();
		if(FE_SentenceDistance.getSentDist(pair) < 4){
			return true;
		}
		return false;
	}
	
	boolean animacyAgreement(PairInstance pair){
		if (isAnimate(pair.getAnaphor()) == isAnimate(pair.getAntecedent())){
			return true;
		}
		return false;
		
	}
	
	boolean genderAgreement(PairInstance pair){
		
		if (FE_Gender.getGender(pair).equals(TriValued.TRUE)){
			return true;
		}
		return false;
		
	}
	
	boolean numberAgreement(PairInstance pair){
		if (FE_Number.getNumber(pair)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * Person â€“ we assign person attributes only to pronouns. We do not enforce
		this constraint when linking two pronouns, however, if one appears within
		quotes. This is a simple heuristic for speaker detection (e.g., I and she point
		to the same person in â€œ[I] voted my conscience,â€� [she] said).
	 */
	
	public void personAgreement(PairInstance pair){
		// missing
			
		}
	
	boolean NERAgreement(PairInstance pair){
		if (	(pair.getAnaphor().getSemanticClass().equals(pair.getAntecedent().getSemanticClass())) || 
				(pair.getAnaphor().equals(SemanticClass.UNKNOWN)) ||
				(pair.getAntecedent().equals(SemanticClass.UNKNOWN))){
			return true;
		}
		
		return false;
			
	}
	

	
	boolean IWithinI(Mention m, Mention ante){
		PairInstance pair = new PairInstance(m, ante);
		if (!isAppositive(pair) && !isRelativePronoun(pair) && !isRoleAppositive(pair)){
			if (pair.getAnaphor().embeds(pair.getAntecedent()) || pair.getAntecedent().embeds(pair.getAnaphor())){
				return true;
			}
		}
		return false;
	}
		
	
	
	boolean noNumericMismatch(PairInstance pair){
		Set<String> Numbers = Generics.newHashSet(Arrays.asList(new String[]{"eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun", "zehn", "elf", "zwölf", "hundert", "tausend", "million", "milliarde"}));
		for (String s: Numbers){
			if ((pair.getAnaphor().toString().contains(s) && !pair.getAntecedent().toString().contains(s)) || (pair.getAntecedent().toString().contains(s) && !pair.getAnaphor().toString().contains(s))){
				return false;
			}
		}
		
		return true;
	}
	
	
	public boolean entityHeadMatch(Mention m ,Mention ante) {
		DiscourseEntity d = m.getDiscourseEntity();
		DiscourseEntity dAnte = ante.getDiscourseEntity();
		Set<String> entityHeads = d.getHeads();
		for (String headAnte : dAnte.getHeads()) {
			if (entityHeads.contains(headAnte)) {
				return true;
			}
		}		
		return false;		
	}
	public boolean wordInclusion(Mention m, Mention ante) {
		
		Set<String> dWords = m.getDiscourseEntity().getWords();
		Set<String> dAnteWords = ante.getDiscourseEntity().getWords();
		
		if (dAnteWords.containsAll(dWords)) {			
			return true;
		}
		
		return false;
		
	}
	
	public boolean compatibleModifiers(Mention m, Mention ante) {
		Set<Tree> dMod = m.getDiscourseEntity().getModifiers();
		Set<Tree> dAnteMod = ante.getDiscourseEntity().getModifiers();
		
		if (dAnteMod.containsAll(dAnteMod)) {
			return true;
		}
		return false;
	}
	
	
	int getMarkableDistance(PairInstance pair){
		return pair.getAnaphor().getMarkable().getIntID()-pair.getAntecedent().getMarkable().getIntID();
	}
	
}
