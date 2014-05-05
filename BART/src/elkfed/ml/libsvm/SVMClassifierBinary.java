/*
 * Copyright 2011 Yannick Versley / Univ. Tuebingen
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

package elkfed.ml.libsvm;

import elkfed.ml.maxent.ClassifierSinkBinary;
import elkfed.ml.maxent.FeatureCombo;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.SparseVector;
import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author yannick
 */
@SuppressWarnings("unchecked")
public class SVMClassifierBinary implements OfflineClassifier {
	List<FeatureDescription> _fds;
    List<FeatureCombo> _combos;
    String[][] _combo_descs=ClassifierSinkBinary.monomial1;
    Alphabet dict;
    int bias_idx;
    svm_model model;
    String _prefix;
    
    public SVMClassifierBinary(String prefix)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream ios=
                new ObjectInputStream(new FileInputStream(prefix+".dict"));
        dict=(Alphabet)ios.readObject();
        bias_idx=dict.lookupIndex(ClassifierSinkBinary.BIAS_FEATURE);
        dict.stopGrowth();
        ios.close();
        model=svm.svm_load_model(prefix+".model");
    }

    public void setHeader(List<FeatureDescription> fds)
    {
        _fds=fds;
        _combos=new ArrayList<FeatureCombo>();
        for (String[] combo_desc: _combo_descs) {
            FeatureCombo combo=new FeatureCombo(fds,combo_desc,dict);
            _combos.add(combo);
        }
    }
    
    public SortedMap<Integer, Double> makeFV(final Instance inst) {
        SortedMap<Integer, Double> indexToValue = new TreeMap<Integer,Double>();
        indexToValue.put(bias_idx,1.0);
        for (FeatureCombo cmb:_combos)
        {
            cmb.addWeightedCombinations(inst,indexToValue);
        }
        return indexToValue;
    }
    
    public double getScore(final Instance inst)
    {
        SortedMap<Integer,Double> fvec=makeFV(inst);
        Set<Integer> keyset=fvec.keySet();
        int[] indices=new int[keyset.size()];
        int ind=0;
        for (int key: keyset) {
            indices[ind++]=key;
        }
        double[] vals=new double[indices.length];
        for (int i=0; i<indices.length; i++) {
            vals[i]=fvec.get(indices[i]);
        }
        SparseVector fv=new SparseVector(indices,vals);
        SVMExample ex=SVMExample.make_binary_example(fv, true);
        return model.get_prediction(ex);
    }

    public <T extends Instance> List<T> getRanking(List<T> cands)
    {
        List<Double> scores=new ArrayList<Double>();
        List<T> result=new ArrayList<T>();
        cand_loop: for (T cand: cands)
        {
            double score=getScore(cand);
            for (int i=0; i<result.size(); i++)
            {
                if (scores.get(i)<score)
                {
                    result.add(i,cand);
                    scores.add(i,score);
                    continue cand_loop;
                }
            }
            result.add(cand);
            scores.add(score);
        }
        return result;
    }

    public Alphabet getDict() {
        return dict;
    }

    public void classify(List<? extends Instance> problems, List output) {
        for (Instance inst: problems)
        {
            double result=getScore(inst);
            output.add(result>0.0);
        }
    }

    public void classify(List<? extends Instance> problems, List output, List<Double> confidence) {
        // confidence values are log-odds, i.e., log(p/(1-p))
        for (Instance inst: problems)
        {
            double result=getScore(inst);
            if (result>0.0) {
                output.add(true);
                confidence.add(result);
            } else {
                output.add(false);
                confidence.add(-result);
            }
        }
    }
}
