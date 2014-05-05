/*
 * StackedTrainerPart2.java
 *
 * Created on August 16, 2007, 5:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.stacked;

import elkfed.coref.PairInstance;
import elkfed.ml.maxent.ClassifierSinkBinary;
import elkfed.ml.maxent.MaxentRanker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public abstract class StackedBase2 extends StackedBase {
   MaxentRanker[] _rankers;

    MaxentRanker[] createRankers(String prefix)
        throws FileNotFoundException, IOException
    {
        MaxentRanker[] result=new MaxentRanker[Component.num_rankers];
        try {
            for (Component component : Component.values())
            {
                if (component.is_ranker)
                {
                    result[component.pos]=
                            new MaxentRanker(prefix+component.toString(),
                                ClassifierSinkBinary.monomial2);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load ranker",e);
        }
        return result;
    }

    public List<PairInstance> do_rank(List<PairInstance> cands,
            Component component, Component nextComponent)
    {
        List<PairInstance> result=_rankers[component.pos].getRanking(cands);
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
}
