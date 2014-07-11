package elkfed.coref.algorithms.sieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
//isCoreferent
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
		Evaluation eval = new Evaluation(mentions);
		Sieve sieve;
		// counts number of links
		int numLinks = 0;		
        _logger.log(Level.INFO,
                String.format("%s: decode document with %d mentions\n",
                getClass().getSimpleName(),
                mentions.size()));        
        // counts number of walk_throughs
        for (int walk_through = 1; walk_through < 11; walk_through++) {
        	
        	//condition to exclude/include specific sieve
//        	if (!(walk_through == 9)) {
//        		continue;
//        	}
        	
        	
        	
        	List<Mention> mentionsToResolve = mentions;
        	// List of first mentions is used instead of all mentions
        	/*
        	Set<DiscourseEntity> de_set = new HashSet<DiscourseEntity>();
        	for (int i = 0; i < mentions.size(); i++) {
        		DiscourseEntity de = mentions.get(i).getDiscourseEntity();
        		de_set.add(de);
        	}
        	// needs to be list because sieve requires a list of mentions
        	List<Mention> first_mention_list = new ArrayList<Mention>();
        	for (DiscourseEntity de : de_set) {
        		Mention first_mention = de.getFirstMention();  
        		first_mention_list.add(first_mention);
        	}     	
        	Collections.sort(first_mention_list);
        	
        	if (!(walk_through == 1 || walk_through == 4)) {
        		mentionsToResolve = first_mention_list;
        	} else {
        		mentionsToResolve = mentions;
        	} */
        	
        	
        	
        	sieve = _factory.createSieve(walk_through, mentions);
	    	String sieveName = sieve.getName();  

		    for (int i = 0; i < mentionsToResolve.size(); i++) {
		    	/* puts singletons in a single disjoint set
		    	   not relevant for MUC scorer, maybe for others
		    	  if (ConfigProperties.getInstance().getOutputSingletons()) {
		    	  		clusters.union(mentions.get(i), mentions.get(i)); } */
	    		int ante_idx = sieve.runSieve(mentions.get(i));
		    	if (ante_idx==-1) {
		           _scorer.scoreNonlink(mentions,i);
		        }
		    	else {
		    		
		            numLinks++;
		            mention_clusters.union(mentions.get(i),mentions.get(ante_idx));
		            antecedents.put(mentions.get(i), mentions.get(ante_idx));
		            
		            if (!(mentions.get(i).getDiscourseEntity() == mentions.get(ante_idx).getDiscourseEntity())) {
		            	//need better solution to stop merging of already merged entities
		            	mentions.get(i).linkToAntecedent(mentions.get(ante_idx));
		            	eval.setLink(mentions.get(i), mentions.get(ante_idx), sieveName);
		            }
		           //mentions.get(i).linkToAntecedent(mentions.get(ante_idx));
		            //Kontrollausgabe
		            DiscourseEntity d = mentions.get(i).getDiscourseEntity();
		            DiscourseEntity dAnte = mentions.get(ante_idx).getDiscourseEntity();
		            if (!(d == dAnte)) {
		            	System.err.println("error: not merged");
		            }
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
        
        eval.printEvaluation();
	    //_scorer.displayResults();
	    return mention_clusters;
	}

    public void printStatistics() {
        _scorer.displayResultsShort();
    }
    
}
