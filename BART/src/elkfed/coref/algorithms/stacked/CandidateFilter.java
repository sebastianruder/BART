package elkfed.coref.algorithms.stacked;

import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Alias;
import elkfed.coref.mentions.Mention;
import java.util.regex.Pattern;

public enum CandidateFilter
{                PRONOUN {
            public boolean wanted(Mention m_i, Mention m_j) {
                return (m_i.getSentId()-m_j.getSentId()<=4);
            }
        },         APPOSITION {
            public boolean wanted(Mention m_i, Mention m_j) {
                return (m_i.getSentId()-m_j.getSentId()==0 &&
                        !m_j.getPronoun());
            }            
        }, SAME_HEAD {
            public boolean wanted(Mention m_i, Mention m_j) {
                if (!m_i.getProperName() &&
                        m_i.getSentId()-m_j.getSentId()>8)
                    return false;
                if (!matchingSubstring(m_i,m_j) &&
                        !FE_Alias.getAlias(
                            new PairInstance(m_i,m_j)))
                    return false;
//                if (m_j.getPronoun())
//                    return false;
                return true;
            }            
        },         BRIDGING {
            public boolean wanted(Mention m_i, Mention m_j) {
//                if (m_j.getPronoun())
//                    return false;
                return (m_i.getSentId()-m_j.getSentId()<=4);            
            }
        };

    public abstract boolean wanted(Mention m_i, Mention m_j);
    protected static final Pattern ignorable=
        Pattern.compile("^(?:Mrs?\\.|Corp\\.|Co\\.|Inc\\.|[A-Za-z]\\.?|[Oo]f)$");
    public static boolean matchingSubstring(Mention m1, Mention m2)
    {
        String[] s1=m1.getHeadOrName().split(" ");
        String[] s2=m2.getHeadOrName().split(" ");
        for (int i=0; i< s1.length; i++)
        {
//            if (ignorable.matcher(s1[i]).matches())
//              continue;
            for (int j=0; j< s2.length; j++)
            {
                if (s1[i].equalsIgnoreCase(s2[j]))
                return true;
            }
        }
        return false;
    }
}