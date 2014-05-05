/*
 * FE_HeadPOS.java
 *
 * Created on August 20, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs.crash;

import static elkfed.mmax.MarkableLevels.DEFAULT_DEPREL_LEVEL;
import java.util.List;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.lang.AbstractLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;

/**
 *
 * @author samuel
 */
public class FE_HeadGrammaticalFunctions implements PairFeatureExtractor {

    public static final FeatureDescription<String> FD_HEADGF_ANTE=
            new FeatureDescription<String>(FeatureType.FT_STRING,
                String.class, "HeadGF_Antecedent");

    public static final FeatureDescription<String> FD_HEADGF_ANA=
            new FeatureDescription<String>(FeatureType.FT_STRING,
                String.class, "HeadGF_Anaphor");

    public static final FeatureDescription<Boolean> FD_HEADGF_MATCH=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"HeadGF_Match");

    public static final FeatureDescription<String> FD_HEADGF_PAIR=
            new FeatureDescription<String>(FeatureType.FT_STRING,
                String.class, "HeadGF_Antecedent_Anaphor");
    
    /** Creates a new instance of FE_SemClass */
    public FE_HeadGrammaticalFunctions() {
    }
    
    @Override
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_HEADGF_ANTE);
        fds.add(FD_HEADGF_ANA);
        fds.add(FD_HEADGF_MATCH);
        fds.add(FD_HEADGF_PAIR);
    }

    @Override
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_HEADGF_ANTE, getHeadGF(inst.getAntecedent()));
        inst.setFeature(FD_HEADGF_ANA,getHeadGF(inst.getAnaphor()));
        inst.setFeature(FD_HEADGF_PAIR, getHeadGF(inst.getAntecedent()) + "-" + getHeadGF(inst.getAnaphor()));
        inst.setFeature(FD_HEADGF_MATCH,getHeadGF(inst.getAnaphor()).equals(getHeadGF(inst.getAntecedent())));
    }

    private String getHeadGF(Mention mention) {

        Markable markable = mention.getMarkable();

        String markableGF = "*NULL*";

        MiniDiscourse doc = markable.getMarkableLevel().getDocument();
        MarkableLevel deprel = doc.getMarkableLevelByName(DEFAULT_DEPREL_LEVEL);

        String head_pos = markable.getAttributeValue(AbstractLanguagePlugin.HEAD_POS);

        int head_disc_pos = doc.getDiscoursePositionFromDiscourseElementID(head_pos);

        try {
            markableGF = deprel.getMarkablesAtDiscoursePosition(head_disc_pos).get(0).getAttributeValue("tag");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return markableGF;
    }
}
