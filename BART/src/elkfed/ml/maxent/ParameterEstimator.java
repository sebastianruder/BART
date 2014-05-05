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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import riso.numerical.LBFGS.ExceptionWithIflag;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.CompressedMatrixWriter;
import elkfed.ml.util.DenseVector;
import elkfed.ml.util.LoadedSparseMatrix;
import elkfed.ml.util.MappedCompressedMatrix;
import elkfed.ml.util.Minimization;
import elkfed.ml.util.SparseMatrix;
import elkfed.ml.util.SparseVector;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author yannick
 */
public class ParameterEstimator implements elkfed.ml.util.Minimizable
{
    double C=1.0;
    private static int MAX_ANTE=16384;
    protected SparseMatrix _vectors;
    protected TIntArrayList _offsets;
    protected List<List<SparseVector>> _pos_insts;
    protected List<List<SparseVector>> _neg_insts;
    
    public ParameterEstimator(int n_parameters,
            SparseMatrix vectors,
            TIntArrayList offsets) {
        _vectors=vectors;
        _offsets=offsets;
    }
    
    public double evaluateFunction(double[] parameters, double[] grad) {
        double val=C*DenseVector.dotSelf(parameters);
        double val0=val;
        for (int i=0; i<parameters.length; i++) {
            grad[i]=2.0*C*parameters[i];
        }
        double m_max=0.0;
        double m_min=0.0;
	double[] prod_pos=new double[MAX_ANTE];
	double[] prod_neg=new double[MAX_ANTE];
        ArrayList<SparseVector> pos_lst=new ArrayList();
        ArrayList<SparseVector> neg_lst=new ArrayList();
        int n_examples=_offsets.size()/2;
        for (int i=0; i<n_examples; i++) {
            int off=i*2;
            pos_lst.clear();
            neg_lst.clear();
            _vectors.getRange(_offsets.get(off),
                    _offsets.get(off+1), pos_lst);
            _vectors.getRange(_offsets.get(off+1),
                    _offsets.get(off+2), neg_lst);
	    int k=0;
	    // step 1: determine all <w,x_k>
            for (SparseVector ex: pos_lst) {
                double prod=ex.dotProduct(parameters);
		prod_pos[k]=prod;
		k++;
	    }
	    k=0;
	    for (SparseVector ex: neg_lst) {
		double prod=ex.dotProduct(parameters);
		prod_neg[k]=prod;
		k++;
	    }
	    // step 2: determine normalizing constant
	    m_max=prod_pos[0];
	    for (k=1; k<pos_lst.size(); k++) {
		if (prod_pos[k]>m_max) {
		    m_max=prod_pos[k];
		}
	    }
	    for (k=0; k<neg_lst.size(); k++) {
		if (prod_neg[k]>m_max) {
		    m_max=prod_neg[k];
		}
	    }
            double mu_good=0.0;
            double mu_bad=0.0;
	    // step 3: determine mu_good, mu_bad
	    for (k=0; k<pos_lst.size(); k++) {
		double m=Math.exp(prod_pos[k]-m_max);
		SparseVector ex=pos_lst.get(k);
		mu_good+=m;
		prod_pos[k]=m;
            }
	    for (k=0; k<neg_lst.size(); k++) {
		double m=Math.exp(prod_neg[k]-m_max);
		SparseVector ex=neg_lst.get(k);
		mu_bad+=m;
		prod_neg[k]=m;
	    }
            val -= Math.log(mu_good)-Math.log(mu_bad+mu_good);
	    // step 4: update derivative
	    double update_pos=-mu_bad/(mu_good*(mu_good+mu_bad));
	    double update_neg=1.0/(mu_good+mu_bad);
	    for (k=0; k<pos_lst.size(); k++) {
		double m=prod_pos[k];
		SparseVector ex=pos_lst.get(k);
		ex.addTo(grad, update_pos*m);
	    }
	    for (k=0; k<neg_lst.size(); k++) {
		double m=prod_neg[k];
		SparseVector ex=neg_lst.get(k);
		ex.addTo(grad, update_neg*m);
	    }
        }
        System.err.format("loss=%f, perplexity=%f\n",val,
                Math.exp((val-val0)/n_examples));
        return val;
    }

    public static void do_estimation(String prefix)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream is;

        is = new ObjectInputStream(new FileInputStream(prefix + ".dict"));
        Alphabet dict = (Alphabet) is.readObject();
        int dict_size = dict.size();
        is.close();
        double[] parameters = new double[dict_size];
        SparseMatrix m;
        FileChannel f=new FileInputStream(prefix+"_val.bin").getChannel();
        // use a MappedCompressed... if the file doesn't fit onto
        // the heap
        boolean is_large=(f.size()*1.6>Runtime.getRuntime().maxMemory());
        f.close();
        if (is_large) {
            System.err.format("%s is large, using MappedCompressedMatrix\n");
            CompressedMatrixWriter w=new CompressedMatrixWriter(prefix);
            w.copy_values();
            m=new MappedCompressedMatrix(prefix);
        } else {
            m=new LoadedSparseMatrix(prefix);
        }
        is = new ObjectInputStream(new FileInputStream(prefix+"_pos_neg.obj"));
        TIntArrayList offsets=(TIntArrayList) is.readObject();
        is.close();

        try {
            ParameterEstimator estimator =
                    new ParameterEstimator(dict_size, m, offsets);
            //Minimization.testGradient(parameters, estimator);
            Minimization.runLBFGS(parameters, estimator);
        } catch (ExceptionWithIflag ex) {
            ex.printStackTrace();
            throw new RuntimeException("Minimization failed", ex);
        }
        if (dict != null) {
            final Map<String, Double> paramValues = new HashMap<String, Double>();
            for (int i = 0; i < parameters.length; i++) {
                paramValues.put((String) dict.lookupObject(i),
                        parameters[i]);
            }
            System.err.format("highest-weight parameters for %s\n", prefix);
            ArrayList<String> keys = new ArrayList<String>(paramValues.keySet());
            Collections.sort(keys, new Comparator<String>() {

                public int compare(String key1, String key2) {
                    double delta = Math.abs(paramValues.get(key1)) -
                            Math.abs(paramValues.get(key2));
                    if (delta < 0.0) {
                        return -1;
                    } else if (delta > 0.0) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            });
            for (String key : keys.subList(keys.size() < 200 ? 0 : keys.size() - 200,
                    keys.size())) {
                System.err.format("%s: %f\n", key, paramValues.get(key));
            }
        }
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(prefix + ".param"));
        oos.writeObject(parameters);
    }
    
    public static void main(String[] args) {
        System.out.println("ParameterEstimator");
        try {
            if (args.length>0) {
                for (String s: args) {
                    do_estimation(s);
                }
            } else {
                do_estimation("models/coref/ranker_pro");
                do_estimation("models/coref/ranker_def");
                do_estimation("models/coref/ranker_app");
            }
            //TestMaximizable.testValueAndGradient(estimator);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}