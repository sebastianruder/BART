/*
 * StackedBase.java
 *
 * Created on August 16, 2007, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.algorithms.stacked;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.LocalDecoder;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureExtractor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
//TODO: We have pretty tight coupling between CandidateFilter, Component,
// StackedBase and the subclasses that do the actual learning -
// ideally, we would have a design where the learning steps
// would not have to inherit from the driver code
public abstract class StackedBase extends LocalDecoder implements CorefTrainer
{
    List<List<PairFeatureExtractor>> _fess;

    public abstract List<PairInstance> do_rank(List<PairInstance> cands,
           Component component, Component nextComponent);
    public abstract PairInstance classify_best(List<PairInstance> cands,
           Component component);
    public abstract PairInstance classify_first(List<PairInstance> cands,
            Component component);

    public PairInstance make_pair(Mention m_i, Mention m_j,
            Component component)
    {
        PairInstance result=new PairInstance(m_i, m_j);
        for (FeatureExtractor fe: _fess.get(component.ordinal()))
        {
            fe.extractFeatures(result);
        }
        result.setFeature(PairInstance.FD_POSITIVE,m_i.isCoreferent(m_j));
        return result;
    }
    
    public List<PairInstance> getCandidates(List<Mention> mentions, int ana,
            Component component)
    {
        List<PairInstance> cands=new ArrayList<PairInstance>();
        Mention m_i=mentions.get(ana);
        for (int j=ana-1;j>=0;j--)
        {
            Mention m_j=mentions.get(j);
            if (component.filter.wanted(m_i,m_j))
                cands.add(make_pair(m_i,m_j,component));
        }
        return cands;
    }

    public int resolvePronoun(List<Mention> mentions, int ana)
    {
        List<PairInstance> cands=
            getCandidates(mentions,ana,Component.RANK_PRONOUN);
        List<PairInstance> cands2=do_rank(cands,Component.RANK_PRONOUN,
                Component.CFY_PRONOUN);
        PairInstance result=classify_best(cands2,Component.CFY_PRONOUN);
        if (result==null)
            return -1;
        else
            return mentions.indexOf(result.getAntecedent());
    }
    
    public int resolveApposition(List<Mention> mentions, int ana)
    {
        List<PairInstance> cands=
                getCandidates(mentions,ana,Component.CFY_APPOSITION);
        PairInstance result=classify_best(cands,Component.CFY_APPOSITION);
        if (result==null)
            return -1;
        else
            return mentions.indexOf(result.getAntecedent());
        
    }

    public int resolveSameHead(List<Mention> mentions, int ana)
    {
        List<PairInstance> cands=
            getCandidates(mentions,ana,Component.CFY_SAME_HEAD);
        //PairInstance result=classify_first(cands,Component.CFY_SAME_HEAD);
        List<PairInstance> cands2=do_rank(cands,Component.RANK_SAME_HEAD,
                Component.CFY_SAME_HEAD);
        PairInstance result=classify_best(cands2,Component.CFY_SAME_HEAD);
        if (result!=null)
            return mentions.indexOf(result.getAntecedent());
        else
        {
//            Mention m_i=mentions.get(ana);
//            if (m_i.getDefinite() || m_i.getProperName())
//                if (!cands.isEmpty())
//                    return mentions.indexOf(cands.get(0).getAntecedent());
            return -1;
        }
    }
    
    public int resolveBridging(List<Mention> mentions, int ana)
    {
        List<PairInstance> cands=
            getCandidates(mentions,ana,Component.RANK_BRIDGING);
        List<PairInstance> cands2=do_rank(cands,Component.RANK_BRIDGING,
                Component.CFY_BRIDGING);
        PairInstance result=classify_best(cands2,Component.CFY_BRIDGING);
        if (result==null)
            return -1;
        else
            return mentions.indexOf(result.getAntecedent());        
    }

    
    public int resolveSingle(List<Mention> mentions, int ana) {
        Mention m_i=mentions.get(ana);
        if (m_i.getPronoun())
        {
            return resolvePronoun(mentions, ana);
        }
        else
        {
            // first try to resolve it as an apposition / copula
            int result1=resolveApposition(mentions, ana);
            if (result1!=-1)
                return result1;
            int result2=resolveSameHead(mentions,ana);
            //return result2;
            if (result2!=-1)
                return result2;
            if (isDiscourseNew(m_i))
                return -1;
            return resolveBridging(mentions,ana);
        }
    }

    public void flush() { /* not used by all */ }
    
    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (int i=1; i<mentions.size(); i++)
        {
            resolveSingle(mentions,i);
        }
        flush();
    }

    
    public boolean isDiscourseNew(Mention m_i)
    {
        if (m_i.getProperName())
            return true;
        List<Tree> premod=m_i.getPostmodifiers();
        if (premod!=null && !m_i.getPostmodifiers().isEmpty())
            return true;
        SemanticClass semclass = m_i.getSemanticClass();
        switch(semclass)
        {
            case PERSON:
                return false;
            case ORGANIZATION:
                return false;
            case GPE:
                return false;
            default:
                return true;
        }
    }
}
