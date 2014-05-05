/*
 * LinkScorer.java
 *
 * Created on August 7, 2007, 9:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.eval;

import elkfed.coref.mentions.Mention;
import java.util.List;

/**
 *
 * @author yannick
 */
public interface LinkScorer {
    void displayResults();
    void displayResultsShort();
    void displayResultsShort(boolean ignoring);

    void scoreLink(List<Mention> mentions, int antecedent, int anaphor);

    void scoreNonlink(List<Mention> mentions, int anaphor);
    
}
