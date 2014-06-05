package elkfed.coref.algorithms.sieve;

import java.util.List;

import elkfed.coref.mentions.Mention;

public class SieveFactory {
	
	Sieve createSieve(int walk_through, List<Mention> mentions) {
		Sieve sieve;
		
    	switch(walk_through) {
    	case 1:
    		sieve = new SpeakerIdentificatonSieve(mentions);
    	case 2: 
    		sieve = new StringMatchSieve(mentions);
    	case 3:
    		sieve = new RelaxedStringMatchSieve(mentions);
    	case 4:
    		sieve = new PreciseConstructSieve(mentions);
    	case 5:
    		sieve = new StrictHeadMatchASieve(mentions);
    	case 6:
    		sieve = new StrictHeadMatchBSieve(mentions);
    	case 7:
    		sieve = new StrictHeadMatchCSieve(mentions);
    	case 8:
    		sieve = new ProperHeadNounMatchSieve(mentions);
    	case 9:
    		sieve = new RelaxedHeadMatchSieve(mentions);
    	case 10: 
    		sieve = new PronounMatchSieve(mentions);
    	}
		return sieve;
	}

}
