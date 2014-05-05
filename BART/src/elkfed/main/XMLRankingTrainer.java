/*
 * XMLRankingTrainer.java
 *
 * Created on August 17, 2007, 10:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.algorithms.stacked.StackedLearner1;
import elkfed.main.xml.CorefExperimentDocument;
import elkfed.main.xml.Experiment;
import elkfed.main.xml.Extractors;
import elkfed.ml.maxent.ParameterEstimator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class XMLRankingTrainer extends XMLTrainer {
    XMLRankingTrainer(Experiment exp) throws IOException
    {
        super(exp);
    }
    
    public CorefTrainer createTrainer(Experiment exp)
        throws IOException
    {
        elkfed.main.xml.System system=exp.getSystem();
        //List<InstanceWriter> iws=createLearners(system.getClassifiers());
        List<List<PairFeatureExtractor>> fess=new ArrayList<List<PairFeatureExtractor>>();
        for (Extractors ext: system.getExtractorsArray())
        {
               fess.add(createExtractors(ext));
        }
        if ("stacked".equalsIgnoreCase(system.getType()))
        {
            return new StackedLearner1(fess);
        }
        else
        {
            throw new RuntimeException("Unsupported system type: "+system.getType());
        }
    }
    
    void do_estimation()
        throws FileNotFoundException, IOException
    {
        String type=_exp.getSystem().getType();
        if ("stacked".equalsIgnoreCase(type))
        {
            try {
                for (String modelname: StackedLearner1.getModelNames())
                {
                    ParameterEstimator.do_estimation(modelname);
                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Cannot do parameter estimation",ex);
            }
        }
        else
        {
            throw new RuntimeException("Unsupported system type: "+type);
        }
    }
    
     /** runs the instance creation using either a file given on the
     *  command line or the default file from the configuration
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
            XMLRankingTrainer t=new XMLRankingTrainer(doc.getCorefExperiment());
            t.run();
            t.do_estimation();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }
}
