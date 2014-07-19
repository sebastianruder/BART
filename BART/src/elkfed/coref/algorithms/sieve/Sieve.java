package elkfed.coref.algorithms.sieve;

import java.util.List;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;
import elkfed.ml.TriValued;
import elkfed.mmax.minidisc.Markable;
import elkfed.nlp.util.Gender;

/**
 * 
 * @author Xenia Kühling, Julian Baumann, Sebastian Ruder
 * 
 */

public abstract class Sieve {

	// sieve utility class
	protected String name; // name of sub class
	// list of antecedents/potential coreferents
	protected List<Mention> mentions;

	/**
	 * Abstract method that uses the particular rules of a sieve to look for an
	 * antecedent for mention in the list of mentions that was provided to the
	 * sieve.
	 * 
	 * @param mention
	 *            the mention whose antecedent is sought
	 * @return index of antecedent; -1 if no antecedent is found
	 */
	abstract int runSieve(Mention mention);

	/**
	 * Returns name of sieve
	 * 
	 * @return name of the sieve
	 */
	String getName() {
		return this.name;
	}

	// language plugin is retrieved
	private static final LanguagePlugin langPlugin = ConfigProperties
			.getInstance().getLanguagePlugin();

	/**
	 * Checks if a mention and its antecedent are in an appositive construction
	 * <p>
	 * Appositive constructions don't appear in TüBa-D/Z; here they form one
	 * mention.
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
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
	 * Checks if mention and antecedent are in a copulative subject-object
	 * relation
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isPredicateNominative(PairInstance pair) {
		if (FE_Copula.getCopula(pair)) {
			System.out.println("PREDICATE NOMINATIVE");
			return true;
		}
		return false;
	}

	/**
	 * Checks if mention and antecedent are in a role appositive construction
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isRoleAppositive(PairInstance pair) {
		Mention mention = pair.getAnaphor();
		Mention antecedent = pair.getAntecedent();

		if (mention.getProperName() && // check if person
				isAnimate(antecedent) && // check if animate
				!isNeutral(antecedent) && // check if neutral
				mention.embeds(antecedent)) { // check if antecedent is
												// contained in mention
			System.out.println("ROLE APPOSITIVE");
			return true;
		}
		return false;
	}

	/**
	 * Checks if mention is animate
	 * <p>
	 * Animacy is set using: (a) a static list for pronouns; (b) NER and Gender
	 * labels (e.g., PERSON and MALE are animate); and (c) lists of animate and
	 * inanimate unigrams taken from
	 * https://github.com/castiron/didh/tree/master/lib/vendor/snlp/dcoref;
	 * German lists have been translated using Google Tranlate
	 * 
	 * @param mention
	 *            mention
	 * @return true or false
	 */
	boolean isAnimate(Mention mention) {
		// (a) check with pronoun list
		// not necessary for isRoleAppositive, maybe for other applications
		String[] tokens = mention.getMarkable().getDiscourseElements();
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i].toLowerCase();
			if (t.matches(MALE_PRONOUN_ADJ) || t.matches(FEMALE_PRONOUN_ADJ)
					|| t.matches(FIRST_PERSON_SG_PRO)
					|| t.matches(FIRST_PERSON_PL_PRO)
					|| t.matches(SECOND_PERSON_PRO)) {
				return true;
			}
		}
		// (b) check with NER and Gender labels
		if (SemanticClass.isaPerson(mention.getSemanticClass())
				|| mention.getGender().equals(Gender.MALE)
				|| mention.getGender().equals(Gender.FEMALE)) {
			return true;
		} else if (SemanticClass.isaObject(mention.getSemanticClass())
				|| SemanticClass.isaNumeric(mention.getSemanticClass())
				|| mention.getGender().equals(Gender.NEUTRAL)) {
			return false;
		}
		// (c) check with animate and inanimate lists
		for (int i = 0; i < tokens.length; i++) {
			/*
			 * at the moment, one token suffices to be animate or inanimate to
			 * arrive at a decision; maybe only consider the head word?
			 */
			if (langPlugin.isInAnimateList(tokens[i])) {
				return true;
			} else if (langPlugin.isInInanimateList(tokens[i])) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Checks if mention is neutral.
	 * <p>
	 * Uses NER and Gender labels, and male, female, and neutral lists taken
	 * from https://github.com/castiron/didh/tree/master/lib/vendor/snlp/dcoref;
	 * German lists have been translated using Google Tranlate
	 * 
	 * @param mention
	 *            mention
	 * @return true or false
	 */
	boolean isNeutral(Mention mention) {
		if (SemanticClass.isaPerson(mention.getSemanticClass())
				|| mention.getGender().equals(Gender.MALE)
				|| mention.getGender().equals(Gender.FEMALE)) {
			return false;
		}
		String[] tokens = mention.getMarkable().getDiscourseElements();

		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i];
			// same comment as loop above
			if (langPlugin.isInNeutralList(t)) {
				return true;
			} else if (langPlugin.isInMaleList(t)
					|| langPlugin.isInFemaleList(t)) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Checks if the mention is a relative pronoun and modifies the antecedent.
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
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
		} else if (langPlugin instanceof EnglishLanguagePlugin) {
			relative_pronouns = EnglishLinguisticConstants.RELATIVE_PRONOUN;
		}

		if (tokens.length == 1 && tokens[0].matches(relative_pronouns)) {
			String rp = tokens[0];
			Gender gender = Gender.UNKNOWN;
			if (rp.equals("die") || rp.equals("welche")) {
				gender = Gender.FEMALE;
			} else if (rp.equals("der") || rp.equals("welcher")) {
				gender = Gender.MALE;
			} else if (rp.equals("das") || rp.equals("welches")) {
				gender = Gender.NEUTRAL;
			} else if (rp.equals("deren")) {
				gender = Gender.PLURAL;
			}
			int word_distance = m1.getLeftmostDiscoursePosition()
					- m2.getRightmostDiscoursePosition();
			// word distance = 3 means there is one word between mention and
			// antecedent
			if (word_distance <= 3 && gender.equals(antecedent.getGender())) {
				System.out
						.println(String
								.format("RELATIVE PRONOUN! %s and %s (Distance: %d, Gender: %s)",
										mention.getMarkable().getID(),
										antecedent.getMarkable().getID(),
										word_distance, antecedent.getGender()));
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if one mention is an acronym of the other mention and vice versa.
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false;
	 */
	boolean isAcronym(PairInstance pair) {
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		return checkOneWayAcronym(mention, antecedent)
				|| checkOneWayAcronym(antecedent, mention);
	}

	/**
	 * Checks if one string is an acronym of the other string
	 * 
	 * @param acronym
	 *            String thought to be an acronym
	 * @param expression
	 *            String whose initials are thought to form said acronym
	 * @return true or false
	 */
	boolean checkOneWayAcronym(String acronym, String expression) {
		if (acronym.toUpperCase().equals(acronym)) {
			String initials = "";
			for (String word : expression.split(" ")) {
				initials += word.substring(0, 1).toUpperCase();
			}
			if (acronym.equals(initials)) {
				System.out.println("ACRONYM");
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if one expression is a demonym of the other using a static list of
	 * countries and their gentilic forms from Wikipedia
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean isDemonym(PairInstance pair) {
		String mention = pair.getAnaphor().toString();
		String antecedent = pair.getAntecedent().toString();
		String mention_lookup = langPlugin.lookupAlias(mention,
				TableName.DemonymMap);
		String antecedent_lookup = langPlugin.lookupAlias(antecedent,
				TableName.DemonymMap);

		if ((mention_lookup != null && mention_lookup.equals(antecedent))
				|| (antecedent_lookup != null && antecedent_lookup
						.equals(mention))) {
			System.out.println("DEMONYM");
			return true;
		}
		return false;
	}

	/**
	 * Compute if sentences in which mention and antecedent appear are not more
	 * than 4 sentences apart.
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */

	public boolean sentenceDistance(PairInstance pair) {
		// FE_SentenceDistance sd = new FE_SentenceDistance();
		if (FE_SentenceDistance.getSentDist(pair) < 4) {
			return true;
		}
		return false;
	}

	/**
	 * Check if mention and antecedent are both in the same state of animacy
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */

	public boolean animacyAgreement(PairInstance pair) {
		if (isAnimate(pair.getAnaphor()) == isAnimate(pair.getAntecedent())) {
			return true;
		}
		return false;

	}

	/**
	 * Check if mention and antecedent have the same gender
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */
	public boolean genderAgreement(PairInstance pair) {

		if (FE_Gender.getGender(pair).equals(TriValued.TRUE)) {
			return true;
		}
		return false;

	}

	/**
	 * Check if mention and antecedent have the same numerus (plural/ singular)
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */

	public boolean numberAgreement(PairInstance pair) {
		if (FE_Number.getNumber(pair)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * Person â€“ we assign person attributes only to pronouns. We do not
	 * enforce this constraint when linking two pronouns, however, if one
	 * appears within quotes. This is a simple heuristic for speaker detection
	 * (e.g., I and she point to the same person in â€œ[I] voted my
	 * conscience,â€� [she] said).
	 */

	public void personAgreement(PairInstance pair) {
		// missing

	}

	/**
	 * Check if mention and antecedent belong to the same semantic class or
	 * mention's or antecedent's semantic class equals "unknown"
	 * 
	 * @param pair
	 *            PairInstance of mention, antecedent
	 * @return true or false
	 */
	boolean NERAgreement(PairInstance pair) {
		if ((pair.getAnaphor().getSemanticClass().equals(pair.getAntecedent()
				.getSemanticClass()))
				|| (pair.getAnaphor().getSemanticClass()
						.equals(SemanticClass.UNKNOWN))
				|| (pair.getAntecedent().getSemanticClass()
						.equals(SemanticClass.UNKNOWN))) {
			return true;
		}

		return false;

	}

	/**
	 * 
	 * 
	 * @param mention
	 * @param ante
	 * @return
	 */

	boolean IWithinI(Mention mention, Mention ante) {
		PairInstance pair = new PairInstance(mention, ante);
		if (!isAppositive(pair) && !isRelativePronoun(pair)
				&& !isRoleAppositive(pair)) {
			if (pair.getAnaphor().embeds(pair.getAntecedent())
					|| pair.getAntecedent().embeds(pair.getAnaphor())) {
				return true;
			}
		}
		return false;
	}

	public boolean noNumericMismatch(PairInstance pair) {
		Set<String> mentionWords = new HashSet<String>();
		Set<String> anteWords = new HashSet<String>();

		mentionWords = pair.getAnaphor().getDiscourseEntity().getWords();
		anteWords = pair.getAntecedent().getDiscourseEntity().getWords();

		for (String mentionWord : mentionWords) {
			if (mentionWord
					.toLowerCase()
					.matches(
							".*(eins|zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf|hundert|tausend|million|milliarde).*")
					|| mentionWord.matches(".*[0-9].*")) {
				if (!anteWords.contains(mentionWord)) {
					return false;
				}

				else {

					for (String anteWord : anteWords) {
						if (anteWord
								.toLowerCase()
								.matches(
										".*(eins|zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf|hundert|tausend|million|milliarde).*")
								|| anteWord.matches(".*[0-9].*")) {
							if (!mentionWords.contains(anteWord)) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public boolean noLocationMismatch(PairInstance pair) {
		Set<String> mentionWords = new HashSet<String>();
		Set<String> anteWords = new HashSet<String>();

		mentionWords = pair.getAnaphor().getDiscourseEntity().getWords();
		anteWords = pair.getAntecedent().getDiscourseEntity().getWords();

		Map<String, String> mentionWordsPOS = new HashMap<String, String>();
		Map<String, String> anteWordsPOS = new HashMap<String, String>();

		for (int i = 0; i < pair.getAnaphor().getMarkable().toString()
				.split("\\s+").length; i++) {
			mentionWordsPOS
					.put(pair.getAnaphor().getMarkable().toString()
							.replace("[", "").replace("]", "").split("\\s+")[i],
							pair.getAnaphor()
									.getJoinedStringFromDiscIds(
											pair.getAnaphor()
													.getMarkable()
													.getLeftmostDiscoursePosition() + 1,
											pair.getAnaphor()
													.getMarkable()
													.getRightmostDiscoursePosition() + 1,
											"pos").split("\\s+")[i]);
		}

		for (int i = 0; i < pair.getAntecedent().getMarkable().toString()
				.split("\\s+").length; i++) {
			anteWordsPOS
					.put(pair.getAntecedent().getMarkable().toString()
							.replace("[", "").replace("]", "").split("\\s+")[i],
							pair.getAntecedent()
									.getJoinedStringFromDiscIds(
											pair.getAntecedent()
													.getMarkable()
													.getLeftmostDiscoursePosition() + 1,
											pair.getAntecedent()
													.getMarkable()
													.getRightmostDiscoursePosition() + 1,
											"pos").split("\\s+")[i]);
		}

		for (String mentionWord : mentionWords) {
			if (mentionWordsPOS.get(mentionWord) != null) {
				if (mentionWordsPOS.get(mentionWord).equalsIgnoreCase("ne")
						&& !anteWords.contains(mentionWord)) {
					return false;
				}

				else {
					if (mentionWord
							.matches("(nördlich.*|südlich.*|.*westlich.*|.*östlich.*|obere.*|niedere.*)")) {
						if (!anteWords.contains(mentionWord)) {
							return false;
						}

						else {
							for (String anteWord : anteWords) {
								if (anteWordsPOS.get(anteWord) != null) {
									if (anteWordsPOS.get(anteWord)
											.equalsIgnoreCase("ne")
											&& !mentionWords.contains(anteWord)) {
										return false;
									}

									else {
										if (anteWord
												.matches("(nördlich.*|südlich.*|.*westlich.*|.*östlich.*|obere.*|niedere.*)")) {
											if (!mentionWords
													.contains(anteWord)) {
												return false;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	/*
	 * public boolean noLocationMismatch(PairInstance pair){ Set<String>
	 * mentionWords = new HashSet<String>(); Set<String> anteWords = new
	 * HashSet<String>();
	 * 
	 * mentionWords = pair.getAnaphor().getDiscourseEntity().getWords();
	 * anteWords = pair.getAntecedent().getDiscourseEntity().getWords();
	 * 
	 * String[] mentionWordsPOS =
	 * pair.getAnaphor().getJoinedStringFromDiscIds(pair
	 * .getAnaphor().getMarkable().getLeftmostDiscoursePosition() +1,
	 * pair.getAnaphor().getMarkable().getRightmostDiscoursePosition() +1,
	 * "pos").split("\\s+"); String[] anteWordsPOS =
	 * pair.getAntecedent().getJoinedStringFromDiscIds
	 * (pair.getAntecedent().getMarkable().getLeftmostDiscoursePosition() +1,
	 * pair.getAntecedent().getMarkable().getRightmostDiscoursePosition() +1,
	 * "pos").split("\\s+");
	 * 
	 * int count = 0;
	 * 
	 * for (int i = 0; i < mentionWordsPOS.length; i++){ if
	 * (mentionWordsPOS[i].equalsIgnoreCase("ne")){ count++; } if (count > 1){
	 * return false; }
	 * 
	 * else { count = 0; for (int j = 0; j < anteWordsPOS.length; j++){ if
	 * (anteWordsPOS[j].equalsIgnoreCase("ne")){ count++; }
	 * 
	 * if (count > 1){ return false; }
	 * 
	 * else { for (String mentionWord: mentionWords){ if (mentionWord.matches(
	 * "(nördlich.*|südlich.*|.*westlich.*|.*östlich.*|obere.*|niedere.*)")){ if
	 * (!anteWords.contains(mentionWord)){ return false; } }
	 * 
	 * else { for (String anteWord: anteWords){ if (anteWord.matches(
	 * "(nördlich.*|südlich.*|.*westlich.*|.*östlich.*|obere.*|niedere.*)")){ if
	 * (!mentionWords.contains(anteWord)){ return false; } } } } } } } } }
	 * return true; }
	 */

	public boolean entityHeadMatch(Mention m, Mention ante) {

		if (m.getPronoun() || ante.getPronoun()) {
			return false;
		}
		String mHead = m.getHeadString();
		Set<String> dAnteHeads = ante.getDiscourseEntity().getHeads();
		if (dAnteHeads.contains(mHead)) {
			return true;
		}
		return false;
	}

	public boolean properNameAgreement(Mention m, Mention ante) {
		if (m.getProperName() || ante.getProperName()) {
			if ((m.getSemanticClass().equals(ante.getSemanticClass()))
					&& (!m.getSemanticClass().equals(SemanticClass.UNKNOWN))) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean relaxedEntityHeadMatch(Mention m, Mention ante) {

		if (m.getPronoun() || ante.getPronoun()) {
			return false;
		}
		String mHead = m.getHeadString();
		if (langPlugin.isInStopwordList(mHead)) {
			return false;
		}
		Set<String> dAnteWords = ante.getDiscourseEntity().getWords();
		if (dAnteWords.contains(mHead)) {
			return true;
		}
		return false;

	}

	public boolean wordInclusion(Mention m, Mention ante) {

		Set<String> mWords = new HashSet<String>();

		for (String token : m.getMarkable().getDiscourseElements()) {
			if (!(langPlugin.isInStopwordList(token))) {
				mWords.add(token);
			}
			
		}
		Set<String> dAnteWords = ante.getDiscourseEntity().getWords();
		
		if (dAnteWords.containsAll(mWords)) {
			return true;
		}

		return false;

	}

	public boolean compatibleModifiers(Mention m, Mention ante) {
		List<Tree> mentionMod = m.getPremodifiers();
		mentionMod.addAll(m.getPostmodifiers());
		Set<Tree> dAnteMod = ante.getDiscourseEntity().getModifiers();
//		if (mentionMod.size() == 0) {
//			return false;
//		}
		if (dAnteMod.containsAll(mentionMod)) {
			return true;
		}
		return false;
	}

	public int getMarkableDistance(PairInstance pair) {
		return pair.getAnaphor().getMarkable().getIntID()
				- pair.getAntecedent().getMarkable().getIntID();
	}

	/**
	 * Check if mention is a speaker
	 * 
	 * @param mention
	 * @return
	 */
	public boolean isSpeaker(Mention mention) {
		int extendedMentionSpanLeft;
		int extendedMentionSpanRight;
		int MentionSpanLeft = mention.getMarkable()
				.getLeftmostDiscoursePosition() + 1;
		int MentionSpanRight = mention.getMarkable()
				.getRightmostDiscoursePosition() + 1;

		String joinedMentionExtendedString = "";
		String joinedMentionString = "";

		if (MentionSpanLeft - 10 <= 0) {
			extendedMentionSpanLeft = mention.getSentenceStart() + 1;
		} else {
			extendedMentionSpanLeft = MentionSpanLeft - 10;
		}

		if (MentionSpanRight + 10 > mention.getSentenceEnd()) {
			extendedMentionSpanRight = mention.getSentenceEnd();
		} else {
			extendedMentionSpanRight = MentionSpanRight + 10;
		}

		if (FE_Speech.isMentionInSpeech(mention)) {
			return false;
		}

		joinedMentionExtendedString = mention.getJoinedStringFromDiscIds(
				extendedMentionSpanLeft, extendedMentionSpanRight, "lemma");
		joinedMentionString = mention.getJoinedStringFromDiscIds(
				MentionSpanLeft, MentionSpanRight, "lemma");

		if (joinedMentionExtendedString != null && joinedMentionString != null) {
			String[] words = joinedMentionExtendedString.replaceAll(
					"[^A-Za-z0-9äöüÄÖÜ# ]", "").split("\\s+");
			String[] mentionWords = joinedMentionString.replaceAll(
					"[^A-Za-zäöüÄÖÜ0-9# ]", "").split("\\s+");

			for (int i = 0; i < words.length; i++) {
				if (langPlugin.isInSpeechVerbList(words[i])) {
					if (i == words.length - 1) {
						if (words[i - 1]
								.equals(mentionWords[mentionWords.length - 1])) {
							return true;
						} else
							return false;
					}

					if (i == 0 && mentionWords.length > 1) {
						if (words[i + 1].equals(mentionWords[0])
								&& words[i + 2].equals(mentionWords[1])) {
							return true;
						}

						else
							return false;
					}

					if (i == 0 && mentionWords.length == 1) {
						if (words[i + 1].equals(mentionWords[0])) {
							return true;
						}

						else
							return false;
					}

					if (i != 0 && i != words.length - 1) {
						if ((words[i - 1]
								.equals(mentionWords[mentionWords.length - 1]))
								|| (words[i + 1].equals(mentionWords[0]))) {
							return true;
						} else
							return false;
					}
				}

				if (words[i].equals("so")) {
					if (i == words.length - 1) {
						return false;
					}

					if (i == words.length - 2) {
						if (words[i + 1].equals(mentionWords[0])) {
							return true;
						}

						else
							return false;
					}

					if (i != words.length - 1 && i != words.length - 2
							&& mentionWords.length > 1) {
						if (words[i + 1].equals(mentionWords[0])
								&& words[i + 2].equals(mentionWords[1])) {
							return true;
						}

						else
							return false;
					}

					if (i != words.length - 1 && i != words.length - 2
							&& mentionWords.length == 1) {
						if (words[i + 1].equals(mentionWords[0])) {
							return true;
						}

						else
							return false;
					}
				}
			}
		}

		return false;
	}

	public boolean isVorfeldEs(Mention mention) {
		if (!(mention.getMarkable().toString().equals("[es]") || mention
				.getMarkable().toString().equals("[Es]"))) {
			return false;
		}

		else {
			if (mention
					.getSentenceTree()
					.toString()
					.matches(
							"\\(Start(.*)\\([VM]F \\(NX \\(PPER [Ee]s\\)\\)\\)(.*)")) {
				return true;
			}
		}

		return false;

	}
}
