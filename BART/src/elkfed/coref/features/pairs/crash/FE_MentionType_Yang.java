/*
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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
package elkfed.coref.features.pairs.crash;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import java.util.List;

/**
 * mention type features from Yang et al 2003 that are not
 * found in FE_MentionType
 * @author versley
 */
public class FE_MentionType_Yang implements PairFeatureExtractor {
        //ante_M_ProperNP Cx is a mentioned proper NP
        //ante_ProperNP_APPOS Cx is a properNP modified by an appositive
        //ante_Appositive Cx is in an apposition structure
        //ante_NearestNP  Cx is the nearest candidate to the anaphor
        //ante_Embedded   Cx is an embedded NP
        //ante_Title      Cx is in title
    public void describeFeatures(List<FeatureDescription> fds) {
        //TODO!!!
    }

    public void extractFeatures(PairInstance inst) {
        //TODO!!!
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
