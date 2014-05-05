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

/** determines the root path of the antecedent.
 * this should approximate syntax-based salience
 * if we give it enough data...
 */
public class FE_SynPos implements PairFeatureExtractor {
    public static final FeatureDescription<String> FD_ANTE_SYN_POS=
            new FeatureDescription<String>(FeatureType.FT_STRING,"Ante_SyntaxPath");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_ANTE_SYN_POS);
    }

    private static int nthIndex(String path,String sep,int n)
    {
        int idx=0;
        for (int i=0;i<n;i++)
        {
            idx=path.indexOf(sep,idx)+1;
            if (idx==0) return -1;
        }
        return idx;
    }
    
    public void extractFeatures(PairInstance inst) {
        String rootPath=inst.getAntecedent().getRootPath();
        int off=nthIndex(rootPath,".",3);
        if (off == -1)
        {
            inst.setFeature(FD_ANTE_SYN_POS, rootPath);
        }
        else
        {
            inst.setFeature(FD_ANTE_SYN_POS, rootPath.substring(0,off));
        }
    }
    
}
