package elkfed.main;

import java.util.ArrayList;
import java.util.List;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.dummy.SoonDummyDecoder;
import elkfed.coref.algorithms.sieve.SieveDecoder;
import elkfed.coref.processors.AnnotationProcessor;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;

/**
 *
 * @author sebastianruder
 * 
 */
public class SieveAnnotator {
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
        try {
        	CorefResolver resolv = new SieveDecoder();
            AnnotationProcessor cap = new AnnotationProcessor(
                    resolv,
                    ConfigProperties.getInstance().getTestData(),
                    ConfigProperties.getInstance().getTestDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            cap.processCorpus();
            AnnotationProcessor.scoreMUC(ConfigProperties.getInstance().getTestData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
