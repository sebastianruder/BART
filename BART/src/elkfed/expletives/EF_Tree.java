/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elkfed.expletives;

import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author versley
 */
public class EF_Tree implements FeatureExtractor<ExpletiveInstance> {

    public static final FeatureDescription<Tree> FD_EXPL_TREE =
            new FeatureDescription<Tree>(FeatureType.FT_TREE_TREE,
            "expl_tree_frag");
    public static final FeatureDescription<Tree> FD_EXPL_TREE2 =
            new FeatureDescription<Tree>(FeatureType.FT_TREE_TREE,
            "expl_tree_full");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_EXPL_TREE);
        fds.add(FD_EXPL_TREE2);
    }

    public static Tree tagged_word(String word, String tag) {
        LabelFactory lf = new StringLabelFactory();
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(tag));
        Tree[] dtrs = new Tree[1];
        dtrs[0] = new LabeledScoredTreeNode(lf.newLabel(word));
        result.setChildren(dtrs);
        return result;

    }

    public static Tree tree_pred(Tree node) {
        LabelFactory lf = new StringLabelFactory();
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(node.value() + "-PRD"));
        if (node.value().equals("PP") &&
                node.children().length == 2 &&
                node.children()[0].value().equals("IN")) {
            Tree[] dtrs = new Tree[2];
            dtrs[0] = node.children()[0];
            dtrs[1] = node.children()[1].headPreTerminal(new ModCollinsHeadFinder());
            result.setChildren(dtrs);
        }
        Tree[] dtrs = new Tree[1];
        dtrs[0] = node.headPreTerminal(new ModCollinsHeadFinder());
        result.setChildren(dtrs);
        return result;
    }

    /** constructs a marked subtree for a subclause
     * outside the path to the pronoun
     * @param node the starting point
     * @return a marked subtree for the tree starting with node
     */
    public static Tree tree_pruned(Tree node) {
        LabelFactory lf = new StringLabelFactory();
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(node.value() + "-X"));
        List<Tree> dtrs = new ArrayList<Tree>();
        boolean cpl_seen = false;
        if (node.value().matches("S|SBAR|VP")) {
            for (Tree t : node.children()) {
                // modals are copied verbatim
                String cat = t.value();
                if (cat.matches("TO|MD|IN")) {
                    dtrs.add(t);
                    cpl_seen = true;
                } else if (cat.startsWith("WH")) {
                    Tree dtr = tagged_word(cat, "WH");
                    cpl_seen = true;
                } else if (t.value().startsWith("VB")) {
                    break;
                } else if (t.value().matches("S|SBAR|VP")) {
                    if (cpl_seen) {
                    //ignore
                    } else {
                        dtrs.add(tree_pruned(t));
                    }
                }
            }
        }
        result.setChildren(dtrs);
        return result;
    }

    private static void dtrs_inside(Tree node, List<Tree> dtrs) {
        boolean after_be = false;
        for (Tree t : node.children()) {
            String cat = t.value();
            if (cat.matches("''|``")) {
            // ignore
            } else if ((cat.startsWith("VB") || cat.equals("AUX")) &&
                    t.children()[0].value().matches("'s|is|was|be|being|been")) {
                //dtrs.add(t);
                dtrs.add(tagged_word(cat, "BE"));
                after_be = true;
            } else if (after_be && cat.matches("NP|ADJP|VP|PP")) {
                if (cat.equals("VP") &&
                        t.children()[0].value().matches("VB[GN]")) {
                    dtrs_inside(t,dtrs);
                    //dtrs.add(tree_outside(t));
                } else {
                    dtrs.add(tree_pred(t));
                }
                after_be = false;
            } else {
                if (cat.matches("S|SBAR")) {
                    dtrs.add(tree_pruned(t));
                } else  if (cat.equals("VP")) {
                    dtrs_inside(t,dtrs);
                    //dtrs.add(tree_outside(t));
                } else {
                    dtrs.add(tree_outside(t));
                }
            }
        }
    }

    /** constructs a marked subtree for parts which are
     * outside the path to the pronoun
     * @param node the starting point
     * @return a marked subtree for the tree starting with node
     */
    public static Tree tree_outside(Tree node) {
        LabelFactory lf = new StringLabelFactory();
        // verbs and modals are copied verbatim
        if (node.value().matches("VB[DZPNG]?")) {
            return tagged_word(Morphology.stemStatic(node.children()[0].value(),
                    node.value()).value(), "VBX");
            //return node;
        } else if (node.value().matches("TO|MD|IN|RB")) {
            return node;
        }
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(node.value()));
        if (node.value().matches("VP")) {
            List<Tree> dtrs = new ArrayList<Tree>();
            dtrs_inside(node, dtrs);
            result.setChildren(dtrs);
        } else {
            List<Tree> dtrs = null;
            result.setChildren(dtrs);
        }
        return result;
    }

    /**
     * constructs a marked subtree for the part where the
     * pronoun is <i>inside</i> the subtree
     * @param node the starting point
     * @param pron our pronoun
     * @return a marked subtree for the tree starting with node
     */
    public static Tree tree_inside(Tree node, Tree pron) {
        LabelFactory lf = new StringLabelFactory();
        int pron_left = pron.leftCharEdge(node);
        int pron_right = pron.rightCharEdge(node);
        List<Tree> dtrs = new ArrayList<Tree>(node.children().length);
        boolean node_seen = false;
        for (Tree t : node.children()) {
            if (t == pron) {
                dtrs.add(t);
                node_seen = true;
            } else if (t.dominates(pron)) {
                dtrs.add(tree_inside(t, pron));
                node_seen = true;
            } else {
                String cat = t.value();
                if (cat.matches("S|SBAR")) {
                    dtrs.add(tree_pruned(t));
                } else {
                    dtrs.add(tree_outside(t));
                }
            }
        }
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(node.value() + "-I"));
        result.setChildren(dtrs);
        return result;
    }
    
    public static Tree tree_markonly(Tree node, Tree pron) {
        LabelFactory lf = new StringLabelFactory();
        List<Tree> dtrs = new ArrayList<Tree>(node.children().length);
        for (Tree t: node.children()) {
            if (t==pron) {
                dtrs.add(t);
            } else if (t.dominates(pron)) {
                dtrs.add(tree_markonly(t,pron));
            } else {
                dtrs.add(t);
            }
        }
        Tree result = new LabeledScoredTreeNode();
        result.setLabel(lf.newLabel(node.value() + "-I"));
        result.setChildren(dtrs);
        return result;        
    }

    public void extractFeatures(ExpletiveInstance inst) {
        Tree t_root = inst.getRoot();
        Tree t_pron = inst.getPronoun();
        int idx;

        LabeledScoredTreeNode t2 = (LabeledScoredTreeNode) t_pron.parent(t_root);
        while (t2 != t_root && !t2.label().value().matches("(S|RRC|FRAG|VP)")) {
            t2 = (LabeledScoredTreeNode) t2.parent(t_root);
        }
        inst.setFeature(FD_EXPL_TREE, tree_inside(t2, t_pron));
        //inst.setFeature(FD_EXPL_TREE2, tree_markonly(t2,t_pron));
        inst.setFeature(FD_EXPL_TREE2, t2);
    }
}
