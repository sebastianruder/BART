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

package elkfed.ml.maxent;

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
public class ClassifierMulticlass implements OfflineClassifier {
    List<FeatureDescription> _fds;
    List<FeatureCombo> _combos;
    String[][] _combo_descs;
    Alphabet dict;
    Alphabet _outcome_dict;
    int bias_idx;
    double[] _weights;
    String _prefix;
    
    public ClassifierMulticlass(String prefix, String[][] combo_descs)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        _combo_descs=combo_descs;
        ObjectInputStream ios=
                new ObjectInputStream(new FileInputStream(prefix+".dict"));
        dict=(Alphabet)ios.readObject();
        bias_idx=dict.lookupIndex(ClassifierSinkBinary.BIAS_FEATURE);
        dict.stopGrowth();
        ios.close();
        ios=new ObjectInputStream(new FileInputStream(prefix+".param"));
        _outcome_dict=(Alphabet)ios.readObject();
        _weights=(double[])ios.readObject();
        ios.close();
    }
    
    public ClassifierMulticlass(String prefix)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        this(prefix,ClassifierSinkBinary.monomial1);
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
    
    public void adjustWeight(String name, double offset)
    {
        int idx=dict.lookupIndex(name);
        if (idx>-1)
            _weights[idx]+=offset;
        else
            throw new RuntimeException("no such weight:"+name);
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
    
    public double[] getScore(final Instance inst)
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
        double[] outcome=new double[_outcome_dict.size()];
        fv.mat_mul(_weights,outcome);
        return outcome;
    }

//    public <T extends Instance> List<T> getRanking(List<T> cands)
//    {
//    }

    public Alphabet getDict() {
        return dict;
    }

    public void classify(List<? extends Instance> problems, List output) {
        for (Instance inst: problems)
        {
            double[] result=getScore(inst);
            Object best_result=_outcome_dict.lookupObject(0);
            double best_value=result[0];
            for (int i=1;i<_outcome_dict.size();i++) {
                if (result[i]>best_value) {
                    best_result=_outcome_dict.lookupObject(i);
                    best_value=result[i];
                }
            }
            output.add(best_result);
        }
    }

    public void classify(List<? extends Instance> problems, List output, List<Double> confidence) {
        // confidence values are log-odds, i.e., log(p/(1-p))
        for (Instance inst: problems)
        {
            double[] result=getScore(inst);
            Object best_result=_outcome_dict.lookupObject(0);
            double best_value=Math.exp(result[0]);
            double total_value=best_value;
            for (int i=1;i<_outcome_dict.size();i++) {
                double val=Math.exp(result[i]);
                total_value+=val;
                if (val>best_value) {
                    best_result=_outcome_dict.lookupObject(i);
                    best_value=val;
                }
            }
            output.add(best_result);
            confidence.add(best_value/(total_value-best_value));
        }
    }
}
