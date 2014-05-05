/*
 * FE_Gender.java
 *
 * Created on July 11, 2007, 6:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import java.util.List;

/**
 * Feature determines wether Ante/Ana are inside quotations and therefore heuristically in speech
 * @author samuel
 */
public class FE_Speech implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_ANA_IS_IN_SPEECH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, Boolean.class, "AnaphorIsInSpeech");
    public static final FeatureDescription<Boolean> FD_ANTE_IS_IN_SPEECH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, Boolean.class, "AntecedentIsInSpeech");
    public static final FeatureDescription<Boolean> FD_IN_SPEECH_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, Boolean.class, "InSpeechMatch");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_ANA_IS_IN_SPEECH);
        fds.add(FD_ANTE_IS_IN_SPEECH);
        fds.add(FD_IN_SPEECH_MATCH);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_ANA_IS_IN_SPEECH, isMentionInSpeech(inst.getAnaphor()));
        inst.setFeature(FD_ANTE_IS_IN_SPEECH, isMentionInSpeech(inst.getAntecedent()));
        inst.setFeature(FD_IN_SPEECH_MATCH, isMentionInSpeech(inst.getAntecedent())==isMentionInSpeech(inst.getAnaphor()));
    }

    public boolean isMentionInSpeech(Mention mention) {

        int markableStart = mention.getMarkable().getLeftmostDiscoursePosition();

        MarkableLevel doc = mention.getDocument().getMarkableLevelByName("lemma");

        boolean isQuotationOpen = false;

        for (Markable markable : doc.getMarkables()) {
            if (markable.getLeftmostDiscoursePosition() > markableStart) {
                return isQuotationOpen;
            }
            if (markable.getAttributeValue("tag").equals("\"")) {
                if (isQuotationOpen) {
                    isQuotationOpen = false;
                } else {
                    isQuotationOpen = true;
                }
            }
        }
        return false;
    }
}
