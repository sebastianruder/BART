package elkfed.coref.algorithms.sieve;

import java.util.List;
import elkfed.coref.mentions.Mention;

public class SieveFactory {
	
	Sieve createSieve(int walk_through, List<Mention> mentions) {
		Sieve sieve = null;
		
    	switch(walk_through) {
    	case 1:
    		sieve = new SpeakerIdentificationSieve(mentions);
    		break;
    	case 2: 
    		sieve = new StringMatchSieve(mentions);
    		break;
    	case 3:
    		sieve = new RelaxedStringMatchSieve(mentions);
    		break;
    	case 4:
    		sieve = new PreciseConstructSieve(mentions);
    		break;
    	case 5:
    		sieve = new StrictHeadMatchASieve(mentions);
    		break;
    	case 6:
    		sieve = new StrictHeadMatchBSieve(mentions);
    		break;
    	case 7:
    		sieve = new StrictHeadMatchCSieve(mentions);
    		break;
    	case 8:
    		sieve = new ProperHeadNounMatchSieve(mentions);
    		break;
    	case 9:
    		sieve = new RelaxedHeadMatchSieve(mentions);
    		break;
    	case 10: 
    		sieve = new PronounMatchSieve(mentions);
    		break;
    	}

		return sieve;
	}
}
