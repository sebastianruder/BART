/*
 * FE_Syntax_Binding.java
 *
 */


package elkfed.coref.features.pairs.de;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;


/**
 * @author samuel
 *
 * Feature to determine if the anaphor is bound by the antecedent. Either true or false.
 */
public class FE_Syntax_Binding implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_ANA_FREE_IN_BINDINGDOM=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AnaphorIsBound");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_ANA_FREE_IN_BINDINGDOM);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_ANA_FREE_IN_BINDINGDOM,getAnaBoundInBindingDomain(inst));
    }

    /**
     * Computes if the antecedent c-commands the anaphor inside a common simpx node
     *
     * @param inst
     * @return true if antecedent c-commands anaphor, false otherwise
     */
    private boolean getAnaBoundInBindingDomain(PairInstance inst)
    {
        Tree AnteTree = inst.getAntecedent().getSentenceTree();
        Tree AnaTree = inst.getAnaphor().getSentenceTree();

        Tree AnteTreeRoot = inst.getAntecedent().getLowestProjection();
        Tree AnaTreeRoot = inst.getAnaphor().getLowestProjection();

        if (!AnteTree.equals(AnaTree)) {
            return false;
        }

        List<Tree> anaPath = AnaTree.pathNodeToNode(AnaTreeRoot, AnaTree);

        for(Tree anaAncestor: anaPath) {

            if(anaAncestor.label().toString().equalsIgnoreCase("simpx")) {
                if (cCommands(anaAncestor, AnteTreeRoot, AnaTreeRoot)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean cCommands(Tree t, Tree t1, Tree t2) {
        List<Tree> sibs = t1.siblings(t);
        if (sibs == null || sibs.size() == 0) {
            if(t1.parent(t)!=null) {
                return cCommands(t, t1.parent(t), t2);
            } else {
                return false;
            }
        }
        for (Tree sib : sibs) {
            if (sib == t2 || sib.contains(t2)) {
                return true;
            }
        }
        return false;
    }
}