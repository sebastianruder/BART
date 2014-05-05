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
import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.Ranker;
import elkfed.ml.util.SparseVector;
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
public class MaxentRanker implements Ranker {
   List<FeatureDescription> _fds;
    List<FeatureCombo> _combos;
    String[][] _combo_descs;
    Alphabet dict;
    double[] _weights;
    String _prefix;
    
    public MaxentRanker(String prefix, String[][] combo_descs)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        _combo_descs=combo_descs;
        ObjectInputStream ios=
                new ObjectInputStream(new FileInputStream(prefix+".dict"));
        dict=(Alphabet)ios.readObject();
        dict.stopGrowth();
        ios.close();
        ios=new ObjectInputStream(new FileInputStream(prefix+".param"));
        _weights=(double[])ios.readObject();
        ios.close();
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
        return fv.dotProduct(_weights);
    }

    public SortedMap<Integer, Double> makeFV(final Instance inst) {
        SortedMap<Integer, Double> indexToValue = new TreeMap<Integer,Double>();
        for (FeatureCombo cmb:_combos) {
            cmb.addWeightedCombinations(inst,indexToValue);
        }
        return indexToValue;
    }
    
    public <T extends Instance> T getHighestRanked(List<T> cands)
    {
        T best=null;
        double best_score=Double.MIN_VALUE;
        for (T cand: cands)
        {
            double score=getScore(cand);
            if (best==null || score>best_score)
            {
                best=cand;
                best_score=score;
            }
        }
        return best;
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
}
