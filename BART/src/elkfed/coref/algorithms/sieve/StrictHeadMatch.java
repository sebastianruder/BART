package elkfed.coref.algorithms.sieve;

import java.util.Set;

/**
 * @author julianbaumann
 */



import edu.stanford.nlp.trees.Tree;
/*
 * ToDo:  Proper WordInclusion with removed StopWords, modificator match,  i within i
 */
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.Mention;

public abstract class StrictHeadMatch extends Sieve {
	
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
		//wordInclusion
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
	
	
}
