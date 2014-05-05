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
 * Feature used to determine person agreement in an instance pair
 * @author massimo
 */
public class FE_FirstSecondPerson implements PairFeatureExtractor {
    
     public static final FeatureDescription<Boolean> FD_I_IS_FIRST_SECOND=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AnteFirstOrSecond");
     public static final FeatureDescription<Boolean> FD_J_IS_FIRST_SECOND=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "AnaFirstOrSecond");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_IS_FIRST_SECOND);
        fds.add(FD_J_IS_FIRST_SECOND);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_I_IS_FIRST_SECOND,inst.getAntecedent().getIsFirstSecondPerson());
        inst.setFeature(FD_J_IS_FIRST_SECOND,inst.getAnaphor().getIsFirstSecondPerson());
    }
    
}
