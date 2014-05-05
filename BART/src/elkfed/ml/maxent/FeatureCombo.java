/*
 * FeatureCombo.java
 *
 * Created on 26. September 2007, 15:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml.maxent;

import elkfed.ml.util.Alphabet;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.Instance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author versley
 */
// TODO: reorganize to have the recursion the other way around
// (single features -> * -> **
// TODO: convert this to use elkfed.ml.FeatureVector
public class FeatureCombo {
    protected int num_star;
    protected int num_starstar;
    protected List<FeatureDescription> _fds;
    protected FeatureDescription[] features;
    private Alphabet alphabet;
    
    /** Creates a new instance of FeatureCombo */
    public FeatureCombo(List<FeatureDescription> fds, Alphabet dict) {
        num_star=0;
        num_starstar=1;
        _fds=fds;
        features=new FeatureDescription[0];
        alphabet=dict;
    }
    
    public FeatureCombo(List<FeatureDescription> fds,
            String[] feature_names, Alphabet dict)
    {
        _fds=fds;
        List<FeatureDescription> feats=new ArrayList<FeatureDescription>();
        loop_i: for (int i=0; i<feature_names.length; i++)
        {
            if ("**".equals(feature_names[i])) {
                num_starstar++;
            } else if ("*".equals(feature_names[i])) {
                num_star++;
            } else {
                System.err.println("looking up feature `"+feature_names[i]+"'");
                for (int j=0; j< fds.size(); j++)
                {
                    if (fds.get(j).name.equals(feature_names[i])) {
                        feats.add(fds.get(j));
                        continue loop_i;
                    }
                }
                System.err.println("Cannot find feature "+feature_names[i]);
            }
        }
        features=feats.toArray(new FeatureDescription[feats.size()]);
        alphabet=dict;
    }
    
    public void addWeightedCombinations(Instance inst, Map<Integer,Double> fweights) {
        StringBuffer sb=new StringBuffer();
        try {
            addWeightedCombinations1(inst, fweights, sb,0,1.0);
        } catch (UnknownValueException ex) {
            // ignore
        }
    }
    
    void addWeightedCombinations1(Instance inst, Map<Integer,Double> fweights,
            StringBuffer sb, int dim_ss, double w) throws UnknownValueException {
        if (dim_ss>=num_starstar) {
            addWeightedCombinations2(inst, fweights, sb, 0, w);
            return;
        }
        int pos=sb.length();
        for (int i=0; i<_fds.size()-1; i++)
        {
            FeatureDescription fd=_fds.get(i);
            try {
                sb.setLength(pos);
                double w2=addFeature(inst, sb, fd);
                addWeightedCombinations1(inst,fweights,sb,dim_ss+1,w*w2);
            } catch (UnknownValueException ex) {
                // ignore
            }
        }
    }
    void addWeightedCombinations2(Instance inst, Map<Integer,Double> fweights,
            StringBuffer sb, int dim_s, double w) throws UnknownValueException {
        if (dim_s>=num_star) {
            addWeightedCombinations3(inst, fweights, sb, w);
            return;
        }
        int pos=sb.length();
        for (int i=0; i<_fds.size()-1; i++)
        {
            FeatureDescription fd=_fds.get(i);
            try {
                if (fd.type!=FeatureType.FT_SCALAR)
                {
                    sb.setLength(pos);
                    double w2=addFeature(inst, sb, fd);
                    addWeightedCombinations2(inst,fweights,sb,dim_s+1,w*w2);
                }
            } catch (UnknownValueException ex) {
                // ignore
            }
        }
    }
    
    void addWeightedCombinations3(Instance inst, Map<Integer,Double> fweights,
            StringBuffer sb, double w) throws UnknownValueException {
        for (int i=0; i<features.length; i++) {
            w*=addFeature(inst, sb, features[i]);
        }
        int f_num=alphabet.lookupIndex(sb.toString());
        if (f_num!=-1)
        {
            fweights.put(f_num,w);
        }
    }
    
    private double addFeature(Instance inst, StringBuffer sb,
            FeatureDescription fd) throws UnknownValueException {
        switch (fd.type) {
            case FT_BOOL:
                Boolean valB=(Boolean)inst.getFeature(fd);
                if (valB==null) {
                    throw new UnknownValueException(fd);
                } else if (valB) {
                    sb.append(fd.name).append("+^");
                } else {
                    sb.append(fd.name).append("-^");
                }
                break;
            case FT_SCALAR:
                sb.append(fd.name).append("^");
                Number valN=(Number)inst.getFeature(fd);
                if (valN==null)
                {
                    throw new UnknownValueException(fd);
                } else {
                    double valD=valN.doubleValue();
                    if (valD==0.0) throw new UnknownValueException(fd);
                    return valD;
                }
            case FT_NOMINAL_ENUM:    //same treatment as FT_STRING
            case FT_STRING:
                Object valO=inst.getFeature(fd);
                if (valO==null) {
                    //sb.append(fd.name).append("?");
                    throw new UnknownValueException(fd);
                } else {
                    sb.append(fd.name).append("=").append(valO).append("^");
                }
                break;
            case FT_TREE_STRING:
            case FT_TREE_TREE:
                throw new RuntimeException("Don't know what to do when combining a tree");
            default:
                throw new RuntimeException("unknown feature type:"+fd.type);
        }
        return 1.0;
    }
}
