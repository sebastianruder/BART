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

import elkfed.ml.ClassifierFactory;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.InstanceWriter;
import elkfed.ml.OfflineClassifier;
import java.io.IOException;

/** realizes a stacking scheme for learning classifiers
 *  that can be fed into classifiers for the same datapoints
 *  (normally, testing the classifier on the training set would
 *  give an overestimation of real performance, which is why
 *  we train classifier for multiple folds and test with a
 *  classifier that didn't use that particular fold).
 * @author versley
 */
public class StackingClassifierFactory implements ClassifierFactory {
    int n_folds=5;
    ClassifierFactory delegate;
    
    public final static FeatureDescription<Integer> FD_FOLD_NO=new FeatureDescription(
            FeatureType.FT_SCALAR, Integer.class, "FOLD_NO");
    
    public StackingClassifierFactory(ClassifierFactory chld) {
        delegate=chld;
    }
    
    public StackingClassifierFactory(ClassifierFactory chld, int n) {
        delegate=chld;
        n_folds=n;
    }

    public InstanceWriter getSink(String model_name, String options, String learner) throws IOException {
        InstanceWriter iw_all=delegate.getSink(model_name,options,learner);
        InstanceWriter[] iw_folds=new InstanceWriter[n_folds];
        for (int i=0; i<n_folds;i++) {
            iw_folds[i]=delegate.getSink(String.format("%s_%02d",model_name,i),options,learner);
        }
        return new StackingWriter(iw_all,iw_folds);
    }

    public void do_learning(String model_name, String options, String learner) throws IOException {
       delegate.do_learning(model_name, options, learner);
       for (int i=0; i<n_folds;i++) {
           delegate.do_learning(String.format("%s_%02d",model_name,i),options,learner);
       }
    }

    public OfflineClassifier getClassifier(String model_name, String options, String learner) throws IOException {
        OfflineClassifier oc_all=delegate.getClassifier(model_name,options,learner);
        OfflineClassifier[] oc_folds=new OfflineClassifier[n_folds];
        for (int i=0; i<n_folds;i++) {
            oc_folds[i]=delegate.getClassifier(String.format("%s_%02d",model_name,i),options,learner);
        }
        return new StackingClassifier(oc_all,oc_folds);
    }
}
