package elkfed.coref.features.pairs.de;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author samuel
 *
 * Feature to measure the number of nodes that have to be crossed on the path from
 * the anaphor to the antecedent, but only simpx, r-simpx and px nodes are
 * counted. The number of nodes is divided into the number of nodes that have to be
 * travelled up and travelled down in the tree.
 *
 */
public class FE_DistanceNodes implements PairFeatureExtractor {

    public enum NodeDistance {
        NONE,
        ONE,
        TWO,
        THREE_OR_MORE
    }

    public enum Direction {
        UP,
        DOWN
    }

    public static final FeatureDescription<String> FD_NODEDIST =
            new FeatureDescription<String>(FeatureType.FT_STRING, "NodeDistance");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_NODEDIST);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_NODEDIST, getNodeDist(inst, Direction.UP) + "+" + getNodeDist(inst, Direction.DOWN));
    }

    public String getNodeDist(PairInstance inst, Direction direction) {
        Tree AnteTree = inst.getAntecedent().getSentenceTree();
        Tree AnaTree = inst.getAnaphor().getSentenceTree();

        Tree AnteLowestProjection = inst.getAntecedent().getHighestProjection();
        Tree AnaLowestProjection = inst.getAnaphor().getHighestProjection();

        int up = 0;
        int down;

        if (!AnteTree.equals(AnaTree)) {
            up = getNodeDistance(AnaLowestProjection, AnaTree, AnaTree, Direction.UP);
            down = getNodeDistance(AnteLowestProjection, AnteTree, AnteTree, Direction.UP);
        } else {
            up = getNodeDistance(AnaLowestProjection, AnteLowestProjection, AnteTree, Direction.UP);
            down = getNodeDistance(AnaLowestProjection, AnteLowestProjection, AnteTree, Direction.DOWN);
        }

        switch(direction) {
            case UP:
            return Direction.UP + "-" + labelDistance(up);
                
            case DOWN:
            return Direction.DOWN + "-" + labelDistance(down);

            default:
                return null;
        }
    }

    private NodeDistance labelDistance(int nd) {
        switch(nd) {
            case 0:
                return NodeDistance.NONE;
            case 1:
                return NodeDistance.ONE;
            case 2:
                return NodeDistance.TWO;
            default:
                return NodeDistance.THREE_OR_MORE;
        }
    }

    private int getNodeDistance(Tree from, Tree to, Tree in, Direction dir) {
        int result = 0;
        
        List<Tree> mentionPath = in.pathNodeToNode(from, to);

        if(mentionPath!=null)
        for (Tree mentionPathItem : mentionPath) {
            if (    (
                    mentionPathItem.label().toString().toLowerCase().contains("simpx") ||
                    mentionPathItem.label().toString().toLowerCase().equals("px") 
                    )
                    &&
                    (
                    mentionPathItem.dominates(to) && ! mentionPathItem.dominates(from) && dir.equals(Direction.DOWN) ||
                    mentionPathItem.dominates(from) && ! mentionPathItem.dominates(to) && dir.equals(Direction.UP)
                    )
               )
            {
                result++;
            }
        }
        return result;
    }

}
