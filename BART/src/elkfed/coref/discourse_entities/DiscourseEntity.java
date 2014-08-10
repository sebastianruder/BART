package elkfed.coref.discourse_entities;

import elkfed.coref.mentions.*;
import edu.stanford.nlp.trees.Tree;
import elkfed.config.ConfigProperties;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.lang.LanguagePlugin;
import java.util.*;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.nlp.util.Gender;
import elkfed.nlp.util.Number;

/**
 * A Class that functions as a Discourse Entity where all mentions belonging to the same entity
 * are jointly modeled. 
 * <p>
 * To be used in entity-centric coreference resolution.
 * 
 * @author Julian
 */
public class DiscourseEntity {

	private static final LanguagePlugin langPlugin = ConfigProperties
			.getInstance().getLanguagePlugin();

	private static int nextID = 0;
	private int ID;

	private boolean firstMention_isFirstMention;

	private TreeSet<Mention> mentions;
	private Set<String> words;
	private Set<String> heads;
	private Set<Tree> modifiers;
	private Set<Gender> genders;
	private Set<Number> numbers;
	
	/**
	 * The constructor which creates a DscourseEntity for the given mention.
	 * Should only be called at mention creation.
	 * 
	 * @param the mention for which a DiscourseEntity shall be created
	 */
	public DiscourseEntity(Mention m) {
		ID = nextID;
		nextID++;
		mentions = new TreeSet<Mention>();
		mentions.add(m);
		words = new HashSet<String>();
		addWords(m);

		heads = new HashSet<String>();
		heads.add(m.getHeadLemma());

		modifiers = new HashSet<Tree>();
		modifiers.addAll(m._premodifiers);
		modifiers.addAll(m._postmodifiers);

		genders = new HashSet<Gender>();
		genders.add(m.getGender());

		numbers = new HashSet<Number>();
		numbers.add(m.getNumberLabel());
	}

	public TreeSet<Mention> getMentions() {
		return mentions;
	}

	public Mention getFirstMention() {
		return mentions.first();
	}

	public void addWords(Mention m) {
		if (langPlugin instanceof GermanLanguagePlugin) {
			for (String word : m.getDiscourseElementsByLevel("lemma")) {
				if (!langPlugin.isInStopwordList(word)) {
					words.add(word);
				}
			}
		}

		else if (langPlugin instanceof EnglishLanguagePlugin) {
			for (String word : m.getDiscourseElementsByLevel("morph")) {
				if (!langPlugin.isInStopwordList(word)) {
					words.add(word);
				}
			}
		}
	}

	public Set<Gender> getGenders() {
		return genders;
	}

	public void setGender(Gender gender) {
		genders.add(gender);
	}

	public void addGender(Gender gender) {
		genders.add(gender);
	}

	public Set<Number> getNumbers() {
		return numbers;
	}
	/**
	 * Merges the DiscourseEntity of the given Mention with this DiscourseEntity
	 * 
	 * @param the Mention which DiscourseEntity should be merged
	 */
	public void merge(Mention ante) {

		DiscourseEntity deAnte = ante.getDiscourseEntity();
		for (Mention m : deAnte.getMentions()) {
			mentions.add(m);
			m.setDiscourseEntity(this);
			addWords(m);
		}
		genders.addAll(getGenders());
		numbers.addAll(getNumbers());
		words.addAll(deAnte.getWords());
		modifiers.addAll(deAnte.getModifiers());
		heads.addAll(deAnte.getHeads());
	}

	public Set<Tree> getModifiers() {
		return modifiers;
	}

	public String getWordsString() {
		StringBuilder w = new StringBuilder();
		for (String word : words) {
			w.append(word);
			w.append(" ");
		}
		return w.toString();
	}

	public Set<String> getHeads() {
		return heads;
	}

	public int getID() {
		return ID;
	}

	public Set<String> getWords() {
		return words;
	}

	public String getHeadsString() {
		StringBuilder w = new StringBuilder();
		for (String head : heads) {
			w.append(head);
			w.append(" ");
		}
		return w.toString();
	}

	public void set_firstMention_isFirstMention(boolean isFirstMention) {
		firstMention_isFirstMention = isFirstMention;
	}
}