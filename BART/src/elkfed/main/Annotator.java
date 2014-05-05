/*
 * Annotator.java
 *
 * Created on July 18, 2007, 6:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import java.util.ArrayList;
import java.util.List;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.dummy.SoonDummyDecoder;
import elkfed.coref.processors.AnnotationProcessor;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;

/**
 *
 * @author yannick
 */
public class Annotator {
    public static List<FeatureDescription>
                getExtractedFeatures(List<? extends FeatureExtractor<?>> fes)
    {
        List<FeatureDescription> fds=new ArrayList<FeatureDescription>();
        for (FeatureExtractor fe: fes)
        {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        return fds;
    }

    public static void main(String args[])    
    {
        try{
              List<PairFeatureExtractor> fes=Trainer.getExtractors();
//            OfflineClassifier cls=new WEKAInstanceClassifier(
//                    new File(
//                        ConfigProperties.getInstance().getModelDir(),
//                        ConfigProperties.getInstance().getModelName()
//                    ));
            CorefResolver resolv=new SoonDummyDecoder(fes,null);
            AnnotationProcessor cap=new AnnotationProcessor(
                    resolv,
                    ConfigProperties.getInstance().getTestData(),
                    ConfigProperties.getInstance().getTestDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            cap.processCorpus();
            AnnotationProcessor.scoreMUC(ConfigProperties.getInstance().getTestData());
//            AnnotationProcessor.scoreCEAF(ConfigProperties.getInstance().getTestData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
