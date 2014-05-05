package elkfed.ml.libsvm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import elkfed.ml.util.SparseVector;

/** this is a stripped-down version
 * of libsvm's SVM solver - basically,
 * everything that is not used in the
 * binary C-SVC problem
 * has been removed
 */


//
// Kernel Cache
//
// l is the number of total data items
// size is the cache size limit in bytes
//
class Cache {
	private final int l;
	private long size;
	private final class head_t
	{
		head_t prev, next;	// a cicular list
		float[] data;
		int len;		// data[0,len) is cached in this entry
	}
	private final head_t[] head;
	private head_t lru_head;

	Cache(int l_, long size_)
	{
		l = l_;
		size = size_;
		head = new head_t[l];
		for(int i=0;i<l;i++) head[i] = new head_t();
		size /= 4;
		size -= l * (16/4);	// sizeof(head_t) == 16
		size = Math.max(size, 2* (long) l);  // cache must be large enough for two columns
		lru_head = new head_t();
		lru_head.next = lru_head.prev = lru_head;
	}

	private void lru_delete(head_t h)
	{
		// delete from current location
		h.prev.next = h.next;
		h.next.prev = h.prev;
	}

	private void lru_insert(head_t h)
	{
		// insert to last position
		h.next = lru_head;
		h.prev = lru_head.prev;
		h.prev.next = h;
		h.next.prev = h;
	}

	// request data [0,len)
	// return some position p where [p,len) need to be filled
	// (p >= len if nothing needs to be filled)
	// java: simulate pointer using single-element array
	int get_data(int index, float[][] data, int len)
	{
		head_t h = head[index];
		if(h.len > 0) lru_delete(h);
		int more = len - h.len;

		if(more > 0)
		{
			// free old space
			while(size < more)
			{
				head_t old = lru_head.next;
				lru_delete(old);
				size += old.len;
				old.data = null;
				old.len = 0;
			}

			// allocate new space
			float[] new_data = new float[len];
			if(h.data != null) System.arraycopy(h.data,0,new_data,0,h.len);
			h.data = new_data;
			size -= more;
			do {int _=h.len; h.len=len; len=_;} while(false);
		}

		lru_insert(h);
		data[0] = h.data;
		return len;
	}

	void swap_index(int i, int j)
	{
		if(i==j) return;
		
		if(head[i].len > 0) lru_delete(head[i]);
		if(head[j].len > 0) lru_delete(head[j]);
		do {float[] _=head[i].data; head[i].data=head[j].data; head[j].data=_;} while(false);
		do {int _=head[i].len; head[i].len=head[j].len; head[j].len=_;} while(false);
		if(head[i].len > 0) lru_insert(head[i]);
		if(head[j].len > 0) lru_insert(head[j]);

		if(i>j) do {int _=i; i=j; j=_;} while(false);
		for(head_t h = lru_head.next; h!=lru_head; h=h.next)
		{
			if(h.len > i)
			{
				if(h.len > j)
					do {float _=h.data[i]; h.data[i]=h.data[j]; h.data[j]=_;} while(false);
				else
				{
					// give up
					lru_delete(h);
					size += h.len;
					h.data = null;
					h.len = 0;
				}
			}
		}
	}
}

//
// Kernel evaluation
//
// the static method k_function is for doing single kernel evaluation
// the constructor of Kernel prepares to calculate the l*l kernel matrix
// the member function get_Q is for getting one column from the Q Matrix
//
abstract class QMatrix {
	abstract float[] get_Q(int column, int len);
	abstract double[] get_QD();
	abstract void swap_index(int i, int j);
};

abstract class Kernel extends QMatrix {
	private SVMExample[] x;
	private final double[] x_square;

	// svm_parameter
	private final int kernel_type;
	private final int degree;
	private final double gamma;
	private final double coef0;

	abstract float[] get_Q(int column, int len);
	abstract double[] get_QD();

	void swap_index(int i, int j)
	{
		do {SVMExample _=x[i]; x[i]=x[j]; x[j]=_;} while(false);
		if(x_square != null) do {double _=x_square[i]; x_square[i]=x_square[j]; x_square[j]=_;} while(false);
	}

	private static double powi(double base, int times)
	{
		double tmp = base, ret = 1.0;

		for(int t=times; t>0; t/=2)
		{
			if(t%2==1) ret*=tmp;
			tmp = tmp * tmp;
		}
		return ret;
	}

	double kernel_function(int i, int j)
	{
		switch(kernel_type)
		{
			case svm_parameter.LINEAR:
				return x[i].dot(x[j]);
			case svm_parameter.POLY:
				return powi(gamma*x[i].dot(x[j])+coef0,degree);
			case svm_parameter.RBF:
				return Math.exp(-gamma*(x_square[i]+x_square[j]-2*x[i].dot(x[j])));
			case svm_parameter.SIGMOID:
				return Math.tanh(gamma*x[i].dot(x[j])+coef0);
			default:
				throw new RuntimeException("Invalid kernel specified!");
		}
	}

	Kernel(int l, SVMExample[] x_, svm_parameter param)
	{
		this.kernel_type = param.kernel_type;
		this.degree = param.degree;
		this.gamma = param.gamma;
		this.coef0 = param.coef0;

		x = (SVMExample[])x_.clone();

		if(kernel_type == svm_parameter.RBF)
		{
			x_square = new double[l];
			for(int i=0;i<l;i++)
				x_square[i] = x[i].dot(x[i]);
		}
		else x_square = null;
	}

	static double k_function(SVMExample x, SVMExample y,
					svm_parameter param)
	{
		switch(param.kernel_type)
		{
			case svm_parameter.LINEAR:
				return x.dot(y);
			case svm_parameter.POLY:
				return powi(param.gamma*x.dot(y)+param.coef0,param.degree);
			case svm_parameter.RBF:
			{
				double sum = 0;
				sum=x.dot(x)+y.dot(y)-2*x.dot(y);
				return Math.exp(-param.gamma*sum);
			}
			case svm_parameter.SIGMOID:
				return Math.tanh(param.gamma*x.dot(y)+param.coef0);
			default:
				return 0;	// java
		}
	}
}

// An SMO algorithm in Fan et al., JMLR 6(2005), p. 1889--1918
// Solves:
//
//	min 0.5(\alpha^T Q \alpha) + p^T \alpha
//
//		y^T \alpha = \delta
//		y_i = +1 or -1
//		0 <= alpha_i <= Cp for y_i = 1
//		0 <= alpha_i <= Cn for y_i = -1
//
// Given:
//
//	Q, p, y, Cp, Cn, and an initial feasible point \alpha
//	l is the size of vectors and matrices
//	eps is the stopping tolerance
//
// solution will be put in \alpha, objective value will be put in obj
//
class Solver {
	int active_size;
	//byte[] y;
	double[] G;		// gradient of objective function
	static final byte LOWER_BOUND = 0;
	static final byte UPPER_BOUND = 1;
	static final byte FREE = 2;
	byte[] alpha_status;	// LOWER_BOUND, UPPER_BOUND, FREE
	double[] alpha;
	QMatrix Q;
	double[] QD;
	double eps;
	double C;
	double[] p;
	int[] active_set;
	double[] G_bar;		// gradient, if we treat free variables as 0
	int l;
	boolean unshrink;	// XXX
	
	static final double INF = java.lang.Double.POSITIVE_INFINITY;
	
	void update_alpha_status(int i)
	{
		if(alpha[i] >= C)
			alpha_status[i] = UPPER_BOUND;
		else if(alpha[i] <= 0)
			alpha_status[i] = LOWER_BOUND;
		else alpha_status[i] = FREE;
	}
	boolean is_upper_bound(int i) { return alpha_status[i] == UPPER_BOUND; }
	boolean is_lower_bound(int i) { return alpha_status[i] == LOWER_BOUND; }
	boolean is_free(int i) {  return alpha_status[i] == FREE; }

	// java: information about solution except alpha,
	// because we cannot return multiple values otherwise...
	static class SolutionInfo {
		double obj;
		double rho;
		double upper_bound;
		double r;	// for Solver_NU
	}

	void swap_index(int i, int j)
	{
		Q.swap_index(i,j);
		//do {byte _=y[i]; y[i]=y[j]; y[j]=_;} while(false);
		do {double _=G[i]; G[i]=G[j]; G[j]=_;} while(false);
		do {byte _=alpha_status[i]; alpha_status[i]=alpha_status[j]; alpha_status[j]=_;} while(false);
		do {double _=alpha[i]; alpha[i]=alpha[j]; alpha[j]=_;} while(false);
		do {double _=p[i]; p[i]=p[j]; p[j]=_;} while(false);
		do {int _=active_set[i]; active_set[i]=active_set[j]; active_set[j]=_;} while(false);
		do {double _=G_bar[i]; G_bar[i]=G_bar[j]; G_bar[j]=_;} while(false);
	}

	void reconstruct_gradient()
	{
		// reconstruct inactive elements of G from G_bar and free variables

		if(active_size == l) return;

		int i,j;
		int nr_free = 0;

		for(j=active_size;j<l;j++)
			G[j] = G_bar[j] + p[j];

		for(j=0;j<active_size;j++)
			if(is_free(j))
				nr_free++;

		if(2*nr_free < active_size)
			svm.info("\nWarning: using -h 0 may be faster\n");

		if (nr_free*l > 2*active_size*(l-active_size))
		{
			for(i=active_size;i<l;i++)
			{
				float[] Q_i = Q.get_Q(i,active_size);
				for(j=0;j<active_size;j++)
					if(is_free(j))
						G[i] += alpha[j] * Q_i[j];
			}	
		}
		else
		{
			for(i=0;i<active_size;i++)
				if(is_free(i))
				{
					float[] Q_i = Q.get_Q(i,l);
					double alpha_i = alpha[i];
					for(j=active_size;j<l;j++)
						G[j] += alpha_i * Q_i[j];
				}
		}
	}

	void Solve(int l, QMatrix Q, double[] p_,
		   double[] alpha_, double C, double eps, SolutionInfo si, int shrinking)
	{
		this.l = l;
		this.Q = Q;
		QD = Q.get_QD();
		p = (double[])p_.clone();
		alpha = (double[])alpha_.clone();
		this.C = C;
		this.eps = eps;
		this.unshrink = false;

		// initialize alpha_status
		{
			alpha_status = new byte[l];
			for(int i=0;i<l;i++)
				update_alpha_status(i);
		}

		// initialize active set (for shrinking)
		{
			active_set = new int[l];
			for(int i=0;i<l;i++)
				active_set[i] = i;
			active_size = l;
		}

		// initialize gradient
		{
			G = new double[l];
			G_bar = new double[l];
			int i;
			for(i=0;i<l;i++)
			{
				G[i] = p[i];
				G_bar[i] = 0;
			}
			for(i=0;i<l;i++)
				if(!is_lower_bound(i))
				{
					float[] Q_i = Q.get_Q(i,l);
					double alpha_i = alpha[i];
					int j;
					for(j=0;j<l;j++)
						G[j] += alpha_i*Q_i[j];
					if(is_upper_bound(i))
						for(j=0;j<l;j++)
							G_bar[j] += C * Q_i[j];
				}
		}

		// optimization step

		int iter = 0;
		int counter = Math.min(l,1000)+1;
		int[] working_set = new int[2];

		while(true)
		{
			// show progress and do shrinking

			if(--counter == 0)
			{
				counter = Math.min(l,1000);
				if(shrinking!=0) do_shrinking();
				svm.info(".");
			}

			if(select_working_set(working_set)!=0)
			{
				// reconstruct the whole gradient
				reconstruct_gradient();
				// reset active set size and check
				active_size = l;
				svm.info("*");
				if(select_working_set(working_set)!=0)
					break;
				else
					counter = 1;	// do shrinking next iteration
			}
			
			int i = working_set[0];
			int j = working_set[1];

			++iter;

			// update alpha[i] and alpha[j], handle bounds carefully

			float[] Q_i = Q.get_Q(i,active_size);
			float[] Q_j = Q.get_Q(j,active_size);

			double old_alpha_i = alpha[i];
			double old_alpha_j = alpha[j];

			double quad_coef = QD[i]+QD[j]-2*Q_i[j];
			if (quad_coef <= 0)
				quad_coef = 1e-12;
			double delta = (G[i]-G[j])/quad_coef;
			double sum = alpha[i] + alpha[j];
			alpha[i] -= delta;
			alpha[j] += delta;

			if(sum > C)
			{
				if(alpha[i] > C)
				{
					alpha[i] = C;
					alpha[j] = sum - C;
				}
			}
			else
			{
				if(alpha[j] < 0)
				{
					alpha[j] = 0;
					alpha[i] = sum;
				}
			}
			if(sum > C)
			{
				if(alpha[j] > C)
				{
					alpha[j] = C;
					alpha[i] = sum - C;
				}
			}
			else
			{
				if(alpha[i] < 0)
				{
					alpha[i] = 0;
					alpha[j] = sum;
				}
			}

			// update G

			double delta_alpha_i = alpha[i] - old_alpha_i;
			double delta_alpha_j = alpha[j] - old_alpha_j;

			for(int k=0;k<active_size;k++)
			{
				G[k] += Q_i[k]*delta_alpha_i + Q_j[k]*delta_alpha_j;
			}

			// update alpha_status and G_bar

			{
				boolean ui = is_upper_bound(i);
				boolean uj = is_upper_bound(j);
				update_alpha_status(i);
				update_alpha_status(j);
				int k;
				if(ui != is_upper_bound(i))
				{
					Q_i = Q.get_Q(i,l);
					if(ui)
						for(k=0;k<l;k++)
							G_bar[k] -= C * Q_i[k];
					else
						for(k=0;k<l;k++)
							G_bar[k] += C * Q_i[k];
				}

				if(uj != is_upper_bound(j))
				{
					Q_j = Q.get_Q(j,l);
					if(uj)
						for(k=0;k<l;k++)
							G_bar[k] -= C * Q_j[k];
					else
						for(k=0;k<l;k++)
							G_bar[k] += C * Q_j[k];
				}
			}

		}

		// calculate rho

		si.rho = calculate_rho();

		// calculate objective value
		{
			double v = 0;
			int i;
			for(i=0;i<l;i++)
				v += alpha[i] * (G[i] + p[i]);

			si.obj = v/2;
		}

		// put back the solution
		{
			for(int i=0;i<l;i++)
				alpha_[active_set[i]] = alpha[i];
		}

		si.upper_bound = C;

		svm.info("\noptimization finished, #iter = "+iter+"\n");
	}

	// return 1 if already optimal, return 0 otherwise
	int select_working_set(int[] working_set)
	{
		// return i,j such that
		// i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
		// j: mimimizes the decrease of obj value
		//    (if quadratic coefficeint <= 0, replace it with tau)
		//    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)
		
		double Gmax = -INF;
		double Gmax2 = -INF;
		int Gmax_idx = -1;
		int Gmin_idx = -1;
		double obj_diff_min = INF;
	
		for(int t=0;t<active_size;t++)
			if(!is_upper_bound(t))
				if(-G[t] >= Gmax)
				{
					Gmax = -G[t];
					Gmax_idx = t;
				}

		int i = Gmax_idx;
		float[] Q_i = null;
		if(i != -1) // null Q_i not accessed: Gmax=-INF if i=-1
			Q_i = Q.get_Q(i,active_size);
	
		for(int j=0;j<active_size;j++)
		{
			if (!is_lower_bound(j))
			{
				double grad_diff=Gmax+G[j];
				if (G[j] >= Gmax2)
					Gmax2 = G[j];
				if (grad_diff > 0)
				{
					double obj_diff; 
					double quad_coef = QD[i]+QD[j]-2.0*Q_i[j];
					if (quad_coef > 0)
						obj_diff = -(grad_diff*grad_diff)/quad_coef;
					else
						obj_diff = -(grad_diff*grad_diff)/1e-12;

					if (obj_diff <= obj_diff_min)
					{
						Gmin_idx=j;
						obj_diff_min = obj_diff;
					}
				}
			}
		}

		if(Gmax+Gmax2 < eps)
			return 1;

		working_set[0] = Gmax_idx;
		working_set[1] = Gmin_idx;
		return 0;
	}

	private boolean be_shrunk(int i, double Gmax1, double Gmax2)
	{	
		if(is_upper_bound(i))
		{
			return(-G[i] > Gmax1);
		}
		else if(is_lower_bound(i))
		{
			return(G[i] > Gmax2);
		}
		else
			return(false);
	}

	void do_shrinking()
	{
		int i;
		double Gmax1 = -INF;		// max { -y_i * grad(f)_i | i in I_up(\alpha) }
		double Gmax2 = -INF;		// max { y_i * grad(f)_i | i in I_low(\alpha) }

		// find maximal violating pair first
		for(i=0;i<active_size;i++)
		{
			if(!is_upper_bound(i))	
			{
				if(-G[i] >= Gmax1)
					Gmax1 = -G[i];
			}
			if(!is_lower_bound(i))
			{
				if(G[i] >= Gmax2)
					Gmax2 = G[i];
			}
		}

		if(unshrink == false && Gmax1 + Gmax2 <= eps*10) 
		{
			unshrink = true;
			reconstruct_gradient();
			active_size = l;
		}

		for(i=0;i<active_size;i++)
			if (be_shrunk(i, Gmax1, Gmax2))
			{
				active_size--;
				while (active_size > i)
				{
					if (!be_shrunk(active_size, Gmax1, Gmax2))
					{
						swap_index(i,active_size);
						break;
					}
					active_size--;
				}
			}
	}

	double calculate_rho()
	{
		double r;
		int nr_free = 0;
		double ub = INF, lb = -INF, sum_free = 0;
		for(int i=0;i<active_size;i++)
		{
			double yG = G[i];

			if(is_lower_bound(i))
			{
				ub = Math.min(ub,yG);
			}
			else if(is_upper_bound(i))
			{
				lb = Math.max(lb,yG);
			}
			else
			{
				++nr_free;
				sum_free += yG;
			}
		}

		if(nr_free>0)
			r = sum_free/nr_free;
		else
			r = (ub+lb)/2;

		return r;
	}

}


//
// Q matrices for various formulations
//
class SVC_Q extends Kernel
{
	private final Cache cache;
	private final double[] QD;

	SVC_Q(List<SVMExample> prob, svm_parameter param)
	{
		super(prob.size(), prob.toArray(new SVMExample[prob.size()]), param);
		cache = new Cache(prob.size(),(long)(param.cache_size*(1<<20)));
		QD = new double[prob.size()];
		for(int i=0;i<prob.size();i++)
			QD[i] = kernel_function(i,i);
	}

	float[] get_Q(int i, int len)
	{
		float[][] data = new float[1][];
		int start, j;
		if((start = cache.get_data(i,data,len)) < len)
		{
			for(j=start;j<len;j++)
				data[0][j] = (float)(kernel_function(i,j));
		}
		return data[0];
	}

	double[] get_QD()
	{
		return QD;
	}

	void swap_index(int i, int j)
	{
		cache.swap_index(i,j);
		super.swap_index(i,j);
		//do {byte _=y[i]; y[i]=y[j]; y[j]=_;} while(false);
		do {double _=QD[i]; QD[i]=QD[j]; QD[j]=_;} while(false);
	}
}



public class svm {
	//
	// construct and solve various formulations
	//
	public static final int LIBSVM_VERSION=310; 
	public static final Random rand = new Random();

	static void info(String s) 
	{
		System.out.println(s);
	}

	private static void solve_c_svc(List<SVMExample> prob, svm_parameter param,
					double[] alpha, Solver.SolutionInfo si,
					double C)
	{
		int l = prob.size();
		double[] minus_ones = new double[l];

		int i;

		System.err.format("solve_c_svc: %d examples\n",prob.size());
		for(i=0;i<l;i++)
		{
			alpha[i] = 0;
			minus_ones[i] = -1;
		}

		Solver s = new Solver();
		s.Solve(l, new SVC_Q(prob,param), minus_ones,
			alpha, C, param.eps, si, param.shrinking);

		double sum_alpha=0;
		for(i=0;i<l;i++)
			sum_alpha += alpha[i];

		svm.info("nu = "+sum_alpha/(C*prob.size())+"\n");
	}

	//
	// decision_function
	//
	static class decision_function
	{
		double[] alpha;
		double rho;	
	};

	static decision_function svm_train_one(
		List<SVMExample> prob, svm_parameter param,
		double C)
	{
		double[] alpha = new double[prob.size()];
		Solver.SolutionInfo si = new Solver.SolutionInfo();
		solve_c_svc(prob,param,alpha,si,C);

		svm.info("obj = "+si.obj+", rho = "+si.rho+"\n");

		// output SVs

		int nSV = 0;
		int nBSV = 0;
		for(int i=0;i<prob.size();i++)
		{
			++nSV;
			if(Math.abs(alpha[i]) >= si.upper_bound)
				++nBSV;
		}

		svm.info("nSV = "+nSV+", nBSV = "+nBSV+"\n");

		decision_function f = new decision_function();
		f.alpha = alpha;
		f.rho = si.rho;
		return f;
	}

	//
	// Interface functions
	//
	public static svm_model svm_train(List<SVMExample> prob, svm_parameter param)
	{
		svm_model model = new svm_model();
		model.param = param;

		int l=prob.size();
		decision_function f;

		f = svm_train_one(prob,param,param.C);

		// build output
		model.rho = f.rho;

		model.SV = new SVMExample[f.alpha.length];
		int p=0;
		for(int i=0;i<l;i++)
			if (f.alpha[i]!=0.0) model.SV[p++] = prob.get(i);

		model.sv_coef = f.alpha;
		return model;
	}

    public static void do_estimation(String prefix, String args)
    throws FileNotFoundException, IOException, ClassNotFoundException
    {
    	List<SVMExample> insts=
    		new ArrayList<SVMExample>();
    	svm_model model=null;
        ObjectInputStream is=
                new ObjectInputStream(new FileInputStream(prefix+".obj"));
        while (true) {
            try {
            	SparseVector vec=(SparseVector)is.readObject();
            	Boolean is_positive=(Boolean)is.readObject();
            	insts.add(SVMExample.make_binary_example(vec, is_positive));
            } catch (EOFException e) {
                break;
            }
        }
        is.close();
        if (insts.size()>0) {
        	svm_parameter param=new svm_parameter();
        	param.C=1.0;
        	param.cache_size=400.0;
        	param.kernel_type=svm_parameter.LINEAR;
        	param.svm_type=svm_parameter.C_SVC;
        	param.degree=2;
        	// TODO: set parameters
        	model=svm.svm_train(insts, param);
        }
        svm.svm_save_model(prefix+".model", model);
    }


	public static double svm_predict(svm_model model, SVMExample x)
	{
		return model.svm_predict_value(x);
	}

	static final String svm_type_table[] =
	{
		"c_svc","nu_svc","one_class","epsilon_svr","nu_svr",
	};

	static final String kernel_type_table[]=
	{
		"linear","polynomial","rbf","sigmoid","precomputed"
	};

	public static void svm_save_model(String model_file_name, svm_model model) throws IOException
	{
		DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(model_file_name)));

		svm_parameter param = model.param;

		fp.writeBytes("svm_type "+svm_type_table[param.svm_type]+"\n");
		fp.writeBytes("kernel_type "+kernel_type_table[param.kernel_type]+"\n");

		if(param.kernel_type == svm_parameter.POLY)
			fp.writeBytes("degree "+param.degree+"\n");

		if(param.kernel_type == svm_parameter.POLY ||
		   param.kernel_type == svm_parameter.RBF ||
		   param.kernel_type == svm_parameter.SIGMOID)
			fp.writeBytes("gamma "+param.gamma+"\n");

		if(param.kernel_type == svm_parameter.POLY ||
		   param.kernel_type == svm_parameter.SIGMOID)
			fp.writeBytes("coef0 "+param.coef0+"\n");

		int l = model.l;
		fp.writeBytes("total_sv "+l+"\n");
	
		{
			fp.writeBytes("rho "+model.rho);
			fp.writeBytes("\n");
		}
	
		fp.writeBytes("SV\n");
		double[] sv_coef = model.sv_coef;
		SVMExample[] SV = model.SV;

		for(int i=0;i<l;i++)
		{
			fp.writeBytes(sv_coef[i]+" ");
			SVMExample p = SV[i];
			p.writeln(fp);
		}

		fp.close();
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	public static svm_model svm_load_model(String model_file_name) throws IOException
	{
		return svm_load_model(new BufferedReader(new FileReader(model_file_name)));
	}

	public static svm_model svm_load_model(BufferedReader fp) throws IOException
	{
		// read parameters

		svm_model model = new svm_model();
		svm_parameter param = new svm_parameter();
		model.param = param;
		model.rho = 0.0;

		while(true)
		{
			String cmd = fp.readLine();
			String arg = cmd.substring(cmd.indexOf(' ')+1);

			if(cmd.startsWith("svm_type"))
			{
				int i;
				for(i=0;i<svm_type_table.length;i++)
				{
					if(arg.indexOf(svm_type_table[i])!=-1)
					{
						param.svm_type=i;
						break;
					}
				}
				if(i == svm_type_table.length)
				{
					System.err.print("unknown svm type.\n");
					return null;
				}
			}
			else if(cmd.startsWith("kernel_type"))
			{
				int i;
				for(i=0;i<kernel_type_table.length;i++)
				{
					if(arg.indexOf(kernel_type_table[i])!=-1)
					{
						param.kernel_type=i;
						break;
					}
				}
				if(i == kernel_type_table.length)
				{
					System.err.print("unknown kernel function.\n");
					return null;
				}
			}
			else if(cmd.startsWith("degree"))
				param.degree = atoi(arg);
			else if(cmd.startsWith("gamma"))
				param.gamma = atof(arg);
			else if(cmd.startsWith("coef0"))
				param.coef0 = atof(arg);
			else if(cmd.startsWith("nr_class"))
			{ } // ignore
			else if(cmd.startsWith("total_sv"))
				model.l = atoi(arg);
			else if(cmd.startsWith("rho"))
			{
				StringTokenizer st = new StringTokenizer(arg);
				model.rho = atof(st.nextToken());
			}
			else if(cmd.startsWith("nr_sv"))
			{ } // ignore
			else if(cmd.startsWith("SV"))
			{
				break;
			}
			else
			{
				System.err.print("unknown text in model file: ["+cmd+"]\n");
				return null;
			}
		}

		// read sv_coef and SV

		int l = model.l;
		model.sv_coef = new double[l];
		model.SV = new SVMExample[l];

		for(int i=0;i<l;i++)
		{
			String line = fp.readLine();
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			model.sv_coef[i] = atof(st.nextToken());
			int n = st.countTokens()/2;
			int[] idx=new int[n]; 
			double[] val=new double[n];
			for(int j=0;j<n;j++)
			{
				idx[j]=atoi(st.nextToken());
				val[j]=atof(st.nextToken());
			}
			model.SV[i] = SVMExample.make_binary_example(new SparseVector(idx,val), true);
		}

		fp.close();
		return model;
	}

	public static String svm_check_parameter(List<SVMExample> prob, svm_parameter param)
	{
		// svm_type

		int svm_type = param.svm_type;
		if(svm_type != svm_parameter.C_SVC &&
		   svm_type != svm_parameter.ONE_CLASS)
			return "unknown svm type";

		// kernel_type, degree
	
		int kernel_type = param.kernel_type;
		if(kernel_type != svm_parameter.LINEAR &&
		   kernel_type != svm_parameter.POLY &&
		   kernel_type != svm_parameter.RBF &&
		   kernel_type != svm_parameter.SIGMOID)
			return "unknown kernel type";

		if(param.gamma < 0)
			return "gamma < 0";

		if(param.degree < 0)
			return "degree of polynomial kernel < 0";

		// cache_size,eps,C,nu,p,shrinking

		if(param.cache_size <= 0)
			return "cache_size <= 0";

		if(param.eps <= 0)
			return "eps <= 0";

		if(svm_type == svm_parameter.C_SVC)
			if(param.C <= 0)
				return "C <= 0";

		if(svm_type == svm_parameter.ONE_CLASS)
			if(param.nu <= 0 || param.nu > 1)
				return "nu <= 0 or nu > 1";

		if(param.shrinking != 0 &&
		   param.shrinking != 1)
			return "shrinking != 0 and shrinking != 1";

		if(param.probability != 0 &&
		   param.probability != 1)
			return "probability != 0 and probability != 1";

		if(param.probability == 1 &&
		   svm_type == svm_parameter.ONE_CLASS)
			return "one-class SVM probability output not supported yet";

		return null;
	}
}
