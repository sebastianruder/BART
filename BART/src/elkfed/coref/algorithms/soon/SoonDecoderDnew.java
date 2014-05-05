/*
 * SoonDecoder.java
 *
 * Created on July 12, 2007, 5:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.soon;

import elkfed.coref.CorefResolver;
import elkfed.coref.eval.LinkScorer;
import elkfed.coref.eval.SplitLinkScorer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Dnew;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.cscott.jutil.DisjointSet;
import elkfed.config.ConfigProperties;


/** SoonDecoder with Dnew filter
 *
 * @author olga, based on SoonDecoder_Expl by Yannick
 * ToDo: check with SoonDecoder -- new adjustments (e.g. singletons)
 */
public class SoonDecoderDnew extends LocalDecoder {
    
    private static final int CHUNK_SIZE=1000;
    
    List<PairFeatureExtractor> _fes;
    List<PairFeatureExtractor> _fesdnew;
    OfflineClassifier _model;
    OfflineClassifier _modeldnew;
    FE_Dnew fee;//=new FE_Dnew();
    private double dnewthr;
    private double doldthr;

    public SoonDecoderDnew(List<PairFeatureExtractor> fes,
            List<PairFeatureExtractor> fesdnew,
            OfflineClassifier model,OfflineClassifier modeldnew) {
        _fes=fes;
        _model=model;
        _fesdnew=fesdnew;
        _modeldnew=modeldnew;
        dnewthr=ConfigProperties.getInstance().getDnewThr();
        doldthr=ConfigProperties.getInstance().getDoldThr();
        fee=new FE_Dnew(_fesdnew,_modeldnew);
        
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        _model.setHeader(fds);
    }
    
    public int resolveSingle(List<Mention> mentions, int ana) {
        List<PairInstance> candLinks=new ArrayList<PairInstance>();
        List<Boolean> result=new ArrayList<Boolean>();
        List<Double> confid=new ArrayList<Double>();
        Mention m_i=mentions.get(ana);
        if (fee.is_dnew(m_i, dnewthr)) {
         System.out.println("DNEW: " + m_i.getMarkableString());
         return -1;
        }
        base_j_loop:
        for (int base_j=ana-1; base_j>=0; base_j -= CHUNK_SIZE) {
            candLinks.clear();
            result.clear();
            final int low_j;
            if (base_j<CHUNK_SIZE)
                low_j=0;
            else
                low_j=base_j-CHUNK_SIZE+1;
            for (int j=base_j; j>=low_j; j--) {
                Mention m_j=mentions.get(j);
                if (m_i.overlapsWith(m_j) || m_i.embeds(m_j))
                    continue;
                PairInstance inst=new PairInstance(m_i, m_j);
                for (PairFeatureExtractor fe: _fes) {
                    fe.extractFeatures(inst);
                }
                // for debugging purposes, add the isCoreferent value to
                // the learning instances
                inst.setFeature(PairInstance.FD_POSITIVE,
                        inst.getAnaphor().isCoreferent(inst.getAntecedent()));
                candLinks.add(inst);
            }
            _model.classify(candLinks,result,confid);

            int minconfarg=-1;
            double minconf=10000;

            for (int j=0; j<candLinks.size(); j++) {
                if (result.get(j)) {
                    PairInstance lnk=candLinks.get(j);
                    return mentions.indexOf(lnk.getAntecedent());
                }
                if (confid.get(j)<minconf) {
                  minconf=confid.get(j);
                  minconfarg=j;
                }
            }

        if (fee.is_dold(m_i, doldthr) && minconfarg>=0) {
// if we are here, it means that we have a sure anaphor with no antecedent suggested yet

System.out.println("DOLD: " + m_i.getMarkableString());
                    PairInstance lnk=candLinks.get(minconfarg);
                    return mentions.indexOf(lnk.getAntecedent());
        
        }

        }
        return -1;
    }
}
