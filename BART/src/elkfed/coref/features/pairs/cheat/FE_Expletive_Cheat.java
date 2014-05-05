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

/**
 *
 * @author versley
 */
public class FE_Expletive_Cheat implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_EXPLETIVE_CHEAT=
            new FeatureDescription<Boolean>(
            FeatureType.FT_BOOL,"EXPL_CHEAT");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_EXPLETIVE_CHEAT);
    }

    public static boolean is_nonref_it(Mention m) {
        if (!m.getMarkableString().equalsIgnoreCase("it")) {
            return false;
        }
        return m.getSetID()==null;
    }
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_EXPLETIVE_CHEAT,
                is_nonref_it(inst.getAntecedent()) ||
                is_nonref_it(inst.getAnaphor()));
    }

}
