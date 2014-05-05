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
package elkfed.expletives;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.ArrayList;
import java.util.List;

/**
 * extracts the four preceding and following POS tags
 * @author versley
 */
public class EF_POS implements FeatureExtractor<ExpletiveInstance> {
    public static final List<FeatureDescription<String>> left_pos;
    public static final List<FeatureDescription<String>> right_pos;
    
    static {
        left_pos=new ArrayList<FeatureDescription<String>>(4);
        right_pos=new ArrayList<FeatureDescription<String>>(4);
        for (int i=1;i<=4;i++) {
            left_pos.add(new FeatureDescription<String>(FeatureType.FT_STRING,
                    "POS_L"+i));
            right_pos.add(new FeatureDescription<String>(FeatureType.FT_STRING,
                    "POS_R"+i));
        }
    }

    public void describeFeatures(List<FeatureDescription> fds) {
        for (FeatureDescription fd: left_pos) {
            fds.add(fd);
        }
        for (FeatureDescription fd: right_pos) {
            fds.add(fd);
        }
    }

    public void extractFeatures(ExpletiveInstance inst) {
        String[] pos=inst.getPOS();
        int idx=inst.getIdx();
        for (int i=0; i<4; i++) {
            int idx1=idx-i-1;
            String val;
            if (idx1<0) {
                val="*BOS*";
            } else {
                val=pos[idx1];
            }
            inst.setFeature(left_pos.get(i), val);
        }
        for (int i=0; i<4; i++) {
            int idx1=idx+i+1;
            String val;
            if (idx1>=pos.length) {
                val="*EOS*";
            } else {
                val=pos[idx1];
            }
            inst.setFeature(right_pos.get(i), val);
        }
    }
}
