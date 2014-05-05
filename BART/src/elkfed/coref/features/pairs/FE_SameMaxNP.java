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

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 * this should actually never fire, but..
 * one of Ng & Cardie features
 */
public class FE_SameMaxNP implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_SAMEMAXNP=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"SameMaxNP");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SAMEMAXNP);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_SAMEMAXNP, getSameMaxNP(inst));
    }

    public static Boolean getSameMaxNP(PairInstance inst) {


        if (inst.getAnaphor().getMaxNPParseTree()== null) 
            return false;
        if (inst.getAnaphor().getMaxNPParseTree()==inst.getAntecedent().getMaxNPParseTree())
            return true;

        return false;


    }
    
}
