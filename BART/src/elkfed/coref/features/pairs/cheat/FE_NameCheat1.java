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
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/** returns true if two names are coreferent
 *
 * @author versley
 */
public class FE_NameCheat1 implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_COREF=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "CoreferentName1");
     
     public void describeFeatures(List<FeatureDescription> fds) {
         fds.add(FD_IS_COREF);     
     }

     
      public void extractFeatures(PairInstance inst) {
          if (inst.getAnaphor().getProperName() &&
                  inst.getAntecedent().getProperName()) {
            inst.setFeature(FD_IS_COREF,inst.getAnaphor().isCoreferent(inst.getAntecedent()));
          } else {
              inst.setFeature(FD_IS_COREF, false);
          }
      }
}
