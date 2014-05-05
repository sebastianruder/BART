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
import elkfed.ml.InstanceWriter;
import java.io.IOException;
import java.util.List;

import static elkfed.ml.stacking.StackingClassifierFactory.FD_FOLD_NO;

/**
 *
 * @author versley
 */
class StackingWriter implements InstanceWriter {
    final InstanceWriter _iw_all;
    final InstanceWriter[] _iw_folds;
    
    public StackingWriter(InstanceWriter iw_all, InstanceWriter[] iw_folds) {
        _iw_all=iw_all;
        _iw_folds=iw_folds;
    }
    
    public void setHeader(List<FeatureDescription> fds) throws IOException {
        _iw_all.setHeader(fds);
        for (InstanceWriter iw: _iw_folds) {
            iw.setHeader(fds);
        }
    }

    public void write(Instance inst) throws IOException {
        _iw_all.write(inst);
        Integer fold_no=inst.getFeature(FD_FOLD_NO);
        if (fold_no!=null) {
            int in_fold=fold_no%_iw_folds.length;
            for (int i=0; i< _iw_folds.length; i++) {
                if (i!=in_fold) {
                    _iw_folds[fold_no%_iw_folds.length].write(inst);
                }
            }
        }
    }

    public void close() throws IOException {
        _iw_all.close();
        for (InstanceWriter iw: _iw_folds) {
            iw.close();
        }
    }

    public void flush() throws IOException {
        _iw_all.flush();
        for (InstanceWriter iw: _iw_folds) {
            iw.flush();
        }
    }

}
