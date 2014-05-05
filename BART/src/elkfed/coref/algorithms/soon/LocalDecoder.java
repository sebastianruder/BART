/*
 * LocalDecoder.java
 *
 * Created on August 13, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.soon;

import elkfed.coref.CorefResolver;
import elkfed.coref.eval.LinkScorer;
import elkfed.coref.eval.SplitLinkScorer;
import elkfed.coref.mentions.Mention;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.cscott.jutil.DisjointSet;
import elkfed.config.ConfigProperties;

/**
 *
 * @author yannick
 * Olga: adjusted to support singleton entities (optional)
 */
public abstract class LocalDecoder implements CorefResolver {
    protected static final Logger _logger = Logger.getAnonymousLogger();
    protected LinkScorer _scorer=new SplitLinkScorer();
     
    abstract public int resolveSingle(List<Mention> mentions, int ana);
    
    protected void setupDocument(List<Mention> mentions) {}
    
    public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
            Map<Mention,Mention> antecedents) {
        DisjointSet<Mention> clusters=new DisjointSet<Mention>();
        int numLinks=0;
        _logger.log(Level.INFO,
                String.format("%s: decode document with %d mentions\n",
                getClass().getSimpleName(),
                mentions.size()));
        setupDocument(mentions);
        for (int i=0; i<mentions.size(); i++) {
            if (ConfigProperties.getInstance().getOutputSingletons()) 
              clusters.union(mentions.get(i),mentions.get(i));
            int ante_idx=resolveSingle(mentions,i);
            if (ante_idx==-1)
            {
               _scorer.scoreNonlink(mentions,i); 
            }
            else
            {
                numLinks++;
                clusters.union(mentions.get(i),mentions.get(ante_idx));
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
        _logger.log(Level.INFO,String.format("joined %d pairs in %d mentions",
                numLinks,mentions.size()));
        //_scorer.displayResults();
        return clusters;
    }
    
    public void printStatistics() {
        _scorer.displayResultsShort();
    }
}
