package elkfed.coref.algorithms.sieve;

import java.util.List;
import java.util.Map;

import net.cscott.jutil.DisjointSet;
import elkfed.coref.CorefResolver;
import elkfed.coref.eval.LinkScorer;
import elkfed.coref.eval.SplitLinkScorer;
import elkfed.coref.mentions.Mention;

/**
*
* @author sebastianruder
* 
*/

public class SieveDecoder implements CorefResolver {
	
	protected LinkScorer _scorer = new SplitLinkScorer();

	@Override
	public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
			Map<Mention, Mention> antecedents) {
		// TODO Auto-generated method stub
		return null;
	}

    public void printStatistics() {
        _scorer.displayResultsShort();
    }
	
	

}
