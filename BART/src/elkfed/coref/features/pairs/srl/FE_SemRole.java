/*
 * SemRole.java
 *
 * Created on July 16, 2007, 4:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.srl;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import java.util.List;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import static elkfed.mmax.MarkableLevels.DEFAULT_SEMROLE_LEVEL;

/** Computes the semantic role features
 *
 * @author ponzetsp
 */
public class FE_SemRole implements PairFeatureExtractor {
    
    public static final FeatureDescription<String> FD_IS_ISem= 
        new FeatureDescription<String>(FeatureType.FT_STRING,   "I-Sem");
   
    public static final FeatureDescription<String> FD_IS_JSem= 
        new FeatureDescription<String>(FeatureType.FT_STRING,   "J-Sem");
    
    /** 
     * The regexp to indentify numbered argument only
     */
    protected static final String NUMBERED_ARGUMENT = "arg[0-9]+";

    /** 
     * The regexp to indentify a numbered argument at the beginning of
     * a string
     */
    protected static final String STARTING_NUMBERED_ARGUMENT = "^arg[0-9]+.*";
        
    /** Whether to include numbered arguments ONLY */
    protected boolean numberedArgsOnly;
    
    /** The buffer we use for storing temp predicate arg string */
    protected StringBuffer roleBuffer;
    
    /** Creates a new instance of SemRole */
    public FE_SemRole() {
         // default to keeping all semantic roles
        this.numberedArgsOnly = false;

        this.roleBuffer = new StringBuffer();
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_ISem);
        fds.add(FD_IS_JSem);
    }

    public void extractFeatures(PairInstance inst) {
        
        final String[] roles = getRolesAttributes(inst);
        
        inst.setFeature(FD_IS_ISem, roles[0]);
        inst.setFeature(FD_IS_JSem, roles[1]);
    }
    
    /** Gets the filtered semantic roles for antecedent and anaphora */
    protected String[] getRolesAttributes(PairInstance inst)
    {
        String[] toReturn = {
            filterRoles(
                inst.getAntecedent().getMarkable().getAttributeValue(DEFAULT_SEMROLE_LEVEL, "")),
            filterRoles(
                inst.getAnaphor().getMarkable().getAttributeValue(DEFAULT_SEMROLE_LEVEL, ""))
        };
        return toReturn;
    }
    
    /** Filters semantic roles based on whether or not to include
     *  the numbered arguments ONLY 
     */
    private String filterRoles(String roleAttribute)
    {
        // append NONE to empty role attributes
        if (roleAttribute.equals(""))
        { return "NONE"; }
        else if (!numberedArgsOnly)
        { return roleAttribute; }
        else
        {
            final String[] roles = roleAttribute.split("_");
            roleBuffer.setLength(0);
            for (String pair : roles)
            {
                if (!
                       (
                            numberedArgsOnly
                          &&
                            !pair.matches(STARTING_NUMBERED_ARGUMENT)
                       )
                )
                { roleBuffer.append(pair).append("_"); }
            }
            if (roleBuffer.toString().equals(""))
            { return "NONE"; }
            else            
            { return roleBuffer.deleteCharAt(roleBuffer.length()-1).toString(); }
        }
    }
        
    /** Sets whether to include numbered arguments ONLY */
    protected void setNumberedArgsOnly(boolean numberedArgsOnly)
    { this.numberedArgsOnly = numberedArgsOnly; }
}
