/*
 * PairInstance.java
 *
 * Created on July 10, 2007, 4:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref;

import elkfed.coref.mentions.Mention;
import elkfed.ml.AbstractInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

/** Stores information about pairs of mentions.
 *
 * @author versley
 */
public class PairInstance extends AbstractInstance {
    
    public static final FeatureDescription<Boolean> FD_POSITIVE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "isPositive");
    boolean _label;
    Mention _anaphor;
    Mention _antecedent;
    /** Creates a new instance of PairInstance */
    public PairInstance(Mention anaphor, Mention antecedent)
    {
        _anaphor=anaphor;
        _antecedent=antecedent;  
    }
    
    public Mention getAnaphor() { return _anaphor; }
    public Mention getAntecedent() { return _antecedent; }

    @Override
    public String getDebugInfo()
    {
        return String.format("%s(%s) -> %s(%s)",
                _anaphor.getMarkableString(),
                _anaphor.getMarkable().getID(),
                _antecedent.getMarkableString(),
                _antecedent.getMarkable().getID());
    }

    public String toString() {
        return getAntecedent() + " <---> " + getAnaphor();
    }

}
