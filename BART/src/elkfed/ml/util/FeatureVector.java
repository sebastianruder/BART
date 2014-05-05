/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elkfed.ml.util;

import elkfed.ml.util.Alphabet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author versley
 */
public class FeatureVector<T> implements IVector {
    Alphabet<T> _dict;
    SortedMap<Integer,Double> _vals;
    
    public FeatureVector(Alphabet<T> dict)
    {
        _dict=dict;
        _vals=new TreeMap<Integer,Double>();
    }
    
    public void addTo(double[] vec, double factor) {
        for (Integer k: _vals.keySet())
        {
            vec[k]+=_vals.get(k)*factor;
        }
    }

    public double dotProduct(double[] vec) {
        double sum=0.0;
        for (Integer k: _vals.keySet())
        {
            sum+=vec[k]*_vals.get(k);
        }
        return sum;
    }

    public void setFeatureValue(T key, double val)
    {
        int idx=_dict.lookupIndex(key);
        if (idx==-1)
            return;
        _vals.put(idx, val);
    }

    public void addFeatureValue(T key, double val) {
        int idx=_dict.lookupIndex(key);
        if (idx==-1)
           return;
        Double oldvalD=_vals.get(idx);
        double oldval;
        if (oldvalD==null) {
            oldval=0.0;
        } else {
            oldval=oldvalD;
        }
        _vals.put(idx, oldval+val);
    }
    
    public double getFeatureValue(T key)
    {
        int idx=_dict.lookupIndex(key);
        if (idx==-1)
            return 0.0;
        return _vals.get(idx);
    }
    

    /** returns a compact representation of this feature vector */
    IVector toVector()
    {
        int size=_vals.size();
        int[] keys=new int[size];
        double[] vals=new double[size];
        int pos=0;
        for (Integer k: _vals.keySet())
        {
            keys[pos]=k;
            vals[pos]=_vals.get(k);
            pos++;
        }
        return new SparseVector(keys,vals);
    }

    public void put(KVFunc func) {
        for (Integer k: _vals.keySet())
        {
            func.put(k,_vals.get(k));
        }
    }

    private class FeatureSet implements Set<T>
    {
        public int size() {
            return _vals.size();
        }

        public boolean isEmpty() {
            return _vals.isEmpty();
        }

        public boolean contains(Object key) {
            if (!_dict.contains(key))
            {
                return false;
            }
            return _vals.containsKey(_dict.lookupIndex((T)key));
        }

        public Iterator<T> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Object[] toArray() {
            Object[] objs=new Object[_vals.size()];
            int pos=0;
            for (int key: _vals.keySet())
            {
                objs[pos++]=_dict.lookupObject(key);
            }
            return objs;
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean add(T e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }        
    }
    
    public Set<T> getFeatures()
    {
        return new FeatureSet();
    }
    
    @Override
    public String toString() {
        if (_vals.isEmpty()) {
            return "FV{}";
        }
        StringBuffer buf=new StringBuffer();
        buf.append("FV{");
        for (int k: _vals.keySet()) {
            buf.append(_dict.lookupObject(k));
            buf.append(":");
            buf.append(_vals.get(k));
            buf.append(", ");
        }
        buf.setLength(buf.length()-2);
        buf.append("}");
        return buf.toString();
    }

    public static void putStringCombo(FeatureVector<? super String> fv,
            String prefix, int nComb, String... args)
    {
        StringBuffer buf=new StringBuffer(prefix);
        putStringCombo(fv,buf,args,0,nComb);
    }

    public static void putStringCombo(FeatureVector<? super String> fv,
            StringBuffer buf, String[] args, int pos, int nComb) {
        int oldpos=buf.length();
        int next_argpos=pos+1;
        if (nComb>0) {
            buf.append("^").append(args[pos]);
            if (next_argpos==args.length) {
                fv.addFeatureValue(buf.toString(), 1.0);
            } else {
                putStringCombo(fv,buf,args,next_argpos,nComb-1);
            }
        }
        buf.setLength(oldpos);
        buf.append("^");
        if (next_argpos==args.length) {
            fv.addFeatureValue(buf.toString(), 1.0);
        } else {
            putStringCombo(fv,buf,args,next_argpos,nComb);
        }
    }

    public static void main(String[] args) {
        Alphabet<String> alph=new Alphabet<String>();
        FeatureVector<String> fv=new FeatureVector<String>(alph);
        putStringCombo(fv,"FA",2,"B","C","D","E");
        putStringCombo(fv,"FX",2,"1","2","3","4");
        System.out.println(fv);
    }
}
