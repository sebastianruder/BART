package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xalan.internal.utils.FeatureManager.Feature;

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

	private List<Integer> getAntecedentIdx(Mention m ) {
		//Kataphern noch berücksichtigen??
		List<Integer> anteIdx = new ArrayList<>();
		for (int idx = mentions.indexOf(m); idx > 0; idx--) {
			
			Mention ante = mentions.get(idx);
			PairInstance pair = new PairInstance(m, ante);

			if(FE_SentenceDistance.getSentDist(pair) > 3) {
				continue;
			}
			if (m.getReflPronoun() && !isInCooargumentDomain(pair)) {
				continue;
			}
			if (m.getReflPronoun() && !isAnimate(ante)) {
				continue;
			}
			if (m.getPersPronoun() && isInCooargumentDomain(pair)) {
				continue;
			}
			if (!numberAgreement(pair) || !genderAgreement(pair)) {
				continue;
			}
			//VorfeldEs Methode könnte man noch verbessern (analog zu  EnglishLanguagePlugin.isExplitiveRB())
			if(isVorfeldEs(ante) || ante.getReflPronoun()) {
				continue;
			}
			if ((FE_Speech.isMentionInSpeech(pair.getAntecedent()) && !FE_Speech.isMentionInSpeech(pair.getAnaphor())) ||
					(!FE_Speech.isMentionInSpeech(pair.getAntecedent()) && FE_Speech.isMentionInSpeech(pair.getAnaphor()))){
					continue;
					}
			anteIdx.add(idx);
			
		}
		return anteIdx;
	}
	
//	private int scorePair(PairInstance pair) {
//		Mention ante = pair.getAntecedent();
//		
//		int score = 0;
//		if (FE_SentenceDistance.getSentDist(pair) == 0) {
//			score += 20;
//		}
//		//Head - Bonus
//		if (!ante.getHighestProjection().parent().equals("NX")) {
//			score += 80;
//		}
//		
//		
//	}

	public int runSieve(Mention mention) {
		if (!mention.getPronoun()) {
			return -1;
		}
		if (mention.getRelPronoun() || isVorfeldEs(mention)){
			return -1;
		}
		
		List<Integer> antecedents = getAntecedentIdx(mention);
		System.out.println(antecedents);
		if (antecedents.isEmpty()) {
			return -1;
		}
		
		//hier könnte man jetzt noch ein Ranking machen. 
		//da wir aber die Grammatischen Funktionen nicht zur Verfügung haben,
		//wusste ich nicht so wirklich was man alles scoren könnte :/
		return antecedents.get(0);
		
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

