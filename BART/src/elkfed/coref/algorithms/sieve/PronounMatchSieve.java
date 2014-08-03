package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xalan.internal.utils.FeatureManager.Feature;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_Speech;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.nlp.util.Gender;
import elkfed.nlp.util.Number;

/**
 * 
 * 
 * @author Xenia
 * 
 */

public class PronounMatchSieve extends Sieve {

	public PronounMatchSieve(List<Mention> mentions) {
		this.mentions = mentions;
		this.name = "PronounMatchSieve";
	}

	private List<Mention> getAntecedents(Mention m) {
		// Kataphern noch berücksichtigen??
		List<Mention> anteIdx = new ArrayList<>();
		for (int idx = mentions.indexOf(m); idx > 0; idx--) {

			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(m, ante);
			if (IWithinI(pair)) {
				continue;
			}
			if (FE_SentenceDistance.getSentDist(pair) > 3) {
				continue;
			}
			if (m.getReflPronoun() && !isInCooargumentDomain(pair)) {
				continue;
			}
			if (m.getReflPronoun() && !isAnimate(ante)) {
				continue;
			}
			// if (m.getPersPronoun() && isInCooargumentDomain(pair)) {
			// continue;
			// }
			if (!numberAgreement(pair) || !genderAgreement(pair)) {
				continue;
			}
			// VorfeldEs Methode könnte man noch verbessern (analog zu
			// EnglishLanguagePlugin.isExplitiveRB())
			if (isVorfeldEs(ante) || ante.getReflPronoun()) {
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

	private int scorePair(PairInstance pair) {
		Mention ante = pair.getAntecedent();
		Mention m = pair.getAnaphor();

		int score = 0;
		if (FE_SentenceDistance.getSentDist(pair) == 0) {
			score += 20;
		}
		// Head - Bonus
//		Tree sentenceTree = ante.getSentenceTree();
//		List<Tree> domPath = sentenceTree.dominationPath(ante.getHighestProjection());
//		for (int i = domPath.size()-2; i >= 0; i--) {
//			if(domPath.get(i).value().equals("SIMPX")) {
//				score += 80;
//				System.out.println("works");
//				break;
//			} 
//			if(domPath.get(i).value().equals("NX")) {
//				break;
//			}
//			
//		}

		if (ante.getDiscourseElementsByLevel("deprel").contains("SUBJ")) {
			score += 170;
		} else {
			if (ante.getDiscourseElementsByLevel("deprel").contains("OBJA")) {
				score += 70;
			} else {
				score += 50;
			}
			
		}


		return score;
	}

	public int runSieve(Mention mention) {
		if (!mention.getPronoun()) {
			return -1;
		}
		if (mention.getRelPronoun() || isVorfeldEs(mention)) {
			return -1;
		}

		List<Mention> antecedents = getAntecedents(mention);
		System.out.println(antecedents);
		if (antecedents.isEmpty()) {
			return -1;
		}
		if (antecedents.size() == 1) {
			return mentions.indexOf(antecedents.get(0));
		}
		int max = 0;
		Mention maxMention = antecedents.get(0);
		for(Mention m: antecedents) {
			PairInstance pair = new PairInstance(mention, m);
			if(scorePair(pair) > max) {
				maxMention = m;
				max = scorePair(pair);
			}
		}
		return mentions.indexOf(maxMention);

//		int sentDist = FE_SentenceDistance.getSentDist(new PairInstance(
//				mention, antecedents.get(0)));
//
//		// always take leftmost Antecedent in a Sentence
//		int idx = 1;
//		while (FE_SentenceDistance.getSentDist(new PairInstance(mention,
//				antecedents.get(idx))) == sentDist) {
//			idx++;
//			if (idx >= antecedents.size()) {
//				break;
//			}
//		}
//		return mentions.indexOf(antecedents.get(idx - 1));
//
	}

}

/**
 * else { if (FE_SentenceDistance.getSentDist(pair) == 0){
 * currentSentencePairs.add(pair); }
 * 
 * if (FE_SentenceDistance.getSentDist(pair) > 0 && sentenceDistance(pair)){
 * previousSentencePairs.add(pair);
 * 
 * } } // sieve uses Hobb's algorithm to find antecedents // starts in list of
 * anaphor/antecedent-pairs in same sentence // checks for number +
 * gender-agreement at mention nearest (markable_id difference) to pronoun //
 * stanford also uses ner-label and animacy-constraints, should check if any
 * improvement (especially ner-label)
 * 
 * for (PairInstance p: currentSentencePairs){ for (int i = 1; i < 20; i++){ if
 * (getMarkableDistance(p) == i && genderAgreement(p) && numberAgreement(p)){
 * ante_idx = mentions.indexOf(p.getAntecedent()); } }
 * 
 * // if no mention found, checks with anaphor/antecedent-pairs in previous
 * sentences // starts at one mention quite distant, should be most distant
 * mention possible if (ante_idx == -1){ for (PairInstance p2:
 * previousSentencePairs){ for (int j = 100 ;j > 0; j--){ if
 * (getMarkableDistance(p2) == j && genderAgreement(p2) && numberAgreement(p2)){
 * ante_idx = mentions.indexOf(p2.getAntecedent()); } } } } }} }
 * 
 * 
 * 
 * 
 * **/

