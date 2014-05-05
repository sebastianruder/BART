/*
 * Copyright 2009 Yannick Versley / Univ. Tuebingen
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
import elkfed.mmax.minidisc.Markable;
import static elkfed.mmax.pipeline.MarkableCreator.SENTENCE_ID_ATTRIBUTE;

/**
 * sentence distance with logarithmic transform
 */
public class FE_DistLogarithmic implements PairFeatureExtractor{
    
    public static final FeatureDescription<Double> FD_SENTDIST=
        new FeatureDescription<Double>(FeatureType.FT_SCALAR, "DistanceLog");
    
    
 
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SENTDIST);        
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_SENTDIST,getSentDist(inst));
    }
    
     public Double getSentDist(PairInstance inst)
    {
        return Math.log(1.0+
                getDistance(inst.getAntecedent().getMarkable(), inst.getAnaphor().getMarkable()));
    }
    
    /** Computes the sentence distance among two markables */
    private int getDistance(final Markable markable1, final Markable markable2)
    {
        final int distance1 = Integer.parseInt(
                markable1.getAttributeValue(SENTENCE_ID_ATTRIBUTE));
        final int distance2 = Integer.parseInt(
                markable2.getAttributeValue(SENTENCE_ID_ATTRIBUTE));
        return Math.abs((distance1-distance2));
    }
}
