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
package elkfed.coref.features.pairs.cheat;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/** returns true if two names are coreferent AND they share a letter 4-gram
 *
 * @author versley
 */
public class FE_NameCheat3 implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_COREF =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "CoreferentName2");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_COREF);
    }

    public static boolean matchingSubstring(Mention m1, Mention m2)
    {
        String[] s1=m1.getHeadOrName().split(" ");
        String[] s2=m2.getHeadOrName().split(" ");
        for (int i=0; i< s1.length; i++)
        {
//            if (ignorable.matcher(s1[i]).matches())
//              continue;
            for (int j=0; j< s2.length; j++)
            {
                if (s1[i].equalsIgnoreCase(s2[j]))
                return true;
            }
        }
        return false;
    }
    public void extractFeatures(PairInstance inst) {
        boolean similar = inst.getAnaphor().getProperName()
                && matchingSubstring(inst.getAnaphor(),
                                     inst.getAntecedent());
        if (similar)
        {
            inst.setFeature(FD_IS_COREF, inst.getAnaphor().isCoreferent(inst.getAntecedent()));
        } else {
            inst.setFeature(FD_IS_COREF, false);
        }
    }
}
