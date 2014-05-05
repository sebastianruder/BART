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
package elkfed.ml.tournament;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.Instance;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author versley
 */
public class CombinerFirst implements FeatureExtractor {
    protected final List<FeatureDescription> singleFeatures;
    protected final List<FeatureDescription> features1;
    
    public CombinerFirst(List<FeatureDescription> features)
    {
        singleFeatures=features;
        features1=new ArrayList<FeatureDescription>(features.size());
        for (FeatureDescription fd : singleFeatures)
        {
            FeatureDescription f1=new FeatureDescription(fd.type,
                    fd.cls, fd.name + "_1");
            features1.add(f1);
        }
    }
    
    public void describeFeatures(List fds) {
        fds.addAll(features1);
    }

    public void extractFeatures(Instance inst0) {
        CandPairInstance inst=(CandPairInstance) inst0;
        for (int i=0; i<singleFeatures.size(); i++)
        {
            FeatureDescription fd=singleFeatures.get(i);
            FeatureDescription f1=features1.get(i);
            Object o1=inst.inst1.getFeature(fd);
            if (o1!=null)
            {
                inst.setFeature(f1, o1);
            }
        }
    }
}

