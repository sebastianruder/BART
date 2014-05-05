//
// svm_model
//
package elkfed.ml.libsvm;

public class svm_model implements java.io.Serializable
{
	public svm_parameter param;	// parameter
	public int l;			// total #SV
	public SVMExample[] SV;	// SVs (SV[l])
	public double[] sv_coef;	// coefficients for SVs in decision functions (sv_coef[k-1][l])
	public double rho;		// constants in decision functions (rho[k*(k-1)/2])

	public double get_prediction(SVMExample x)
	{
		double sum = 0;
		for(int i=0;i<l;i++)
			sum += sv_coef[i] * Kernel.k_function(x,SV[i],param);
		sum -= rho;
		return sum;
	}
	
	public double svm_predict_value(SVMExample x)
	{
		double sum=get_prediction(x);
		return (sum>0?1:-1);
	}
};
