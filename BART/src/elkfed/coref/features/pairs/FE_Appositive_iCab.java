/*
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

import elkfed.config.ConfigProperties;
import java.util.List;
import elkfed.mmax.minidisc.Markable;


import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_SemClassAgree;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.ml.TriValued;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * second appositive feature
 * aimed at constructions like "[il ceceno] [Nur Pasha Kulayev]"
 * adjusted to cover annotations of the type [[il ceceno] Nur Pasha Kulaev], if the min is appropriate
 * adjusted to be more restrictive -- checks for number and semclass agreement (exception -- countries match ACE_COUNTRY)
 */
public class FE_Appositive_iCab implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_APPOSITIVE_ICAB =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AppositiveICab");

    public static boolean areNeighbouring(PairInstance inst) {
        Markable m1=inst.getAntecedent().getMarkable();
        Markable m2=inst.getAnaphor().getMarkable();
        int s1=m1.getLeftmostDiscoursePosition();
        int s2=m2.getLeftmostDiscoursePosition();
        int e1=m1.getRightmostDiscoursePosition();
        int e2=m2.getRightmostDiscoursePosition();

        MiniDiscourse doc = m1.getMarkableLevel().getDocument();


        if (m1.getAttributeValue("min_ids") != null) {
           String[] spans = MarkableHelper.parseRanges(m1.getAttributeValue("min_ids"));
           s1 = doc.DiscoursePositionFromDiscourseElementID(spans[0]);
           e1 = doc.DiscoursePositionFromDiscourseElementID(spans[spans.length - 1]);
        }
        if (m2.getAttributeValue("min_ids") != null) {
           String[] spans = MarkableHelper.parseRanges(m2.getAttributeValue("min_ids"));
           s2 = doc.DiscoursePositionFromDiscourseElementID(spans[0]);
           e2 = doc.DiscoursePositionFromDiscourseElementID(spans[spans.length - 1]);
        }
          

        if (e1==s2-1) return true;
        if (e2==s1-1) return true;
        return false;

    }

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_APPOSITIVE_ICAB);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_APPOSITIVE_ICAB, getAppositive(inst));
    }

    public static boolean getAppositive(PairInstance inst) {

        if (!sameSentence(inst)) return false;
        if (!inst.getAnaphor().isEnamex()) return false;
        if (!inst.getAntecedent().getDefinite()) return false;

// should be adjacent
        if (!areNeighbouring(inst)) return false;

// special treatment for ACE-style guidelines
        if (inst.getAnaphor().getEnamexType().toLowerCase().startsWith("gpe") && inst.getAntecedent().getHeadOrName().toLowerCase().matches(ACE_COUNTRY))
           return true;


// should agree in number

        if (!FE_Number.getNumber(inst)) return false;

// should agree in semclass (and it's still not enough, finer classification needed -- cf. "X city" where X is city vs. country)

        if (FE_SemClassAgree.compareSemClassHierarchy(inst)!=TriValued.TRUE) 
          return false;

        return true;
    }

    /** Checks wether the antecedent and anaphora are in the same sentence */
    public static boolean sameSentence(PairInstance inst) {
        final int sent1 = inst.getAnaphor().getSentId();
        final int sent2 = inst.getAntecedent().getSentId();
        return (sent1 == sent2);
    }

    public static int checkRole(PairInstance inst) {
        LanguagePlugin plugin=ConfigProperties.getInstance().getLanguagePlugin();
        String head1=inst.getAntecedent().getHeadOrName();
        String lookup=plugin.lookupAlias(head1, TableName.RoleMap);
        if (lookup==null) {
            return 0;
        } else if (lookup.equals("PER") &&
                SemanticClass.isaPerson(
                    inst.getAnaphor().mentionType().semanticClass)) {
            return 2;
        } else if (lookup.equals("ORG") &&
                inst.getAnaphor().mentionType().semanticClass==SemanticClass.ORGANIZATION) {
            return 3;
        } else {
            return 1;
        }
    }
}
