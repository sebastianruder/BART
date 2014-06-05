package elkfed.coref.algorithms.sieve;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.cscott.jutil.DisjointSet;
import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.eval.LinkScorer;
import elkfed.coref.eval.SplitLinkScorer;
import elkfed.coref.mentions.Mention;

/**
*
* @author sebastianruder
* 
*/

public class SieveDecoder implements CorefResolver {
	protected static final Logger _logger = Logger.getAnonymousLogger();
	protected LinkScorer _scorer = new SplitLinkScorer();
	
	
	
	public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
			Map<Mention, Mention> antecedents) {
		
		// creates data structure where disjoint sets of mentions are
		// going to be stored
		DisjointSet<Mention> mention_clusters = new DisjointSet<Mention>();
		Set<DiscourseEntity> entities;
		// counts number of links
		int numLinks = 0;		
        _logger.log(Level.INFO,
                String.format("%s: decode document with %d mentions\n",
                getClass().getSimpleName(),
                mentions.size()));        
        
        // counts number of walk_throughs
        
        for (int walk_through = 1; walk_through < 11; walk_through++) {
		    
        	
        	
        	// is this loop even necessary or do we rather just iterate 10 times
        	// and each sieve goes through the mentions / entities on its own?
		    for (int i = 0; i < mentions.size(); i++) {
		    	
		    	/* Instead of the switch statement, it would be better just being
		    	 * able to pass the number of the walk_through and get an instance
		    	 * of the appropriate sieve pack
		    	 * Maybe implement a SieveFactory
		    	 * with method createSieve(int walkthrough);
		    	 * 
		    	 */
		    	
		    	switch(walk_through) {
		    	/**case 1:
		    		Sieve sieve = new SpeakerIdentificatonSieve();
		    	// puts singletons in a single disjoint set
				// --> confirm with Yannick
		    	if (ConfigProperties.getInstance().getOutputSingletons()) 
		            clusters.union(mentions.get(i), mentions.get(i));
		            **/
		    	case 2: 
		    		Sieve sieve = new StringMatchSieve(mentions);
		    		int ante_idx = sieve.runSieve(mentions.get(i));
		    		System.out.println(ante_idx);
		    	
		    		/**
		    	case 3: RelaxedStringMatchSieve;
		    	case 4: PreciseConstructSieve;
		    	case 5: StrictHeadMatchASieve;
		    	case 6: StrictHeadMatchBSieve;
		    	case 7: StrictHeadMatchCSieve;
		    	case 8: ProperHeadNounMatchSieve;
		    	case 9: RelaxedHeadMatchSieve;
		    	case 10: PronounMatchSieve;
		    	**/
		    	
		    	
		    	// return of resolveSingle method is either -1 or index of coreferent
		    	//int ante_idx = -1; 
		    	
		        //int ante_idx=resolveSingle(mentions,i);
		        
		    	if (ante_idx==-1)
		        {
		           _scorer.scoreNonlink(mentions,i); 
		        }
		        else
		        {
		            numLinks++;
		            mention_clusters.union(mentions.get(i),mentions.get(ante_idx));
		            antecedents.put(mentions.get(i), mentions.get(ante_idx));
		            mentions.get(i).linkToAntecedent(mentions.get(ante_idx));
		            _scorer.scoreLink(mentions, ante_idx, i);
		            if (_logger.isLoggable(Level.FINE)) {
		                Object[] args={mentions.get(i),mentions.get(ante_idx)};
		                    _logger.log(Level.FINE,
		                                "joining %s and %s\n",
		                                args);
		            }
		        }
		    }
        }}
        _logger.log(Level.INFO,String.format("joined %d pairs in %d mentions",
                numLinks,mentions.size()));
	    //_scorer.displayResults();
	    return mention_clusters;
	}

    public void printStatistics() {
        _scorer.displayResultsShort();
    }
	
	

}
