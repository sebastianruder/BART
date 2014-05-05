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

import java.io.Serializable;

/**
 *
 * @author versley
 */
public final class SparseVector implements IVector, Serializable {
    static final long serialVersionUID = 7412871925217568170L;
    final int[] _keys;
    final double[] _vals;
    public SparseVector(int[] keys, double[] vals) {
        _keys=keys; _vals=vals;
    }

    public final void addTo(double[] vec, double factor) {
        for (int i=0; i< _keys.length; i++)
        {
            vec[_keys[i]]+=_vals[i]*factor;
        }
    }

    public void addTo(double[] vec, double[] factor) {
        int dim2=factor.length;
        for (int i=0; i< _keys.length; i++)
        {
            int off=dim2*_keys[i];
            double val=_vals[i];
            for (int j=0; j<dim2; j++) {
                vec[off+j]=val*factor[j];
            }
        }
    }

    public void addTo1(double[] vec, double factor, int dim2, int j) {
        for (int i=0; i< _keys.length; i++)
        {
            int off=dim2*_keys[i];
            double val=_vals[i];
            vec[off+j]=val*factor;
        }
    }
    public final double dotProduct(double[] vec) {
        double sum=0.0;
        for (int i=0;i<_keys.length; i++)
        {
            sum+=vec[_keys[i]]*_vals[i];
        }
        return sum;
    }
    
    public final double dotProduct(SparseVector vec) {
    	/* caution: works only if vec's indices are sorted
    	 * which they should be if they come out of a FeatureVector
    	 */
    	double sum=0.0;
    	int i,j;
		int idx_i;
		int idx_j;
		if (_keys.length==0 || vec._keys.length==0) {
			return 0.0;
		}
    	i=j=0;
    	while (i<_keys.length && j<vec._keys.length) {
        	idx_i=_keys[i];
    		idx_j=vec._keys[j];
    		if (idx_i<idx_j) {
    			++i;
    		} else if (idx_j<idx_i) {
    			++j;
    		} else {
    			sum+=_vals[i]*vec._vals[j];
    			++i; ++j;
    		}
    	}
    	return sum;
    }

    /** matrix is interpreted as a matrix compatible to prod */
    public void mat_mul(double[] matrix, double[] prod) {
        int dim2=prod.length;
        for (int j=0; j<dim2;j++) {
            prod[j]=0.0;
        }
        for (int i=0;i<_keys.length;i++)
        {
            int off=_keys[i]*dim2;
            double val=_vals[i];
            for (int j=0;j<dim2;j++) {
                prod[j]+=matrix[off+j]*val;
            }
        }
    }

    public final void put(KVFunc func)
    {
        for (int i=0;i<_keys.length; i++)
        {
            func.put(_keys[i],_vals[i]);
        }        
    }

	public int size() {
		return _keys.length;
	}
}
