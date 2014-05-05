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
package elkfed.ml.stacking;

import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import java.util.List;

import static elkfed.ml.stacking.StackingClassifierFactory.FD_FOLD_NO;

/**
 *
 * @author versley
 */
public class StackingClassifier implements OfflineClassifier {
    OfflineClassifier _oc_all;
    OfflineClassifier[] _oc_folds;
    
    public StackingClassifier(OfflineClassifier oc_all,
            OfflineClassifier[] oc_folds) {
        _oc_all=oc_all;
        _oc_folds=oc_folds;
    }

    public void setHeader(List<FeatureDescription> fds) {
        _oc_all.setHeader(fds);
        for (OfflineClassifier oc: _oc_folds) {
            oc.setHeader(fds);
        }
    }

    public void classify(List<? extends Instance> problems, List output) {
        Integer fold_no=problems.get(0).getFeature(FD_FOLD_NO);
        if (fold_no!=null) {
            int in_fold=fold_no%_oc_folds.length;
            _oc_folds[in_fold].classify(problems, output);
        } else {
            _oc_all.classify(problems, output);
        }
    }
    
    public void classify(List<? extends Instance> problems, List output, List<Double> confidence) {
        Integer fold_no=problems.get(0).getFeature(FD_FOLD_NO);
        if (fold_no!=null) {
            int in_fold=fold_no%_oc_folds.length;
            _oc_folds[in_fold].classify(problems, output);
        } else {
            _oc_all.classify(problems, output);
        }
    }

}
