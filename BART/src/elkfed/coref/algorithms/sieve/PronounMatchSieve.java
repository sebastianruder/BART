package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.List;

import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.features.pairs.de.FE_Syntax_Binding;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;

/**
 * This Sieves finds antecedents for Pronouns.
 * First we find all compatible Mentions that are at most 3 sentences apart from the anapher.
 * Then we rank them according to their salience and return the most salient antecedent.
 * 
 * @author Julian, Xenia
 * 
 */
public class PronounMatchSieve extends Sieve {

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PronounMatchSieve";
	}
	
	/**
	 * 
	 * @param m Mention for which we want to find compatible antecedents  
	 * @return a List of Mentions which are compatible to the pronoun
	 */
	private List<Mention> getAntecedents(Mention m) {
		
		List<Mention> anteIdx = new ArrayList<>();

		for (int idx = 0;idx < mentions.indexOf(m); idx++) {
			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(m, ante);
			if (IWithinI(pair)) {
				continue;
			}
			if (FE_SentenceDistance.getSentDist(pair) > 3) {
				continue;
			}
			if (m.getReflPronoun() && !FE_Syntax_Binding.getAnaBoundInBindingDomain(pair)) {
				continue;
			}
			if (m.getPersPronoun() && !m.getHeadPOS().equalsIgnoreCase("pposat") &&FE_Syntax_Binding.getAnaBoundInBindingDomain(pair)) {
				continue;
			}
			
			if (!numberAgreement(pair) || !genderAgreement(pair)) {
				continue;
			}
			// VorfeldEs Methode könnte man noch verbessern (analog zu
			// EnglishLanguagePlugin.isExplitiveRB())
			if (langPlugin.isExpletiveRB(ante) || ante.getReflPronoun()) {
				continue;
			}
			if ((FE_Speech.isMentionInSpeech(pair.getAntecedent()) && !FE_Speech
					.isMentionInSpeech(pair.getAnaphor()))
					|| (!FE_Speech.isMentionInSpeech(pair.getAntecedent()) && FE_Speech
							.isMentionInSpeech(pair.getAnaphor()))) {
				continue;
			}

			anteIdx.add(mentions.get(idx));

		}
		return anteIdx;
	}
	/**
	 * Scores the Salience of the pairInstance:
	 * 	+100 if the antecedent is a named entity
	 *  +20 if mention and antecedent are in the same Sentence
	 *  +170 if the antecedent is the Subject of its clause
	 *  +70 if the antecedent is the Accusative Object of its clause
	 *  +50 if the antecedent is the Dative or Genitive Object of its clause
	 *  +35 if the mention and the Antecedent have the same grammatical function
	 * @param pair the pair{@link PairInstance} to score
	 * @return the Salience Score
	 */
	private double scorePair(PairInstance pair) {
		
		Mention ante = pair.getAntecedent();
		Mention m = pair.getAnaphor();

		double score = 0;
		//same Sentence Bonus
		int sentenceDis = FE_SentenceDistance.getSentDist(pair);
		if (sentenceDis == 0) {
			score += 20;
		}
		
		if (ante.getHeadPOS().equalsIgnoreCase("ne")) {
			score += 100;
		}
		
		// Head - Bonus
//		Tree sentenceTree = ante.getSentenceTree();		
//		if (!ante.getHighestProjection().parent(sentenceTree).value().equalsIgnoreCase("NX")) {
//			score += 80;
//		}
		//GF Bonus
		String headGF = langPlugin.getHeadGF(ante);
		if (headGF.equalsIgnoreCase("subj")) {
			score += 170;
		}
		if (headGF.equalsIgnoreCase("obja")) {
			score += 70;
		}
		if (headGF.equalsIgnoreCase("objd")) {
			score += 50;
		}
		if (headGF.equalsIgnoreCase("objg")) {
			score += 50;
		}
		if (headGF.equals(langPlugin.getHeadGF(m))) {
			score += 35;
		}
		
		score = score / Math.pow(2.0, sentenceDis);			
		
		return score;
	}

	public int runSieve(Mention mention) {
		if (!mention.getPronoun()) {
			return -1;
		}
		if (mention.getRelPronoun() || langPlugin.isExpletiveRB(mention)) {
			return -1;
		}

		List<Mention> antecedents = getAntecedents(mention);
		
		//if no compatible antecedent found, take first subject Antecedent
		//does not work well
		if (antecedents.isEmpty()) {
			return -1;
			
			
		}
		if (antecedents.size() == 1) {
			return mentions.indexOf(antecedents.get(0));
		}
		double max = Double.MIN_VALUE;
		Mention maxMention = antecedents.get(0);
		for(Mention m: antecedents) {
			PairInstance pair = new PairInstance(mention, m);
			if(scorePair(pair) >= max) {
				maxMention = m;
				max = scorePair(pair);
			}
		}
		return mentions.indexOf(maxMention);


	}

}



