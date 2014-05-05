/*
 * Copyright 2007 Project ELERFED
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.algorithms.cand_rank.CandRankEncoder;
import elkfed.coref.algorithms.soon.SoonEncoder;
import elkfed.coref.algorithms.soon.SoonEncoder_Expl;
import elkfed.coref.algorithms.soon.SoonEncoderDnew;
import elkfed.coref.algorithms.soon.SplitEncoder;
import elkfed.coref.algorithms.soon.split.Splitting2;
import elkfed.coref.algorithms.soon.split.Splitting4;
import elkfed.coref.algorithms.soon.split.Splitting2b;
import elkfed.coref.algorithms.soon.split.Splitting2a;
import elkfed.coref.algorithms.soon.split.Splitting3a;
import elkfed.coref.algorithms.soon.split.Splitting3;
import elkfed.coref.algorithms.cand_rank.MixedRankEncoder;
import elkfed.coref.algorithms.cand_rank.RankingEncoder;
import elkfed.coref.algorithms.soon.split.Splitting2Filtered;
import elkfed.coref.algorithms.stacked.StackedLearner2;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.ml.InstanceWriter;
import elkfed.ml.weka.WEKAInstanceWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import elkfed.main.xml.*;
import elkfed.ml.RankerSink;
import elkfed.ml.maxent.ClassifierSinkBinary;
import elkfed.ml.maxent.MaxentRankerSink;
import elkfed.ml.svm.SVMLightInstanceWriter;

/**
 * runs the training data creation based on information
 * read from an XML file.
 *
 * The interface code for the XML files was generated with XMLBeans.
 * To change the file format, it is necessary to modify
 * xml-experiment.xsd (in the xml-schemas subdirectory)
 * and then run
 * scomp -out ../libs/coref-exp.jar xml-experiment.xsd xml-experiment.xsdconfig
 * that directory to update the coref-exp.jar library that contains the
 * generated glue code.
 *
 * @author yannick
 */
public class XMLTrainer {
    final Experiment _exp;
    CorefTrainer _trainer;
    List<InstanceWriter> _iws;
    List<RankerSink> _rss;
   
    public CorefTrainer createTrainer(Experiment exp)
        throws IOException
    {
        elkfed.main.xml.System system=exp.getSystem();
        _iws=createLearners(system.getClassifiers());
        _rss=createRankingLearners(system.getClassifiers());
        List<List<PairFeatureExtractor>> fess=new ArrayList<List<PairFeatureExtractor>>();
        for (Extractors ext: system.getExtractorsArray())
        {
               fess.add(createExtractors(ext));
        }
        if ("soon".equalsIgnoreCase(system.getType()))
        {
            return new SoonEncoder(fess.get(0),_iws.get(0));
        }
        if ("evaldummy".equalsIgnoreCase(system.getType()))
        {
            return new SoonEncoder(fess.get(0),_iws.get(0));
        }
        else if ("soonE".equalsIgnoreCase(system.getType()))
        {
            return new SoonEncoder_Expl(fess.get(0),_iws.get(0));
        }
        else if ("soonD".equalsIgnoreCase(system.getType()))
        {
            return new SoonEncoderDnew(fess.get(0),_iws.get(0),fess.get(1),_iws.get(1));

        }
        else if ("split".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting2());
        }
        else if ("split2filter".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting2Filtered());
        }
        else if ("split4".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting4());
        }
        else if ("split2a".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting2a());
        }
        else if ("split2b".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting2b());
        }
        else if ("split3a".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting3a());
        }
        else if ("split3".equalsIgnoreCase(system.getType()))
        {
            return new SplitEncoder(fess,
                    _iws.toArray(new InstanceWriter[_iws.size()]),
                    new Splitting3());
        }
        else if ("mixrank".equalsIgnoreCase(system.getType()))
        {
            return new MixedRankEncoder(fess,_iws.get(0),
                    _rss.get(0));
        }
        else if ("candrank".equalsIgnoreCase(system.getType()))
        {
            return new CandRankEncoder(fess.get(0),_iws.get(0),
                    _rss.get(0),_rss.get(1));
        }
        else if ("rank".equalsIgnoreCase(system.getType()))
        {
            elkfed.coref.TuningParameters params=
                    new elkfed.coref.TuningParameters();
            params.readParameters(system.getTuningParameters());
            return new RankingEncoder(fess,_rss,params);
        }
        else if ("stacked".equalsIgnoreCase(system.getType()))
        {
            return new StackedLearner2(fess,_iws);
        }
        else
        {
            throw new RuntimeException("Unsupported system type: "+system.getType());
        }
    }
    
    public static List<InstanceWriter> createLearners(Classifiers classifiers)
        throws IOException
    {
        List<InstanceWriter> iws=new ArrayList<InstanceWriter>();
        for (Classifier c:classifiers.getClassifierArray())
        {
            if ("weka".equalsIgnoreCase(c.getType()))
            {
                iws.add(new WEKAInstanceWriter(new FileWriter(
                        new File(ConfigProperties.getInstance().getModelDir(),
                            c.getModel()+".arff"))));
            }
            else if ("SVMLight".equalsIgnoreCase(c.getType()))
            {
                iws.add(new SVMLightInstanceWriter(new FileWriter(
                        new File(ConfigProperties.getInstance().getModelDir(),
                        c.getModel()+".data")),
                        new File(ConfigProperties.getInstance().getModelDir(),
                        c.getModel()+".alphabet")));
            }
            else if ("MaxEnt".equalsIgnoreCase(c.getType()))
            {
//System.out.println("Creating a learner for " + c.getModel());
                iws.add(new ClassifierSinkBinary(
                        new File(ConfigProperties.getInstance().getModelDir(),
                        c.getModel()).getAbsolutePath(),
                        ClassifierSinkBinary.comboFromString(c.getOptions())));
            }
            else if ("libsvm".equalsIgnoreCase(c.getType()))
            {
                iws.add(new ClassifierSinkBinary(
                        new File(ConfigProperties.getInstance().getModelDir(),
                        c.getModel()).getAbsolutePath(),
                        ClassifierSinkBinary.monomial1));
            }
            else
            {
                throw new RuntimeException(
                        "Unsupported classifier type: "+c.getType());
            }
        }
        return iws;
    }

    public static List<RankerSink> createRankingLearners(Classifiers classifiers)
        throws IOException
    {
        List<RankerSink> rs=new ArrayList<RankerSink>();
        for (elkfed.main.xml.Ranker r : classifiers.getRankerArray()) {
            if ("maxent".equalsIgnoreCase(r.getType()))
            {
                rs.add(new MaxentRankerSink(new File(ConfigProperties.getInstance().getModelDir(),
                        r.getModel()).getAbsolutePath(),
                        ClassifierSinkBinary.comboFromString(r.getOptions())));
            }
        }
        return rs;
    }
    
    private static final String[] extractorPackages={
        "elkfed.coref.features.pairs",
        "elkfed.coref.features.pairs.srl",
        "elkfed.coref.features.pairs.wiki",
        "elkfed.coref.features.pairs.wn",
        "elkfed.coref.features.entities"
    };
    public static List<PairFeatureExtractor> createExtractors(Extractors exs)
    {
        List<PairFeatureExtractor> fes=new ArrayList<PairFeatureExtractor>();
        for (Extractor ex: exs.getExtractorArray())
        {
            String className=ex.getName();
            Class cls=null;
            try {
                // first, check for full package name
                cls=Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                for (String pkg: extractorPackages)
                {
                    try {
                        cls=Class.forName(pkg+"."+className);
                        break;
                    }
                    catch(ClassNotFoundException ee) { continue; }
                }
            }
            if (cls==null)
            {
                throw new RuntimeException("No Feature Extractor found: "+className);
            }
            PairFeatureExtractor fe;
            try {
                fe=(PairFeatureExtractor)cls.newInstance();
            }
            catch (ClassCastException e)
            {
                throw new RuntimeException("Not a PairFeatureExtractor: "+cls,e);
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException("Cannot instantiate "+cls,e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Cannot instantiate "+cls,e);
            }
            fes.add(fe);
        }
        return fes;
    }

    public XMLTrainer(Experiment exp) throws IOException
    {
        _exp=exp;
        _trainer=createTrainer(exp);
    }

    public void close() throws IOException {
        for (RankerSink rs: _rss) {
            rs.close();
        }
        for (InstanceWriter iw: _iws) {
            iw.close();
        }
    }

    public void run() throws IOException
    {
        TrainerProcessor proc=new TrainerProcessor(
                    _trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
        );
        proc.createTrainingData(ConfigProperties.getInstance().getRunPipeline());
        close();
    }

    public void runFold(int fold, int nFolds) throws IOException
    {
        TrainerProcessor proc=new TrainerProcessor(
                    _trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
        );
        proc.createTrainingFold(fold, nFolds);
        close();
    }
    
    /** runs the instance creation using either a file given on the
     *  command line or the idc0_system.xml default file from this package.
     */
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
            new XMLTrainer(doc.getCorefExperiment()).run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }
}
