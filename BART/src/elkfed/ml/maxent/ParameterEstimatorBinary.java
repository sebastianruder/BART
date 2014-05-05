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
import elkfed.ml.util.DenseVector;
import elkfed.ml.util.SparseVector;
import elkfed.ml.util.Minimizable;
import elkfed.ml.util.Minimization;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import riso.numerical.LBFGS.ExceptionWithIflag;

/**
 * estimates the parameters for a binary MaxEnt classifier
 * @author yannick
 */
public class ParameterEstimatorBinary implements Minimizable {
    double C; // =1.0;
    protected List<SparseVector> _insts;
    protected List<Boolean> _outcomes;
    
     public ParameterEstimatorBinary(List<SparseVector> insts,
                                     List<Boolean> outcomes)
     {
        _insts=insts;
        _outcomes=outcomes;
        C=1.0;
    }
     public ParameterEstimatorBinary(List<SparseVector> insts,
                                     List<Boolean> outcomes, double cc)
     {
        _insts=insts;
        _outcomes=outcomes;
        C=cc;
    }
    
    public double evaluateFunction(double[] x, double[] grad)
    {
        double val=C*DenseVector.dotSelf(x);
        double val0=val;
        for (int i=0; i<x.length; i++) {
            grad[i]=2.0*C*x[i];
        }
        double m_max=0.0;
        double m_min=0.0;
        int tp=0;
        int fp=0;
        int fn=0;
        for (int i=0; i<_insts.size(); i++) {
            SparseVector ex=_insts.get(i);
            double prod=ex.dotProduct(x);
            if (prod>35.) {
                m_max=Math.max(m_max,prod);
                prod=35.;
            } else if (prod<-35.) {
                m_min=Math.min(m_min,prod);
                prod=-35.;
            }
            double factor;
            if (_outcomes.get(i)) {
                factor=-1.0;
            } else {
                factor=1.0;
            }
            double m=Math.exp(prod*factor);
            if (m<1.0) {
                if (_outcomes.get(i)) tp++;
            } else {
                if (_outcomes.get(i)) fn++;
                else fp++;
            }
            val += Math.log(1.0+m);
            ex.addTo(grad,m/(1.0+m)*factor);
        }
        if (m_max>30.) {
            System.err.format("m<=%f",m_max);
        }
        if (m_max<-30.) {
            System.err.format("m>=%f",m_min);
        }
        System.err.format("loss=%f, Prec=%f Recl=%f\n",val,
                (double)tp/Math.max(tp+fp,1.0),
                (double)tp/((double)tp+fn));
        return val;
    }
    
    public static void do_estimation(String prefix, String cparam)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
         double Cpar=1.0;
         if (cparam!=null && !cparam.equals(""))
            Cpar=Double.parseDouble(cparam);
         

            List<SparseVector> insts=
                    new ArrayList<SparseVector>();
            List<Boolean> labels=
                    new ArrayList<Boolean>();
            ObjectInputStream is=
                    new ObjectInputStream(new FileInputStream(prefix+".obj"));
            while (true) {
                try {
                    insts.add((SparseVector)is.readObject());
                    labels.add((Boolean)is.readObject());
                } catch (EOFException e) {
                    break;
                }
            }
            is.close();
            is=new ObjectInputStream(new FileInputStream(prefix+".dict"));
            Alphabet dict=(Alphabet)is.readObject();
            double[] parameters=new double[dict.size()];
            if (insts.size()>0) {
                try {
                    ParameterEstimatorBinary estimator=
                            new ParameterEstimatorBinary(insts,labels,Cpar);
                    //Minimization.testGradient(parameters, estimator);
                    Minimization.runLBFGS(parameters, estimator);
                } catch (ExceptionWithIflag ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Minimization failed",ex);
                }
            }
            final Map<String,Double> paramValues=new HashMap<String,Double>();
            for (int i=0; i<parameters.length; i++)
            {
                paramValues.put((String)dict.lookupObject(i),
                        parameters[i]);
            }
            System.err.format("highest-weight parameters for %s\n",prefix);
            ArrayList<String> keys=new ArrayList<String>(paramValues.keySet());
            Collections.sort(keys, new Comparator<String>() {

            public int compare(String key1, String key2) {
                double delta=Math.abs(paramValues.get(key1))-
                        Math.abs(paramValues.get(key2));
                if (delta<0.0) {
                    return -1;
                } else if (delta>0.0) {
                    return +1;
                } else {
                    return 0;
                }
            }
                
            });
            for (String key : keys.subList(keys.size() < 200 ?
                                            0 : keys.size() - 200,
                                            keys.size())) {
                System.err.format("%s: %f\n", key, paramValues.get(key));
            }
            ObjectOutputStream oos=new ObjectOutputStream(
                    new FileOutputStream(prefix+".param"));
            oos.writeObject(parameters);
            oos.close();
    }
    
    public static void main(String[] args) {
        System.out.println("ParameterEstimatorBinary");
        try {
            do_estimation("models/coref/idc0","");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}