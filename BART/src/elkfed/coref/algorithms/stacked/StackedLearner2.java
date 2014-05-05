/*
 * StackedLearner2.java
 *
 * Created on August 16, 2007, 7:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.stacked;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.maxent.MaxentRanker;
import elkfed.ml.maxent.MaxentRankerSink;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class StackedLearner2 extends StackedBase2 {
    List<InstanceWriter> _iws;
    MaxentRanker[][] _foldedRankers;
    int _fold;

    
    public StackedLearner2(List<List<PairFeatureExtractor>> fess,
                    List<InstanceWriter> iws) throws FileNotFoundException, IOException
    {
        _fess=fess;
        _iws=iws;
        _rankers=createRankers("models/coref/stacked_");
        _foldedRankers=new MaxentRanker[StackedLearner1.NUM_FOLDS][];
        for (int i=0; i<StackedLearner1.NUM_FOLDS; i++)
            _foldedRankers[i]=createRankers("models/coref/stacked_fold"+i+"_");
        for (Component component: Component.values())
        {
            ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
            for (PairFeatureExtractor fe : _fess.get(component.ordinal())) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            if (component.is_ranker)
            {
                _rankers[component.pos].setHeader(fds);
                for (int i=0; i<StackedLearner1.NUM_FOLDS; i++)
                {
                    _foldedRankers[i][component.pos].setHeader(fds);
                }
            }
            else
                try {
                    _iws.get(component.pos).setHeader(fds);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        }
    }

    public List<PairInstance> do_rank(List<PairInstance> cands,
            Component component, Component nextComponent)
    {
        List<PairInstance> result=_foldedRankers[_fold][component.pos].getRanking(cands);
        List<PairInstance> result2=
                new ArrayList<PairInstance>(component.shortlist_size);
        for (int i=0; i<Math.min(result.size(),component.shortlist_size); i++)
        {
            PairInstance inst=result.get(i);
            if (nextComponent!=null)
                result2.add(make_pair(inst.getAnaphor(), inst.getAntecedent(),
                        nextComponent));
            else
                result2.add(inst);
        }
        return result2;
    }
    
    public PairInstance classify_best(List<PairInstance> cands, Component component) {
        InstanceWriter iw=_iws.get(component.pos);
        try {
            for (PairInstance cand: cands)
                iw.write(cand);
        } catch (IOException e) {
            throw new RuntimeException("Can't write instances",e);
        }
        if (cands.isEmpty())
            return null;
        else
            return cands.get(0);
    }

    public PairInstance classify_first(List<PairInstance> cands, Component component) {
        InstanceWriter iw=_iws.get(component.pos);
        boolean ante_found=false;
        for (PairInstance cand: cands)
            if (cand.getFeature(PairInstance.FD_POSITIVE))
                ante_found=true;
        if (!ante_found) return null;
        try {
            for (PairInstance cand: cands)
            {
                iw.write(cand);
                if (cand.getFeature(PairInstance.FD_POSITIVE))
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't write instances",e);
        }
        if (cands.isEmpty())
            return null;
        else
            return cands.get(0);
    }

    public void flush()
    {
        try {
            for (InstanceWriter iw: _iws)
                iw.flush();
            _fold++;
            if (_fold==StackedLearner1.NUM_FOLDS) _fold=0;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot write file",e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file",e);
        }
    }
}
