/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

package elkfed.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.DirectSearchOptimizer;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.random.JDKRandomGenerator;

import elkfed.coref.eval.MUCScorer;
import elkfed.coref.eval.Score;
import elkfed.main.xml.CorefExperimentDocument;
import elkfed.main.xml.TuningParameter;
import elkfed.main.xml.TuningParameters;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author yannick.versley
 */
public class AutoTune {
    public static final int N_SAMPLES=200;
    public static class Evaluator implements MultivariateRealFunction {
        CorefExperimentDocument _doc;
        JDKRandomGenerator random=new JDKRandomGenerator();
        double[] bestParam;
        double bestVal=0.0;

        public Evaluator(CorefExperimentDocument doc) {
            _doc=doc;
        }

        public double value(double[] values) {
            List<Score> score;
            System.out.println("values: len="+values.length);
            setParameters(_doc.getCorefExperiment().getSystem().getTuningParameters(),
                    values);
            try {
                score=XMLCrossValidate.run(_doc, MUCScorer.getInstance());
//                double val = Math.exp(-Math.pow((Math.ceil(values[0])+values[1]-10),2)-
//                        Math.pow(values[2]-3, 2));
//                score=new ArrayList<Score>();
//                score.add(new PRScore(val,val,"TOTAL"));
//                if (false) throw new IOException();
//                if (false) throw new ClassNotFoundException();
                    System.out.format("score=%f best=%f\n",
                            score.get(score.size()-1).getScore(),
                            bestVal);
                if (score.get(score.size()-1).getScore()>bestVal) {
                    System.out.println("Save new best value");
                    bestVal=score.get(score.size()-1).getScore();
                    bestParam=new double[values.length];
                    for (int i=0; i<values.length;i++) {
                        bestParam[i]=values[i];
                    }
                }
                System.out.print("values examined:");
		for (double d: values) {
		    System.out.print(" "+d);
		}
		System.out.println();
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            return 1.0-score.get(score.size()-1).getScore();
        }

        private void setParameters(TuningParameters tuningParameters, double[] values) {
            int offset=0;
            for (TuningParameter param: tuningParameters.getParameterArray()) {
                if (param.getType().equals("Float")) {
                    param.setValue(Double.toString(values[offset]));
                    offset++;
                } else if (param.getType().equals("Integer")) {
                    param.setValue(Integer.toString((int)values[offset]));
                    offset++;
                }
            }
        }

        public int getNumParameters() {
            int result=0;
            TuningParameters tuningParameters=
                    _doc.getCorefExperiment().getSystem().getTuningParameters();
            for (TuningParameter param: tuningParameters.getParameterArray()) {
                if (param.getType().equals("Float")) {
                    result++;
                } else if (param.getType().equals("Integer")) {
                    result++;
                }
            }
            return result;
        }

        public double[] sample() {
            TDoubleArrayList result=new TDoubleArrayList();
            TuningParameters tuningParameters=
                    _doc.getCorefExperiment().getSystem().getTuningParameters();
            for (TuningParameter param: tuningParameters.getParameterArray()) {
                if (param.getType().equals("Float")) {
                    result.add(-0.5+random.nextGaussian()*3.0);
                } else if (param.getType().equals("Integer")) {
                    result.add(7+random.nextInt(10));
                }
            }
            return result.toArray();
        }

        public void saveBest(OutputStream os) throws IOException
        {
            setParameters(_doc.getCorefExperiment().getSystem().getTuningParameters(),
                    bestParam);
            _doc.save(os);
        }
    }

    public static class StupidConvergenceChecker implements RealConvergenceChecker{

        public boolean converged(int iteration,
        		RealPointValuePair previous,
        		RealPointValuePair current) {
            double minCost=previous.getValue();
            double maxCost=current.getValue();
	    System.err.println("converged() called, minCost="+minCost+
			       " maxCost="+maxCost);
            return (maxCost-minCost)<0.001;
        }
        
    }

    public static void main(String[] args) {
        try {
            CorefExperimentDocument doc;
            doc=CorefExperimentDocument.Factory.parse(
                            new FileInputStream(args[0]));
            Evaluator eval=new Evaluator(doc);
            double[][] samples=new double[eval.getNumParameters()+1][];
            for (int i=0;i<eval.getNumParameters()+1;i++) {
                samples[i]=eval.sample();
            }
            DirectSearchOptimizer optimizer=new NelderMead();
            try {
            	optimizer.setStartConfiguration(samples);
            	optimizer.setConvergenceChecker(new StupidConvergenceChecker());
            	optimizer.optimize(eval,GoalType.MINIMIZE, samples[0]);
            } catch (ConvergenceException ex) {
                ex.printStackTrace();
            }
            System.out.println("*** Best values:");
            eval.saveBest(System.out);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
