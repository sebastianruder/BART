/*
 * FE_StringKernel.java
 *
 * Created on August 9, 2007, 11:00 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author ajern
 */
public class FE_StringKernel implements PairFeatureExtractor {
    
    private double lambda = 0.4;
    private double lambda2 = lambda*lambda;
    
    public static final FeatureDescription<Double> FD_STRING_KERNEL=
            new FeatureDescription<Double>(FeatureType.FT_SCALAR, "StringKernel");
    
    /**
     * Creates a new instance of FE_StringKernel
     */
    public FE_StringKernel(double l) {
        lambda = l;
        lambda2 = l*l;
    }
    
    public FE_StringKernel() {
        ;
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_STRING_KERNEL);        
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_STRING_KERNEL, getSK(inst.getAnaphor().getMarkableString(),
                inst.getAntecedent().getMarkableString()));
    }
    
    public double getSK(String s1, String s2) {
        return SK(s1, s2) / 
                (Math.sqrt( SK(s1,s1) * SK(s2,s2) ));
    }
    
    private double SK(String s1, String s2) {
        
        // Dynamic programming matrices
        double DPS[][] = new double[s1.length()+1][s2.length()+1];
        double DP[][] = new double[s1.length()+1][s2.length()+1];
        //double delta_matrix[MAX_WORD_LENGTH][MAX_WORD_LENGTH];
        double kernel_mat[] = new double[s1.length()+1];
        
        int i,j,l,p;
        double K;
        
        p = s1.length(); if (s2.length() < s1.length()) p = s2.length();
        
        
        for (j = 0; j <= s2.length(); j++) {
            for (i = 0; i <= s1.length(); i++) {
                DPS[i][j]=DP[i][j]=0;
            }
        }
        
        kernel_mat[0] = 0;
        for (i=1; i <= s1.length(); i++) {
            for (j = 1; j <= s2.length(); j++) {
                //printf("part 1 -- substr1: %s ... substr2: %s\n", s1+i-1, s2+j-1);
                //if(strcmp(s1+i-1, s2+j-1)==0)
                //System.out.println("char1: " + s1.charAt(i-1) + " char2 :" + s2.charAt(j-1));
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                //if (*(s1+i-1) == *(s2+j-1)) {
                    DPS[i][j] = lambda2;
                    kernel_mat[0]+=DPS[i][j];
                } else DPS[i][j]=0;
            }
        }
        
        for(l = 1;l < p; l++) {
            kernel_mat[l]=0;
            for (j = 0; j <= s2.length(); j++)
                DP[l-1][j]=0;
            for (i = 0; i <= s1.length(); i++)
                DP[i][l-1]=0;
            
            //System.out.println("\nPART 2");
            for (i = l; i <= s1.length(); i++) {
                for (j = l; j <= s2.length(); j++) {
                    DP[i][j] = DPS[i][j]+lambda*DP[i-1][j]
                            + lambda*DP[i][j-1]
                            - lambda2*DP[i-1][j-1];
                    
                    //printf("part2 -- substr1: %s ... substr2: %s\n", s1+i-1, s2+j-1);
                    //if(strcmp(s1+i-1, s2+j-1)==0) {
                    
                    //System.out.println("char1: " + s1.charAt(i-1) + " char2 :" + s2.charAt(j-1));
                    if (s1.charAt(i-1) == s2.charAt(j-1)) {
                        DPS[i][j] = lambda2 * DP[i-1][j-1];
                        kernel_mat[l] += DPS[i][j];
                    }
                }
            }
        }
        
        K = 0;
        for(l = 0; l < p; l++) {
            //System.out.println("l = " + l + " kernel_mat[l] = " + kernel_mat[l]);
            K += kernel_mat[l];
        }
        return K;
        
    }

    
}
