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
package elkfed.ml.maxent;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import riso.numerical.LBFGS.ExceptionWithIflag;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.DenseVector;
import elkfed.ml.util.Minimizable;
import elkfed.ml.util.Minimization;
import elkfed.ml.util.SparseVector;
import gnu.trove.list.array.TIntArrayList;

/**
 * estimates the parameters for a binary MaxEnt classifier
 * @author yannick
 */
public class ParameterEstimatorMulticlass implements Minimizable {

    double C = 1.0;
    protected List<SparseVector> _insts;
    protected Alphabet<Object> _outcome_dict;
    protected TIntArrayList _outcomes;

    public ParameterEstimatorMulticlass(List<SparseVector> insts,
            List<Object> outcomes) {
        _insts = insts;
        _outcome_dict = new Alphabet<Object>();
        _outcomes = new TIntArrayList(outcomes.size());
        for (int i = 0; i < outcomes.size(); i++) {
            _outcomes.add(_outcome_dict.lookupIndex(outcomes.get(i)));
        }
    }

    public double evaluateFunction(double[] x, double[] grad) {
        int num_outcomes = _outcome_dict.size();
        double val = C * DenseVector.dotSelf(x);
        double val0 = val;
        for (int i = 0; i < x.length; i++) {
            grad[i] = 2.0 * C * x[i];
        }
        double[] deriv_good=new double[x.length];
        double[] deriv_all=new double[x.length];
        double m_max = 0.0;
        double m_min = 0.0;
        double[] prod = new double[num_outcomes];
        double[] ms=new double[num_outcomes];
        for (int i = 0; i < _insts.size(); i++) {
            Arrays.fill(deriv_good,0.0);
            Arrays.fill(deriv_all,0.0);
            double mu_all=0.0;
            SparseVector ex = _insts.get(i);
            int outcome=_outcomes.get(i);
            ex.mat_mul(x, prod);
            for (int j = 0; j < prod.length; j++) {
                if (prod[j] > 35.) {
                    m_max = Math.max(m_max, prod[j]);
                    prod[j] = 35.;
                } else if (prod[j] < -35.) {
                    m_min = Math.min(m_min, prod[j]);
                    prod[j] = -35.;
                }
            }
            for (int j=0;j<num_outcomes;j++) {
                double m=Math.exp(prod[j]);
                mu_all+=m;
                ms[j]=m;
            }
            double mu_good=ms[outcome];
            ex.addTo(deriv_all,ms);
            ex.addTo1(deriv_good,ms[outcome],num_outcomes,outcome);
            val -= prod[outcome]-Math.log(mu_all);
            DenseVector.plusEquals(grad,deriv_good,-1.0/mu_good);
            DenseVector.plusEquals(grad,deriv_all,1.0/mu_all);
        }
        if (m_max > 30.) {
            System.err.format("m<=%f", m_max);
        }
        if (m_max < -30.) {
            System.err.format("m>=%f", m_min);
        }
        //TODO: print P/R values for all classes?
        System.err.format("loss=%f\n", val);
        return val;
    }

    public static void do_estimation(String prefix)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        List<SparseVector> insts =
                new ArrayList<SparseVector>();
        List<Object> labels =
                new ArrayList<Object>();
        ObjectInputStream is =
                new ObjectInputStream(new FileInputStream(prefix + ".obj"));
        while (true) {
            try {
                insts.add((SparseVector) is.readObject());
                labels.add(is.readObject());
            } catch (EOFException e) {
                break;
            }
        }
        is.close();
        is = new ObjectInputStream(new FileInputStream(prefix + ".dict"));
        Alphabet dict = (Alphabet) is.readObject();
        Alphabet outcome_dict=null;
        double[] parameters = new double[dict.size()];
        try {
            ParameterEstimatorMulticlass estimator =
                    new ParameterEstimatorMulticlass(insts, labels);
            Minimization.testGradient(parameters, estimator);
            System.err.format("ParamEstMC: %d labels, %d features\n",
                estimator._outcome_dict.size(),dict.size());
            Minimization.runLBFGS(parameters, estimator);
            outcome_dict=estimator._outcome_dict;
        } catch (ExceptionWithIflag ex) {
            ex.printStackTrace();
            throw new RuntimeException("Minimization failed", ex);
        }
        SortedMap<String, Double> paramValues = new TreeMap<String, Double>();
        for (int i = 0; i < parameters.length; i++) {
            paramValues.put((String) dict.lookupObject(i),
                    parameters[i]);
        }
        System.err.format("parameters for %s\n", prefix);
        for (String key : paramValues.keySet()) {
            System.err.format("%s: %f\n", key, paramValues.get(key));
        }
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(prefix + ".param"));
        oos.writeObject(outcome_dict);
        oos.writeObject(parameters);
        oos.close();
    }

    public static void main(String[] args) {
        System.out.println("ParameterEstimatorMulticlass");
        try {
            do_estimation("models/coref/idc0");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
