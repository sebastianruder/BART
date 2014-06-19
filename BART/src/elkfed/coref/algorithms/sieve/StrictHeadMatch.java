package elkfed.coref.algorithms.sieve;

import java.util.Set;

/**
 * @author julianbaumann
 */


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
		DiscourseEntity d = m.getDiscourseEntity();
		DiscourseEntity dAnte = ante.getDiscourseEntity();
		Set<String> anteWords = dAnte.getWords();
		for ( String word : d.getWords()) {
			if (!(anteWords.contains(word))) {
				return false;
			}
		}
		
		return true;
		
	}
	
	
}
