/*
 * XMLAnnotator.java
 *
 * Created on July 21, 2007, 7:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


/*
* This is a fake comment, added only for testing purposes. Please delete it when merging with your copy. Olga
*/

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.TuningParameters;
import elkfed.coref.algorithms.cand_rank.CandRankDecoder;
import elkfed.coref.algorithms.cand_rank.MixedRankDecoder;
import elkfed.coref.algorithms.cand_rank.RankingDecoder;
import elkfed.coref.algorithms.soon.SoonDecoder;
import elkfed.coref.algorithms.dummy.SoonDummyDecoder;
import elkfed.coref.algorithms.soon.SoonDecoder_Expl;
import elkfed.coref.algorithms.soon.SoonDecoderDnew;
import elkfed.coref.algorithms.soon.SplitDecoder;
import elkfed.coref.algorithms.soon.split.Splitting2;
import elkfed.coref.algorithms.soon.split.Splitting2Filtered;
import elkfed.coref.algorithms.soon.split.Splitting4;
import elkfed.coref.algorithms.soon.split.Splitting2b;
import elkfed.coref.algorithms.soon.split.Splitting2a;
import elkfed.coref.algorithms.soon.split.Splitting3a;
import elkfed.coref.algorithms.soon.split.Splitting3;
import elkfed.coref.algorithms.stacked.StackedDecoder;
import elkfed.coref.eval.CEAFAggrScorer;
import elkfed.coref.processors.AnnotationProcessor;
import elkfed.main.xml.Classifier;
import elkfed.main.xml.Classifiers;
import elkfed.main.xml.CorefExperimentDocument;
import elkfed.main.xml.Experiment;
import elkfed.main.xml.Extractors;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.maxent.ClassifierBinary;
import elkfed.ml.maxent.ClassifierSinkBinary;
import elkfed.ml.maxent.MaxentRanker;
import elkfed.ml.svm.SVMClassifierFactory;
import elkfed.ml.svm.SVMLightClassifier;
import elkfed.ml.libsvm.LibSVMClassifierFactory;
import elkfed.ml.weka.WEKAInstanceClassifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class XMLAnnotator {
    final Experiment _exp;
    CorefResolver _resolver;
    
    public XMLAnnotator (Experiment exp)
        throws IOException, ClassNotFoundException
    {
        _exp=exp;
        _resolver=createResolver(exp);
    }
    
    public static CorefResolver createResolver(Experiment exp)
        throws IOException, ClassNotFoundException
    {
        elkfed.main.xml.System system=exp.getSystem();
        List<List<PairFeatureExtractor>> fess=
                new ArrayList<List<PairFeatureExtractor>>();
        for (Extractors ext: system.getExtractorsArray())
        {
            fess.add(XMLTrainer.createExtractors(ext));
        }
        if (system.getType()!=null &&
                system.getType().startsWith("dummy:"))
        {
            try {
                Class cls=Class.forName("elkfed.coref.algorithms.dummy."+
                        system.getType().substring(6));
                Constructor ct=cls.getConstructor(List.class,OfflineClassifier.class);
                List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
                return (CorefResolver)ct.newInstance(fess.get(0), ocs.get(0));

            } catch (Exception ex) {
                throw new RuntimeException("Cannot run DummyDecoder",ex);
            }
        }

        if ("soon".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SoonDecoder(fess.get(0),ocs.get(0));
        }

        else if ("soonE".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SoonDecoder_Expl(fess.get(0),ocs.get(0));
        }
        else if ("soonD".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SoonDecoderDnew(fess.get(0),fess.get(1),ocs.get(0),ocs.get(1));
        }
        else if ("split".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting2());
        }
        else if ("split2filter".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting2Filtered());
        }
        else if ("split2a".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting2a());
        }
        else if ("split2b".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting2b());
        }
        else if ("split3a".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting3a());
        }
        else if ("split3".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting3());
        }
        else if ("split4".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new SplitDecoder(fess, ocs, new Splitting4());
        }
        else if ("candrank".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            List<elkfed.ml.Ranker> rs=loadRankers(system.getClassifiers());
            return new CandRankDecoder(fess.get(0), ocs.get(0),
                    rs.get(0), rs.get(1));
        }
        else if ("mixrank".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            List<elkfed.ml.Ranker> rs=loadRankers(system.getClassifiers());
            return new MixedRankDecoder(fess, ocs.get(0),
                    rs.get(0));
        }
        else if ("rank".equalsIgnoreCase(system.getType())) {
            TuningParameters params=new TuningParameters();
            List<elkfed.ml.Ranker> rs=loadRankers(system.getClassifiers());
            params.readParameters(system.getTuningParameters());
            return new RankingDecoder(fess,rs,params);
        }
        else if ("stacked".equalsIgnoreCase(system.getType()))
        {
            List<OfflineClassifier> ocs=loadClassifiers(system.getClassifiers());
            return new StackedDecoder(fess,ocs);
        }
        else
        {
            throw new RuntimeException("Unsupported system type: "+system.getType());
        }
    }
    
    public static List<OfflineClassifier>
            loadClassifiers(Classifiers classifiers)
            throws IOException, ClassNotFoundException
    {
         List<OfflineClassifier> ocs=new ArrayList<OfflineClassifier>();
         for (Classifier c:classifiers.getClassifierArray())
         {
            if ("weka".equalsIgnoreCase(c.getType()))
            {
                ocs.add(new WEKAInstanceClassifier(
                        new File(ConfigProperties.getInstance().getModelDir(),
                            c.getModel()+".obj").getCanonicalPath()));
            }
            else if ("svmlight".equalsIgnoreCase(c.getType()))
            {
            	ocs.add(SVMClassifierFactory.getInstance().getClassifier(c.getModel(),
            			c.getOptions(), c.getLearner()));
            }
            else if ("maxent".equalsIgnoreCase(c.getType()))
            {
                ocs.add(new ClassifierBinary(new File(ConfigProperties.getInstance().getModelDir(),
                        c.getModel()).getAbsolutePath(),
                        ClassifierSinkBinary.comboFromString(c.getOptions())));
            } else if ("libsvm".equalsIgnoreCase(c.getType())) {
            	ocs.add(LibSVMClassifierFactory.getInstance().getClassifier(c.getModel(),
            			c.getOptions(), c.getLearner()));
            }
         }
         return ocs;
    }

    public static List<elkfed.ml.Ranker> loadRankers(Classifiers classifiers)
            throws IOException, ClassNotFoundException {
        List<elkfed.ml.Ranker> rs = new ArrayList<elkfed.ml.Ranker>();
        for (elkfed.main.xml.Ranker r : classifiers.getRankerArray()) {
            if ("maxent".equalsIgnoreCase(r.getType()))
            {
                rs.add(new MaxentRanker(new File(ConfigProperties.getInstance().getModelDir(),
                        r.getModel()).getAbsolutePath(),
                        ClassifierSinkBinary.comboFromString(r.getOptions())));
            }
        }
        return rs;
    }
    
    public void run() throws IOException
    {
        AnnotationProcessor proc=new AnnotationProcessor(
                    _resolver,
                    ConfigProperties.getInstance().getTestData(),
                    ConfigProperties.getInstance().getTestDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
                    
        );
        proc.processCorpus(ConfigProperties.getInstance().getRunPipeline()); 
        AnnotationProcessor.scoreMUC(ConfigProperties.getInstance().getTestData());
//        AnnotationProcessor.scoreCEAF(ConfigProperties.getInstance().getTestData());
        AnnotationProcessor.scoreCEAFAggr(ConfigProperties.getInstance().getTestData(), CEAFAggrScorer.Metric.PHI3);

        AnnotationProcessor.scoreCEAFAggr(ConfigProperties.getInstance().getTestData(), CEAFAggrScorer.Metric.PHI4);
        //AnnotationProcessor.dump(ConfigProperties.getInstance().getTestData());

        if (ConfigProperties.getInstance().getDbgPrint()) 
          _resolver.printStatistics();
    }

    public void runFold(int fold, int n_folds) throws IOException
    {
        AnnotationProcessor proc=new AnnotationProcessor(
                    _resolver,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTestDataId(),
                    ConfigProperties.getInstance().getMentionFactory()

        );
        proc.annotateFold(fold,n_folds);
        _resolver.printStatistics();
    }

    public static void main(String[] args)
    {
        try {
            CorefExperimentDocument doc;
            if (args.length==0)
            {
                doc=CorefExperimentDocument.Factory.parse(
                    ClassLoader.getSystemResourceAsStream("elkfed/main/"+
                        ConfigProperties.getInstance().getDefaultSystem()+".xml"));
            }
            else
            {
                doc=CorefExperimentDocument.Factory.parse(
                        new FileInputStream(args[0]));
            }
            new XMLAnnotator(doc.getCorefExperiment()).run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }
}
