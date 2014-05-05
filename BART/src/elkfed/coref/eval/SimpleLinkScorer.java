/*
 * SimpleLinkScorer.java
 *
 * Created on August 6, 2007, 7:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.eval;

import elkfed.coref.mentions.Mention;
import java.util.List;

/** a simplistic scorer for links
 *
 * @author yannick
 */
public class SimpleLinkScorer implements LinkScorer {
    int _tp=0;
    int _fp=0;
    int _fn=0;
    int _fpi = 0;
    int _fni = 0;
    int _tp_local=0;
    int _fp_local=0;
    int _fn_local=0;
    int _fpi_local=0;
    int _fni_local=0;
    String _name;
    
    /** Creates a new instance of SimpleLinkScorer */
    public SimpleLinkScorer(String name) {
        _name=name;
    }    
    
    public static int getAnte(List<Mention> mentions, int anaphor)
    {
        Mention m_i=mentions.get(anaphor);
        for (int k=0;k<anaphor;k++)
        {
            if (m_i.isCoreferent(mentions.get(k)))
            {
                 return k;
            }
        }
        return -1;
    }
    
    public static boolean hasAnte(List<Mention> mentions, int anaphor)
    {
        Mention m_i=mentions.get(anaphor);
        for (int k=0;k<anaphor;k++)
        {
            if (m_i.isCoreferent(mentions.get(k)))
            {
                 return true;
            }
        }
        return false;
    }
    
    public void scoreLink(List<Mention> mentions, int antecedent, int anaphor)
    {
        Mention m_i=mentions.get(anaphor);
        Mention m_j=mentions.get(antecedent);
        if (m_i.isCoreferent(m_j))
        {
            _tp_local++;
            //System.out.println("CORRECT: " + m_i.getMarkableString() + " " + m_i.getGender().toString() +
            //        " * " + m_j.getMarkableString() + " " + m_j.getGender().toString());
        }
        else
        {            
            //System.out.println("INCORRECT: " + m_i.getMarkableString() + " " + m_i.getGender().toString() +
            //        " * " + m_j.getMarkableString() + " " + m_j.getGender().toString());
            _fp_local++;
            if (hasAnte(mentions,anaphor)) _fn_local++;
            
            if ((m_i.getSetID() != null) && (m_j.getSetID() != null))
            {
                _fpi_local++;
            }
            if (hasAnte(mentions,anaphor) && (m_i.getSetID() != null))
            {
                _fni_local++;
            }
        }
    }
    
    public void scoreNonlink(List<Mention> mentions, int anaphor)
    {            
        if (hasAnte(mentions, anaphor)) 
        {
            //int x = getAnte(mentions, anaphor);
            //System.out.println("MISSING LINK " + mentions.get(anaphor).getMarkableString() + " " +
            //        mentions.get(anaphor).getGender().toString() + " * " + mentions.get(x).getMarkableString() + " " +
            //        mentions.get(x).getGender().toString());
            _fn_local++;
            _fni_local++;
        }
    }
    
    public void displayResults()
    {
        double precision_local=(_tp_local)/Math.max(_tp_local+_fp_local,1.0);
        double recall_local=(_tp_local)/Math.max(_tp_local+_fn_local,1.0);
        double f1_local=(recall_local>0.0 && precision_local>0.0) ?
            2*recall_local*precision_local/(recall_local+precision_local) :
            0.0;
        
        double precision_local_i=(_tp_local)/Math.max(_tp_local+_fpi_local,1.0);
        double recall_local_i=(_tp_local)/Math.max(_tp_local+_fni_local,1.0);
        double f1_local_i=(recall_local_i>0.0 && precision_local_i>0.0) ?
            2*recall_local_i*precision_local_i/(recall_local_i+precision_local_i) :
            0.0;
        
        _tp+=_tp_local;
        _fp+=_fp_local;
        _fn+=_fn_local;
        _fpi+=_fpi_local;
        _fni+=_fni_local;
        
        double precision=(_tp)/Math.max(_tp+_fp,1.0);
        double recall=(_tp)/Math.max(_tp+_fn,1.0);
        double f1=(recall>0.0 && precision>0.0) ?
            2*recall*precision/(recall+precision) : 0.0;       
        
        double precision_i=(_tp)/Math.max(_tp+_fpi,1.0);
        double recall_i=(_tp)/Math.max(_tp+_fni,1.0);
        double f1_i=(recall_i>0.0 && precision_i>0.0) ?
            2*recall_i*precision_i/(recall_i+precision_i) : 0.0;
        
        System.out.println(_name);
        System.out.format("Prec: %d/%d = %.3f (all: %d/%d = %.3f)\n",
                _tp_local,_tp_local+_fp_local,precision_local,
                _tp,_tp+_fp,precision);
        System.out.format("Recl: %d/%d = %.3f (all: %d/%d = %.3f)\n",
                _tp_local,_tp_local+_fn_local,recall_local,
                _tp,_tp+_fn,recall);
        System.out.format("F1=%.3f (all: %.3f)\n",f1_local,f1);
        
        System.out.println(_name + " ignoring markable errors");
        System.out.format("Prec: %d/%d = %.3f (all: %d/%d = %.3f)\n",
                _tp_local,_tp_local+_fpi_local,precision_local_i,
                _tp,_tp+_fpi,precision_i);
        System.out.format("Recl: %d/%d = %.3f (all: %d/%d = %.3f)\n",
                _tp_local,_tp_local+_fni_local,recall_local_i,
                _tp,_tp+_fni,recall_i);
        System.out.format("F1=%.3f (all: %.3f)\n",f1_local_i,f1_i);
        
        _tp_local=0;
        _fp_local=0;
        _fn_local=0;
        _fpi_local=0;
        _fni_local=0;
    }
    
    public void displayResultsShort()
    {
        _tp+=_tp_local;
        _fp+=_fp_local;
        _fn+=_fn_local;
        _fpi+=_fpi_local;
        _fni+=_fni_local;
        _tp_local=0;
        _fp_local=0;
        _fn_local=0;
        _fpi_local=0;
        _fni_local=0;

        double precision=(_tp)/Math.max(_tp+_fp,1.0);
        double recall=(_tp)/Math.max(_tp+_fn,1.0);
        double f1=2*recall*precision;
        if (f1>0.0) {
            f1 /= recall+precision;
        }
        
        double precision_i=(_tp)/Math.max(_tp+_fpi,1.0);
        double recall_i=(_tp)/Math.max(_tp+_fni,1.0);
        double f1_i=(recall_i>0.0 && precision_i>0.0) ?
            2*recall_i*precision_i/(recall_i+precision_i) : 0.0;
        
        System.out.format("%40s %.3f %.3f %.3f\n",
                _name,precision,recall,f1);
        System.out.format("%40s %.3f %.3f %.3f\n",
                _name+" ignoring markable errors",precision_i,recall_i,f1_i);
    }
    
    public void displayResultsShort(boolean ignoring)
    {
        _tp+=_tp_local;
        _fp+=_fp_local;
        _fn+=_fn_local;
        _fpi+=_fpi_local;
        _fni+=_fni_local;
        _tp_local=0;
        _fp_local=0;
        _fn_local=0;
        _fpi_local=0;
        _fni_local=0;
        if (ignoring) {
            double precision_i=(_tp)/Math.max(_tp+_fpi,1.0);
            double recall_i=(_tp)/Math.max(_tp+_fni,1.0);
            double f1_i=(recall_i>0.0 && precision_i>0.0) ?
                2*recall_i*precision_i/(recall_i+precision_i) : 0.0;
            System.out.format("%40s %.3f %.3f %.3f\n",
                    _name+" ignoring markable errors",precision_i,recall_i,f1_i);
        } else {
            double precision=(_tp)/Math.max(_tp+_fp,1.0);
            double recall=(_tp)/Math.max(_tp+_fn,1.0);
            double f1=(recall>0.0 && precision>0.0) ?
                2*recall*precision/(recall+precision) : 0.0;
            System.out.format("%40s %.3f %.3f %.3f\n",
                    _name,precision,recall,f1);
        }
    }
}
