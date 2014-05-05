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
package elkfed.ml.util;

import java.util.Arrays;
import riso.numerical.LBFGS;
import riso.numerical.LBFGS.ExceptionWithIflag;
/**
 * Wrapper around LBFGS to add some sanity.
 * The <i>testGradient</i> function was inspired by Mallet's
 * <i>TestMaximizable</i>.
 * @author versley
 */
public class Minimization {
    public static class LBFGSResult {
        public int iflag;
        public int n_iter;
        public double[] x;
        public double[] gradient;
    }
    public static LBFGSResult runLBFGS(double[] x0, Minimizable func,
            int max_iter, int m, double eps, double xtol)
            throws LBFGS.ExceptionWithIflag
    {
        final int n=x0.length;
        int[] iprint=new int[] {0,0};
        int[] iflag=new int[] {0};
        double g[]=new double[n];
        double[] diag=new double[n];
        boolean diagco=false;
        int n_iter=0;
        while (n_iter<max_iter)
        {
            double f=func.evaluateFunction(x0, g);
            LBFGS.lbfgs(n, m, x0, f, g, diagco, diag, iprint, eps, xtol, iflag);
            if (iflag[0]<=0) break;
            n_iter++;
        }
        LBFGSResult result=new LBFGSResult();
        result.iflag=iflag[0];
        result.n_iter=n_iter;
        result.x=x0;
        result.gradient=g;
        return result;
    }
    
    public static LBFGSResult runLBFGS(double[] x0, Minimizable func)
            throws ExceptionWithIflag
    {
        return runLBFGS(x0,func,100,5,1e-5,1e-16);
    }
    
    public static class Rosenbrock implements Minimizable
    {

        public double evaluateFunction(double[] x, double[] gradient) {
            double f=0.0;
            for (int j=0; j<x.length; j+=2)
            {
                double t1=1.0e0-x[j];
                double t2=1.0e1*(x[j+1]-x[j]*x[j]);
                gradient[j+1]=2.0e1*t2;
                gradient[j]=-2.0e0*(x[j]*gradient[j+1]+t1);
                f=f+t1*t1+t2*t2;
            }
            return f ;
        }   
    }
    
    public static void testGradient(double[] x0, Minimizable func)
    {
        double factor=1.0;
        final int n=x0.length;
        double[] derivA=new double[n];
        double[] derivE=new double[n];
        double[] derivT=new double[n];
        double val=func.evaluateFunction(x0,derivA);
        double norm=0.1;
        for (int i=0; i<n; i++)
        {
            norm=Math.max(norm,derivA[i]);
        }
        double eps=0.1*factor/norm;
        System.err.format("eps=%f\n",eps);
        double tol=5.0*eps;
        //Java 1.5 doesn't have Arrays.copyOf :-(
        //double[] x2=Arrays.copyOf(x0,n);
        double[] x2=new double[n];
        for (int i=0;i<n;i++) {
            x2[i]=x0[i];
        }
        for (int i=0;i<n;i++)
        {
            x2[i]=x0[i]+eps;
            double val2=func.evaluateFunction(x2,derivT);
            double slope=(val2-val)/eps;
            derivE[i]=slope;
            x2[i]=x0[i];
        }
        double normA=Math.sqrt(DenseVector.dotSelf(derivA));
        if (normA==0.0)
        {
            System.err.println("normA is null!");
            return;
        }
        for (int i=0; i<n; i++)
        {
            derivA[i] /= normA;
        }
        double normE=Math.sqrt(DenseVector.dotSelf(derivE));
        if (normE==0.0)
        {
            System.err.println("normE is null!");
            return;
        }
        for (int i=0; i<n; i++)
        {
            derivE[i] /= normE;
        }
        double rho=DenseVector.dot(derivA,derivE);
        double angle=Math.acos(rho);
        System.err.format("normA=%s normE=%s\n",normA,normE);
        System.err.format("rho=%s angle=%s\n",rho,angle);
        int at=-1;
        double maxdiff=0.0;
        for (int i=0; i<n; i++)
        {
            double diff=Math.abs(derivE[i]-derivA[i]);
            if (diff>maxdiff)
            {
                at=i; maxdiff=diff;
            }
        }
        System.err.format("largest difference: %f at %d",maxdiff,at);
    }
    
    /* test code */
    public static void main(String[] args)
    {
        final int n=100;
        double[] x=new double[n];
        Arrays.fill(x, 0.0);
        for (int j=0; j<n; j+=2)
        {
            x[j]=-1.2e0;
            x[j+1]=1.0e0;
        }
        try {
            runLBFGS(x,new Rosenbrock());
        } catch (ExceptionWithIflag e) {
            e.printStackTrace();
        }
    }
}
