/*
 * RankerSink.java
 *
 * Created on August 4, 2007, 2:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml.maxent;

import elkfed.ml.util.Alphabet;
import elkfed.ml.util.SparseVector;
import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.InstanceWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author yannick
 */
//TODO: can we save the FeatureCombo list to the dict file and
// then automagically know the feature combos that were used in training?
public class ClassifierSinkBinary implements InstanceWriter {
    List<FeatureDescription> _fds;
    List<FeatureCombo> _combos;
    String[][] _combo_descs;
    Alphabet<String> dict;
    public static final String BIAS_FEATURE="**BIAS**";
    int bias_idx;
    ObjectOutputStream _os;
    String _prefix;
    
    public static final String[][] monomial1 =
            new String[][] { new String[] { "**" }};
    public static final String[][] monomial2 =
            new String[][] { new String[] { "**", "**" }};
    
    public static String[][] comboFromString(String opts)
    {
        if (opts==null)
        {
            return monomial1;
        } else {
            String[] cmb1=opts.split("\\|");
            String[][] result=new String[cmb1.length][];
            for (int i=0;i<cmb1.length; i++)
            {
                result[i]=cmb1[i].split(" +");
                System.err.format("%2d: %d slots(%s)\n",i,result[i].length,cmb1[i]);
            }
            return result;
        }
    }
    
    public ClassifierSinkBinary(String prefix, String[][] combo_descs) throws FileNotFoundException, IOException {
        _combo_descs=combo_descs;
        _prefix=prefix;
        _os=new ObjectOutputStream(new FileOutputStream(prefix+".obj"));
        dict=new Alphabet<String>();
        bias_idx=dict.lookupIndex(BIAS_FEATURE);
    }
    
    public ClassifierSinkBinary(String prefix) throws FileNotFoundException, IOException {
        this(prefix, monomial1);
    }
    
    public void setHeader(List<FeatureDescription> fds) {
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
        for (FeatureCombo cmb:_combos) {
            cmb.addWeightedCombinations(inst,indexToValue);
        }
        return indexToValue;
    }
    
    
    public void flush() throws FileNotFoundException, IOException {
        _os.flush();
        _os.reset();
        ObjectOutputStream ds=new ObjectOutputStream(
                new FileOutputStream(_prefix+".dict"));
        ds.writeObject(dict);
        ds.close();
    }
    
    public void close() throws FileNotFoundException, IOException {
        flush();
        _os.close();
    }
    
    public void write(Instance inst) throws IOException {
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
        _os.writeObject(fv);
        _os.writeObject(inst.getFeature(_fds.get(_fds.size()-1)));
    }
}
