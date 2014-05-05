/*
 * StackedLearner1.java
 *
 * Created on August 16, 2007, 7:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.stacked;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.maxent.ClassifierSinkBinary;
import elkfed.ml.maxent.MaxentRankerSink;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class StackedLearner1 extends StackedBase {
    public final static int NUM_FOLDS=5;
    private int _fold;
    MaxentRankerSink[] _rankerSink;
    MaxentRankerSink[][] _foldedRankerSinks;
    
    MaxentRankerSink[] createSinks(String prefix)
        throws FileNotFoundException, IOException
    {
        MaxentRankerSink[] result=new MaxentRankerSink[Component.num_rankers];
        for (Component component : Component.values())
        {
            if (component.is_ranker)
            {
                result[component.pos]=
                        new MaxentRankerSink(prefix+component.toString(),
                            ClassifierSinkBinary.monomial2);
            }
        }
        return result;
    }

    public static String[] getModelNames() {
        String[] result=new String[Component.num_rankers*(1+NUM_FOLDS)];
        for (Component component : Component.values())
        {
            if (component.is_ranker)
            {
                result[component.pos]=
                        "models/coref/stacked_"+component.toString();
            }
        }
        for (int i=0; i<NUM_FOLDS; i++)
        {
            for (Component component : Component.values())
            {
                if (component.is_ranker)
                {
                    result[(i+1)*Component.num_rankers+component.pos]=
                        "models/coref/stacked_fold"+i+"_"+component.toString();
                }
            }
        }
        return result;
    }

    public StackedLearner1(List<List<PairFeatureExtractor>> fess)
        throws FileNotFoundException, IOException
    {
        _fess=fess;
        _rankerSink=createSinks("models/coref/stacked_");
        _foldedRankerSinks=new MaxentRankerSink[NUM_FOLDS][];
        for (int i=0; i<NUM_FOLDS; i++)
            _foldedRankerSinks[i]=createSinks("models/coref/stacked_fold"+i+"_");
        for (Component component: Component.values())
        {
            ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
            for (PairFeatureExtractor fe : _fess.get(component.ordinal())) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            if (component.is_ranker)
            {
                _rankerSink[component.pos].setHeader(fds);
                for (int i=0; i<NUM_FOLDS; i++)
                {
                    _foldedRankerSinks[i][component.pos].setHeader(fds);
                }
            }
        }
    }

    public List<PairInstance> do_rank(List<PairInstance> cands, Component component, Component nextComponent)
    {
        try {
            _rankerSink[component.pos].write(cands);
            for (int i=0;i<NUM_FOLDS;i++)
            {
                if (i!=_fold)
                    _foldedRankerSinks[i][component.pos].write(cands);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write to RankerSink",e);
        }
        return cands;
    }

    public void flush()
    {
        try {
            for (MaxentRankerSink rs: _rankerSink)
                rs.flush();
            for (MaxentRankerSink[] rss: _foldedRankerSinks)
                for (MaxentRankerSink rs: rss)
                    rs.flush();
            _fold++;
            if (_fold==NUM_FOLDS) _fold=0;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot write file",e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file",e);
        }
    }

    public PairInstance classify_best(List<PairInstance> cands, Component component)
    { return null; }

    public PairInstance classify_first(List<PairInstance> cands, Component component)
    { return null; }

}
