/*
 * StackedDecoder.java
 *
 * Created on August 16, 2007, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.stacked;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class StackedDecoder extends StackedBase2
{
    List<OfflineClassifier> _classifiers;

    public StackedDecoder(List<List<PairFeatureExtractor>> fess,
            List<OfflineClassifier> models) throws FileNotFoundException, IOException {
        _fess=fess;
        _classifiers=models;
        _rankers=createRankers("models/coref/stacked_");
        for (Component component: Component.values())
        {
            ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
            for (PairFeatureExtractor fe : _fess.get(component.ordinal())) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            if (component.is_ranker)
                _rankers[component.pos].setHeader(fds);
            else
                _classifiers.get(component.pos).setHeader(fds);
        }
    }
    

    public PairInstance classify_best(List<PairInstance> cands, Component component)
    {
        PairInstance best=null;
        double best_confidence=-1.0;
        List<Boolean> output=new ArrayList<Boolean>();
        List<Double> confidence=new ArrayList<Double>();
        _classifiers.get(component.pos).classify(cands,output,confidence);
        for (int i=0; i<cands.size(); i++)
        {
            if (output.get(i) && confidence.get(i)>best_confidence)
            {
                best_confidence=confidence.get(i);
                best=cands.get(i);
            }
        }
        return best;
    }
    
    public PairInstance classify_first(List<PairInstance> cands, Component component)
    {
        List<Boolean> output=new ArrayList<Boolean>();
        _classifiers.get(component.pos).classify(cands,output);
        for (int i=0; i<cands.size(); i++)
        {
            if (output.get(i))
            {
                return cands.get(i);
            }
        }
        return null;
    }
}
