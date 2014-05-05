/*
 * ExtractData.java
 *
 * Created on August 7, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.eval.SimpleLinkScorer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.nlp.util.Stopwords;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static elkfed.lang.EnglishLinguisticConstants.PRONOUN;

/**
 *
 * @author yannick
 */
public class ExtractData implements CorefTrainer {
    PrintStream _os;
    ExtractData(PrintStream os) {
        _os=os;
    }
    
    public boolean matchingSubstring(Mention m1, Mention m2)
    {
        String[] s1=m1.getHeadOrName().split(" ");
        String[] s2=m2.getHeadOrName().split(" ");
        for (int i=0; i< s1.length; i++)
        {
            for (int j=0; j< s2.length; j++)
            {
                if (s1[i].equalsIgnoreCase(s2[j]))
                    return true;
            }
        }
        return false;
    }
    
    public boolean hasSameHead(List<Mention> mentions, int anaphor)
    {
        Mention m_i=mentions.get(anaphor);
        for (int k=0;k<anaphor;k++)
        {
            if (matchingSubstring(m_i,mentions.get(k)))
            {
                 return true;
            }
        }
        return false;
    }
    
    public void encodeDocument(List<Mention> mentions)
    throws IOException {
        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
//            if (m_i.getPronoun())
//            {
//                _os.format("%s\t%s\n",m_i.getMarkableString(),
//                    SimpleLinkScorer.hasAnte(mentions,i));
//            }
            if (m_i.getPronoun())
                continue;
            if (!SimpleLinkScorer.hasAnte(mentions,i))
                continue;
            if (hasSameHead(mentions,i))
                continue;
            for (int j=i-1;j>=0;j--) {
                Mention m_j=mentions.get(j);
                if (m_j.getPronoun())
                    continue;
                if (m_i.getMarkableString().matches(PRONOUN) ||
                        m_j.getMarkableString().matches(PRONOUN) ||
                        Stopwords.getInstance()
                        .contains(m_i.getMarkableString().toLowerCase()) ||
                        Stopwords.getInstance()
                        .contains(m_j.getMarkableString().toLowerCase()))
                    continue;
                _os.format("%s\t%s\t%s\t%s\t%d\t%s\n",
                        m_i.getHeadOrName(), m_i.getProperName(),
                        m_j.getHeadOrName(), m_j.getProperName(),
                        m_i.getSentId()-m_j.getSentId(),
                        m_i.isCoreferent(m_j));
                if (m_i.isCoreferent(m_j))
                    break;
            }
        }
        _os.flush();
    }
    
    public static void main(String[] args) {
        try {
            PrintStream out=new PrintStream(new FileOutputStream(
                    System.getProperty("elkfed.corpus","data")+".out"));
            ExtractData trainer=new ExtractData(out);
            TrainerProcessor proc=new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
                    );
            proc.createTrainingData();
//            proc=new TrainerProcessor(
//                    trainer,
//                    ConfigProperties.getInstance().getTestData(),
//                    ConfigProperties.getInstance().getTestDataId(),
//                    ConfigProperties.getInstance().getMentionFactory()
//            );
//            proc.createTrainingData();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
