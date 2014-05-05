/*
 * Copyright 2007 Project ELERFED
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

import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
* {ana, ante} is a coordination
* needed for semeval-italian and similar annotation schemes
 */
public class FE_Coordination implements PairFeatureExtractor {
    
     public static final FeatureDescription<Boolean> FD_I_IS_COORDINATION=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AnteCoordination");
     public static final FeatureDescription<Boolean> FD_J_IS_COORDINATION=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AnaCoordination");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_IS_COORDINATION);
        fds.add(FD_J_IS_COORDINATION);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_I_IS_COORDINATION,inst.getAntecedent().isCoord());
        inst.setFeature(FD_J_IS_COORDINATION,inst.getAnaphor().isCoord());
    }
    
}
