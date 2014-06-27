package elkfed.coref.algorithms.sieve;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.cscott.jutil.DisjointSet;
import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.eval.LinkScorer;
import elkfed.coref.eval.SplitLinkScorer;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.mentions.Mention;

/**
*
* @author sebastianruder
* 
*/

public class SieveDecoder implements CorefResolver {
	protected static final Logger _logger = Logger.getAnonymousLogger();
	protected LinkScorer _scorer = new SplitLinkScorer();
	protected SieveFactory _factory = new SieveFactory();
	
	public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
			Map<Mention, Mention> antecedents) {
		
		// creates data structure where disjoint sets of mentions are
		// going to be stored
		DisjointSet<Mention> mention_clusters = new DisjointSet<Mention>();
		Set<DiscourseEntity> entities;
		Sieve sieve;
		// counts number of links
		int numLinks = 0;		
        _logger.log(Level.INFO,
                String.format("%s: decode document with %d mentions\n",
                getClass().getSimpleName(),
                mentions.size()));        
        
        // counts number of walk_throughs
        for (int walk_through = 0; walk_through < 11; walk_through++) {
        	
        	// skips other sieves; should be changed when new sieves are implemented
        	if (walk_through != 5 && walk_through != 2) {
        		continue;
        	}
        	
        	// iterates over mentions
		    for (int i = 0; i < mentions.size(); i++) {
		    	
		    	/* puts singletons in a single disjoint set
		    	   --> confirm with Yannick
		    	  if (ConfigProperties.getInstance().getOutputSingletons()) {
		    	  		clusters.union(mentions.get(i), mentions.get(i)); } */
		    	
		    	sieve = _factory.createSieve(walk_through, mentions);		    	
	    		int ante_idx = sieve.runSieve(mentions.get(i));
	    		System.out.println("Highest Projection: " + mentions.get(i)._highestProjection);
	    		System.out.println("Premods: " + mentions.get(i)._premodifiers);
    			System.out.println("Postmods: " + mentions.get(i)._postmodifiers);
	    		
	    		if (ante_idx == -1) {
	    			System.out.println(String.format("#%d: No match found: %s", i, mentions.get(i).toString()));
	    		}
	    		else {
	    			System.out.println(String.format("#%d: Antecedent of '%s': '%s' with sieve nr %d",
		    				i, mentions.get(i).toString(), mentions.get(ante_idx).toString(), walk_through));
	    			/*
	    			PairInstance instance = new PairInstance(mentions.get(i), mentions.get(ante_idx));
	    			if (instance.getFeature(PairInstance.FD_POSITIVE) == true) {
                        System.out.println("True positive!");
		            }
		            */
	    		}
	    		
		    	if (ante_idx==-1)
		        {
		           _scorer.scoreNonlink(mentions,i); 
		        }
		        else
		        {
		            numLinks++;
		            mention_clusters.union(mentions.get(i),mentions.get(ante_idx));
		            antecedents.put(mentions.get(i), mentions.get(ante_idx));
		            
		            if (!(mentions.get(i).getDiscourseEntity() == mentions.get(ante_idx).getDiscourseEntity())){
		            	//need better solution to stop merging of already merged entities
		            	mentions.get(i).linkToAntecedent(mentions.get(ante_idx));
		            }
		           //mentions.get(i).linkToAntecedent(mentions.get(ante_idx));
		            //Kontrollausgabe
		            DiscourseEntity d = mentions.get(i).getDiscourseEntity();
		            DiscourseEntity dAnte = mentions.get(ante_idx).getDiscourseEntity();
		            if (!(d == dAnte)) {
		            	System.err.println("error: not merged");
		            }
		            System.out.println(String.format("Discourse ID: %d\nHeads: %s\nWords: %s", 
		            								 d.getID(), d.getHeadsString(), d.getWordsString()));
		            
		            
		            _scorer.scoreLink(mentions, ante_idx, i);
		            if (_logger.isLoggable(Level.FINE)) {
		                Object[] args={mentions.get(i),mentions.get(ante_idx)};
		                    _logger.log(Level.FINE,
		                                "joining %s and %s\n",
		                                args);
		            }
		        }
		    }
        }
        _logger.log(Level.INFO,String.format("joined %d pairs in %d mentions",
                numLinks,mentions.size()));
	    //_scorer.displayResults();
	    return mention_clusters;
	}

    public void printStatistics() {
        _scorer.displayResultsShort();
    }
    
}
