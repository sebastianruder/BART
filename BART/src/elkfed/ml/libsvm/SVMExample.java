package elkfed.ml.libsvm;

import java.io.DataOutputStream;
import java.io.IOException;

import elkfed.ml.util.KVFunc;
import elkfed.ml.util.SparseVector;

public class SVMExample {
	private final double[] factors;
	private final SparseVector[] vectors;
	public SVMExample(double[] fs, SparseVector[] vecs) {
		factors=fs;
		vectors=vecs;
	}
	
	public final double dot(SVMExample other) {
		double sum=0.0;
		for (int i=0; i<vectors.length; i++) {
			for (int j=0; j<other.vectors.length; j++) {
				sum+=factors[i]*factors[j]*vectors[i].dotProduct(other.vectors[j]);
			}
		}
		System.err.println(sum);
		return sum;
	}

	interface WriterKVFunc extends KVFunc {
		void maybe_throw() throws IOException;
	}
	
	public final void writeln(final DataOutputStream f) throws IOException {
		if (vectors.length>1) {
			throw new RuntimeException("Cannot write this!");
		}
		final double fac=factors[0];
		WriterKVFunc writer=new WriterKVFunc() {
			IOException last_exc=null;
			public void put(int key, double value) {
				try {
					f.writeBytes(key+":"+fac*value+" ");
				} catch (IOException ex) {
					last_exc=ex;
				}
			}
			public void maybe_throw() throws IOException {
				if (last_exc!=null) {
					throw last_exc;
				}
			}
		};
		vectors[0].put(writer);
		writer.maybe_throw();
		f.writeBytes("\n");
	}
	
	public final boolean is_positive() {
		assert factors.length==1;
		return factors[0]>0;
	}
	
	public static SVMExample make_binary_example(SparseVector vec, boolean is_positive) {
		double[] fs=new double[1];
		SparseVector[] vecs={vec};
		if (is_positive) {
			fs[0]=+1.0;
		} else {
			fs[0]=-1.0;
		}
		return new SVMExample(fs, vecs);
	}
}
