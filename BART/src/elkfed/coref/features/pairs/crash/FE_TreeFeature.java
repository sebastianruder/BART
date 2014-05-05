/*
 * FE_TreeFeature.java
 *
 * Created on July 25, 2007, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.crash;

import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.knowledge.SyntaxTreeFeature;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/** Xiaofeng's tree-based features
 *
 * @author yannick
 */
public class FE_TreeFeature implements PairFeatureExtractor {
    public static final FeatureDescription<String> FD_TREE_XY_CONN=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_XY_Connecting");
    public static final FeatureDescription<String> FD_TREE_XY_ANTE=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_XY_Ante");
    public static final FeatureDescription<String> FD_TREE_XY_ANA=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_XY_Ana");
    public static final FeatureDescription<String> FD_TREE_XY_ANTE_FRROOT=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_XY_Ante_FromRoot");
    public static final FeatureDescription<String> FD_TREE_XY_ANA_FRROOT=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_XY_Ana_FromRoot");
    public static final FeatureDescription<String> FD_TREE_BW_ANTE=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnTE");
    public static final FeatureDescription<String> FD_TREE_BW_ANA=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnA");
    public static final FeatureDescription<String> FD_TREE_BW_ANTE_POS=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnTE_Pos");
    public static final FeatureDescription<String> FD_TREE_BW_ANA_POS=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnA_Pos");
    public static final FeatureDescription<String> FD_TREE_BW_ANTE_WORD=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnTE_Word");
    public static final FeatureDescription<String> FD_TREE_BW_ANA_WORD=
            new FeatureDescription<String>(FeatureType.FT_TREE_STRING, String.class,
            "Tree_BW_AnA_Word");

    static SyntaxTreeFeature _stfeature=new SyntaxTreeFeature();
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_TREE_XY_CONN);
        fds.add(FD_TREE_XY_ANTE);
        fds.add(FD_TREE_XY_ANA);
        fds.add(FD_TREE_XY_ANTE_FRROOT);
        fds.add(FD_TREE_XY_ANA_FRROOT);
        fds.add(FD_TREE_BW_ANTE);
        fds.add(FD_TREE_BW_ANA);
        fds.add(FD_TREE_BW_ANTE_POS);
        fds.add(FD_TREE_BW_ANA_POS);
        fds.add(FD_TREE_BW_ANTE_WORD);
        fds.add(FD_TREE_BW_ANA_WORD);
        

    }
    
    public void extractFeatures(PairInstance inst) {
        Mention m1=inst.getAnaphor();
        Mention m2=inst.getAntecedent();
        if (m1.getSentenceTree()==null ||
                m2.getSentenceTree()==null)
        {
            return;
        }
        else
        {
            String[] vals=_stfeature.GetSyntaxFeatures(inst.getAntecedent(),
                                                          inst.getAnaphor());
            inst.setFeature(FD_TREE_XY_CONN, vals[0]);
            inst.setFeature(FD_TREE_XY_ANTE, vals[1]);
            inst.setFeature(FD_TREE_XY_ANA, vals[2]);
            inst.setFeature(FD_TREE_XY_ANTE_FRROOT, vals[3]);
            inst.setFeature(FD_TREE_XY_ANA_FRROOT, vals[4]);
            inst.setFeature(FD_TREE_BW_ANTE, vals[5]);
            inst.setFeature(FD_TREE_BW_ANA, vals[6]);
            inst.setFeature(FD_TREE_BW_ANTE_POS, vals[7]);
            inst.setFeature(FD_TREE_BW_ANA_POS, vals[8]);
            inst.setFeature(FD_TREE_BW_ANTE_WORD, vals[9]);
            inst.setFeature(FD_TREE_BW_ANA_WORD, vals[10]);

        }
    }
}
