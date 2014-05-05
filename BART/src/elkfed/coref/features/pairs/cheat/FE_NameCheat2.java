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

import java.util.List;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

/** returns true if two names are coreferent AND they share a letter 4-gram
 *
 * @author versley
 */
public class FE_NameCheat2 implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_COREF =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "CoreferentName2");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_COREF);
    }

    public void extractFeatures(PairInstance inst) {
        boolean similar = false;
        if (inst.getAnaphor().getProperName() &&
                inst.getAntecedent().getProperName()) {
            String ana_str = inst.getAnaphor().getMarkableString().toLowerCase();
            String ante_str = inst.getAntecedent().getMarkableString().toLowerCase();
            for (int i = 0; i < ana_str.length() - 4; i++) {
                if (ante_str.indexOf(ana_str.substring(i, i + 4)) != -1) {
                    similar = true;
                    break;
                }
            }
        }
        if (similar)
        {
            inst.setFeature(FD_IS_COREF, inst.getAnaphor().isCoreferent(inst.getAntecedent()));
        } else {
            inst.setFeature(FD_IS_COREF, false);
        }
    }
}
