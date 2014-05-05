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

/**
 * general utility function for doing linear algebra with
 * arrays of doubles
 * 
 * @author versley
 */
public class DenseVector implements IVector {

    static double dot(double[] x1, double[] x2) {
        double result=0.0;
        final int n=Math.min(x1.length, x2.length);
        for (int i=0; i<n; i++)
        {
            result+=x1[i]*x2[i];
        }
        return result;
    }
    private final double[] _vals;
    
    DenseVector(double[] vals)
    {
        _vals=vals;
    }

    public final void clear()
    {
        Arrays.fill(_vals,0.0);
    }
    
    public final void addTo(double[] vec, double factor) {
        final int n=vec.length<_vals.length?vec.length:_vals.length;
        for (int i=0; i<n; i++)
        {
            vec[i]+=_vals[i]*factor;
        }
    }

    public static final void plusEquals(double[] y, double[] x, double a)
    {
        final int n=x.length<y.length?x.length:y.length;
        for (int i=0; i<n; i++)
        {
            y[i]+=x[i]*a;
        }
    }
    
    public static final double dotSelf(double[] vec) {
        double result=0.0;
        for (double d: vec) {
            result+=d*d;
        }
        return result;
    }
    
    public final double dotProduct(double[] vec) {
        final int n=vec.length<_vals.length?vec.length:_vals.length;
        double sum=0.0;
        for (int i=0; i<n; i++)
        {
            sum+=vec[i]*_vals[i];
        }
        return sum;
    }

    public void put(KVFunc func) {
        for (int i=0; i<_vals.length; i++)
        {
            func.put(i,_vals[i]);
        }
    }
}
