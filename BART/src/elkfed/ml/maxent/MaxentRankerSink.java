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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.RankerSink;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.SparseMatrixWriter;
import elkfed.ml.util.SparseVector;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author yannick
 */
public class MaxentRankerSink implements RankerSink {

    List<FeatureDescription> _fds;
    List<FeatureCombo> _combos;
    String[][] _combo_descs;
    Alphabet dict;
    SparseMatrixWriter _os;
    TIntArrayList _offsets;
    int n_written;
    String _prefix;

    public MaxentRankerSink(String prefix, String[][] combo_descs)
            throws FileNotFoundException, IOException {
        _combo_descs = combo_descs;
        _prefix = prefix;
        _os = new SparseMatrixWriter(prefix);
        _offsets = new TIntArrayList();
        n_written = 0;
        _offsets.add(n_written);
        dict = new Alphabet();
    }

    public MaxentRankerSink(String prefix)
            throws FileNotFoundException, IOException {
        this(prefix, ClassifierSinkBinary.monomial1);
    }

    public void setHeader(List<FeatureDescription> fds) {
        _fds = fds;
        _combos = new ArrayList<FeatureCombo>();
        for (String[] combo_desc : _combo_descs) {
            FeatureCombo combo = new FeatureCombo(fds, combo_desc, dict);
            _combos.add(combo);
        }
    }

    public SortedMap<Integer, Double> makeFV(final Instance inst) {
        SortedMap<Integer, Double> indexToValue = new TreeMap<Integer, Double>();
        for (FeatureCombo cmb : _combos) {
            cmb.addWeightedCombinations(inst, indexToValue);
        }
        return indexToValue;
    }

    public void write(List<? extends Instance> insts) throws IOException {
        List<SparseVector> pos_fvs = new ArrayList<SparseVector>();
        List<SparseVector> neg_fvs = new ArrayList<SparseVector>();
        for (Instance inst : insts) {
            SortedMap<Integer, Double> fvec = makeFV(inst);
            Set<Integer> keyset = fvec.keySet();
            int[] indices = new int[keyset.size()];
            int ind = 0;
            for (int key : keyset) {
                indices[ind++] = key;
            }
            double[] vals = new double[indices.length];
            for (int i = 0; i < indices.length; i++) {
                vals[i] = fvec.get(indices[i]);
            }
            SparseVector fv = new SparseVector(indices, vals);
            if ((Boolean) inst.getFeature(_fds.get(_fds.size() - 1))) {
                pos_fvs.add(fv);
            } else {
                neg_fvs.add(fv);
            }
        }
        if (!pos_fvs.isEmpty() && !neg_fvs.isEmpty()) {
            for (SparseVector fv : pos_fvs) {
                _os.write(fv);
            }
            n_written += pos_fvs.size();
            _offsets.add(n_written);
            for (SparseVector fv : neg_fvs) {
                _os.write(fv);
            }
            n_written += neg_fvs.size();
            _offsets.add(n_written);
        }
    }

    public void flush() throws FileNotFoundException, IOException {
        ObjectOutputStream ds = new ObjectOutputStream(
                new FileOutputStream(_prefix + ".dict"));
        ds.writeObject(dict);
        ds.close();
    }

    public void close() throws FileNotFoundException, IOException {
        ObjectOutputStream v_out = new ObjectOutputStream(
                new FileOutputStream(_prefix + "_pos_neg.obj"));
        v_out.writeObject(_offsets);
        v_out.close();
        flush();
        _os.close();
    }
}
