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
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Alias;
import elkfed.coref.features.pairs.FE_NameStructure;
//import elkfed.coref.features.pairs.FE_NameStructure_Vals;
import elkfed.coref.features.pairs.FE_StringKernel;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.ml.FeatureDescription;
import elkfed.ml.InstanceWriter;
import elkfed.ml.svm.SVMLightInstanceWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static elkfed.lang.EnglishLinguisticConstants.PRONOUN;

/**
 *
 * @author yannick
 */
public class ExtractNameMatch implements CorefTrainer {
    InstanceWriter _out;
    List<PairFeatureExtractor> _fes;
    ExtractNameMatch(InstanceWriter out)
        throws IOException
    {
        _out=out;
        _fes=new ArrayList<PairFeatureExtractor>();
        _fes.add(new FE_StringMatch());
        _fes.add(new FE_Alias());
        _fes.add(new FE_StringKernel());
        _fes.add(new FE_NameStructure());
        //_fes.add(new FE_NameStructure_Vals());
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        _out.setHeader(fds);
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
            if (!m_i.getMarkable().getAttributeValue("type").equals("enamex"))
            {
                continue;
            }
//            if (!SimpleLinkScorer.hasAnte(mentions,i))
//                continue;
//            if (hasSameHead(mentions,i))
//                continue;
            for (int j=i-1;j>=0;j--) {
                Mention m_j=mentions.get(j);
                if (!m_j.getMarkable().getAttributeValue("type").equals("enamex"))
                {
                    continue;
                }
                PairInstance inst=new PairInstance(m_i,m_j);
                for (PairFeatureExtractor fe: _fes) {
                    fe.extractFeatures(inst);
                }
                inst.setFeature(PairInstance.FD_POSITIVE,m_i.isCoreferent(m_j));
                _out.write(inst);
                    
            }
        }
        _out.flush();
    }
    
    public static void main(String[] args) {
        try {
            InstanceWriter out=new SVMLightInstanceWriter(new FileWriter(
                    System.getProperty("elkfed.corpus","data")+"_names.data"),
                    new File(System.getProperty("elkfed.corpus","data")+"_names.dict"));
            ExtractNameMatch trainer=new ExtractNameMatch(out);
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
