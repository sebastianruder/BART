package elkfed.coref.algorithms.sieve;

import static elkfed.lang.EnglishLinguisticConstants.FEMALE_PRONOUN_ADJ;
import static elkfed.lang.EnglishLinguisticConstants.FIRST_PERSON_PL_PRO;
import static elkfed.lang.EnglishLinguisticConstants.FIRST_PERSON_SG_PRO;
import static elkfed.lang.EnglishLinguisticConstants.MALE_PRONOUN_ADJ;
import static elkfed.lang.EnglishLinguisticConstants.SECOND_PERSON_PRO;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.lang.EnglishLinguisticConstants;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.lang.GermanLinguisticConstants;
import elkfed.coref.features.pairs.FE_DistanceWord;

import java.util.Arrays;
import java.util.Set;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;
import elkfed.config.ConfigProperties;
import elkfed.coref.PairInstance;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.features.pairs.FE_AppositiveParse;
import elkfed.coref.features.pairs.FE_Copula;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;
import elkfed.ml.TriValued;
import elkfed.mmax.minidisc.Markable;
import elkfed.nlp.util.Gender;

/**
 * Utilitiy class for sieves
 * 
 * @author Sebastian
 */
public class SieveUtilities {
	
	// language plugin is retrieved
	private static final LanguagePlugin langPlugin = ConfigProperties.getInstance().getLanguagePlugin();
	
	/**
	 * Checks if a mention and its antecedent are in
	 * an appositive construction
	 * <p>
	 * Appositive constructions don't appear in TüBa-D/Z;
	 * here they form one mention.
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isAppositive(PairInstance pair) {
		if (FE_AppositiveParse.getAppositivePrs(pair)) {
			System.out.println("APPOSITIVE");
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if mention and antecedent are in a
	 * copulative subject-object relation
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isPredicateNominative(PairInstance pair) {
		if (FE_Copula.getCopula(pair)){
			System.out.println("PREDICATE NOMINATIVE");
			return true;
		}
		return false;
	}		
	
	/**
	 * Checks if mention and antecedent are in a role
	 * appositive construction
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false
	 */
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
	
	/**
	 * Checks if mention is animate
	 * <p>
	 * Animacy is set using:
	 * (a) a static list for pronouns;
	 * (b) NER and Gender labels (e.g., PERSON and MALE are animate); and
	 * (c) lists of animate and inanimate unigrams
	 * 	   taken from https://github.com/castiron/didh/tree/master/lib/vendor/snlp/dcoref;
	 * 	   German lists have been translated using Google Tranlate
	 * @param mention mention
	 * @return true or false
	 */
	boolean isAnimate(Mention mention) {
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
		// (b) check with NER and Gender labels
		if (SemanticClass.isaPerson(mention.getSemanticClass()) ||
				mention.getGender().equals(Gender.MALE) ||
				mention.getGender().equals(Gender.FEMALE)) {
			return true;
		}
		else if (SemanticClass.isaObject(mention.getSemanticClass()) ||
				SemanticClass.isaNumeric(mention.getSemanticClass()) ||
				mention.getGender().equals(Gender.NEUTRAL)) {
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
	
	/**
	 * Checks if mention is neutral.
	 * <p>
	 * Uses NER and Gender labels, and male, female, and neutral lists
	 * taken from https://github.com/castiron/didh/tree/master/lib/vendor/snlp/dcoref;
	 * German lists have been translated using Google Tranlate
	 * @param mention mention
	 * @return true or false
	 */
	boolean isNeutral(Mention mention) {
		if (SemanticClass.isaPerson(mention.getSemanticClass()) ||
				mention.getGender().equals(Gender.MALE) ||
				mention.getGender().equals(Gender.FEMALE)) {
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
	
	/**
	 * Checks if the mention is a relative pronoun and
	 * modifies the antecedent.
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isRelativePronoun(PairInstance pair) {
		Mention mention = pair.getAnaphor();
		Mention antecedent = pair.getAntecedent();
		Markable m1 = mention.getMarkable();
		Markable m2 = antecedent.getMarkable();
		String relative_pronouns = "";
		String[] tokens = mention.getMarkable().getDiscourseElements();
		// checks langPlugin to know which relative pronouns to use
		if (langPlugin instanceof GermanLanguagePlugin) {
			relative_pronouns = GermanLinguisticConstants.RELATIVE_PRONOUN;
		}
		else if (langPlugin instanceof EnglishLanguagePlugin) {
			relative_pronouns = EnglishLinguisticConstants.RELATIVE_PRONOUN;
		}
		
		if (tokens.length == 1 && tokens[0].matches(relative_pronouns)) {
			String rp = tokens[0];
			Gender gender = Gender.UNKNOWN;
			if (rp.equals("die") || rp.equals("welche")) {
				gender = Gender.FEMALE;
			}
			else if (rp.equals("der") || rp.equals("welcher")) {
				gender = Gender.MALE;
			}
			else if (rp.equals("das") || rp.equals("welches")) {
				gender = Gender.NEUTRAL;
			}
			else if (rp.equals("deren")) {
				gender = Gender.PLURAL;
			}
			int word_distance = m1.getLeftmostDiscoursePosition() - m2.getRightmostDiscoursePosition();
			// word distance = 3 means there is one word between mention and antecedent
			if (word_distance <= 3 && gender.equals(antecedent.getGender())) {
				System.out.println(String.format("RELATIVE PRONOUN! %s and %s (Distance: %d, Gender: %s)",
						mention.getMarkable().getID(), antecedent.getMarkable().getID(), word_distance, antecedent.getGender()));
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if one mention is an acronym of the other mention
	 * and vice versa.
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false;
	 */
	boolean isAcronym(PairInstance pair) {
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		return checkOneWayAcronym(mention, antecedent) || checkOneWayAcronym(antecedent, mention);
	}
	
	/**
	 * Checks if one string is an acronym of the other string
	 * @param acronym String thought to be an acronym
	 * @param expression String whose initials are thought
	 * to form said acronym
	 * @return true or false
	 */
	boolean checkOneWayAcronym(String acronym, String expression) {
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
	
	/**
	 * Check if one expression is a demonym of the other using
	 * a static list of countries and their gentilic forms from
	 * Wikipedia
	 * @param pair PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isDemonym(PairInstance pair) {
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
