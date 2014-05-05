/*
 *   Copyright 2007 Project ELERFED
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
package elkfed.coref.features.pairs;

import elkfed.coref.mentions.Mention;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;

import elkfed.config.ConfigProperties;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.NodeCategory;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

/**
 * Feature used to determine whether markble is a possible
 * appositive construct of another markable
 *
 * @author vae2101
 */
public class FE_Appositive implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_APPOSITIVE =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "Appositive");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_APPOSITIVE);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_APPOSITIVE, getAppositive(inst));
    }

    public static boolean getAppositive(PairInstance inst) {
        // for a pair to be an apposition
        if ( // *trick to speed up* right at the beginning: they must be
                // in the same sentence...
                sameSentence(inst) &&
                // the anaphora must NOT contain a verb (we check PoS)
                !containsVerb(inst.getAnaphor()) &&
                // antecedent and anaphora must be only comma separated
                areCommaSeparated(inst) &&
                // one of the two must be a proper name
                atLeastOneProperName(inst) &&
                // anaphora must be a noun phrase in any case
                isNounPhrase(inst.getAnaphor())) {
            // in MUC-6 the anaphor must be a definite for the
            // expression to be an apposition
            if (ConfigProperties.getInstance().getTrainingDataId().
                    equals(ConfigProperties.MUC6_ID) ||
                    ConfigProperties.getInstance().getTestDataId().
                    equals(ConfigProperties.MUC6_ID)) {
                return inst.getAnaphor().getDefinite() ||
                        startsWithNoun(inst.getAnaphor().getMarkable());
            } else {
                // not MUC-6
                return inst.getAnaphor().getDefinite() ||
                        inst.getAnaphor().getIndefinite();
            }
        } else {
            return false;
        }
    }

    /** Checks wether the antecedent and anaphora are in the same sentence */
    public static boolean sameSentence(PairInstance inst) {
        final int sent1 = inst.getAnaphor().getSentId();
        final int sent2 = inst.getAntecedent().getSentId();
        return (sent1 == sent2);
    }

    /** Checks whether the markable contains a verb */
    private static boolean containsVerb(final Mention mention) {
        Markable markable = mention.getMarkable();
        String pos_strs = markable.getAttributeValue(DEFAULT_POS_LEVEL);
        if (pos_strs == null) return false;
        for (String pos : pos_strs.split(" ")) {
            if (pos.startsWith("v")) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether the instance has at least one proper-name */
    private static boolean atLeastOneProperName(PairInstance inst) {
        return (inst.getAntecedent().getProperName() // IsProperNameFE.get().isProperName(instance.getAntecedent())
                ||
                inst.getAnaphor().getProperName() // IsProperNameFE.get().isProperName(instance.getAnaphora())
                );
    }

    /** Checks whether the antecedent and anaphor are only separated by a comma */
    protected static boolean areCommaSeparated(PairInstance inst) {
        final MiniDiscourse doc = inst.getAnaphor().getDocument();
        int rtoken = inst.getAntecedent().getMarkable().getRightmostDiscoursePosition() + 1;
        if (inst.getAnaphor().getMarkable().getLeftmostDiscoursePosition() - 1 != rtoken) {
            return false;
        }
        return doc.getDiscourseElementAtDiscoursePosition(rtoken).equals(",");
    }

    /** Checks whether the markable is a noun phrase */
    private static boolean isNounPhrase(Mention mention) {
        return !mention.isEnamex();
    }

    /** Checks whether the markable starts with a noun */
    private static boolean startsWithNoun(Markable markable) {
        LanguagePlugin plugin=ConfigProperties.getInstance().getLanguagePlugin();
        String firstPos = markable.getAttributeValue(DEFAULT_POS_LEVEL);
        int idx=firstPos.indexOf(' ');
        if (idx!=-1) {
            firstPos = firstPos.substring(0, idx);
        }
        NodeCategory firstCat=plugin.labelCat(firstPos);
        return (firstCat==NodeCategory.CN ||
                firstCat==NodeCategory.PN ||
                firstCat==NodeCategory.ADJ);
    }
}
