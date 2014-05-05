/*
 * FE_HeadPOS.java
 *
 * Created on August 20, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author samuel
 */
public class FE_HeadPartOfSpeech implements PairFeatureExtractor {

    public static final FeatureDescription<String> FD_HEADPOS_ANTE = new FeatureDescription<String>(FeatureType.FT_STRING, "HeadPOS_Antecedent");
    public static final FeatureDescription<String> FD_HEADPOS_ANA = new FeatureDescription<String>(FeatureType.FT_STRING, "HeadPOS_Anaphor");
    public static final FeatureDescription<String> FD_HEADPOS_PAIR = new FeatureDescription<String>(FeatureType.FT_STRING, "HeadPOS_Antecedent_Anaphor");

    @Override
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_HEADPOS_ANTE);
        fds.add(FD_HEADPOS_ANA);
        fds.add(FD_HEADPOS_PAIR);
    }

    @Override
    public void extractFeatures(PairInstance inst) {

        inst.setFeature(FD_HEADPOS_ANTE, inst.getAntecedent().getHeadPOS());
        inst.setFeature(FD_HEADPOS_ANA, inst.getAnaphor().getHeadPOS());
        inst.setFeature(FD_HEADPOS_PAIR, getHeadPOSPair(inst));
    }

    private String getHeadPOSPair(PairInstance inst) {

        Mention anaphor = inst.getAnaphor();
        Mention antecedent = inst.getAntecedent();

        return String.format("%s-%s", antecedent.getHeadPOS(), anaphor.getHeadPOS());
    }
}
