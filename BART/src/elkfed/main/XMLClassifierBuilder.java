/*
 * XMLClassifierBuilder.java
 *
 * Created on July 21, 2007, 7:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.main.xml.Classifier;
import elkfed.main.xml.Ranker;
import elkfed.main.xml.CorefExperimentDocument;
import elkfed.ml.libsvm.LibSVMClassifierFactory;
import elkfed.ml.maxent.MaxEntClassifierFactory;
import elkfed.ml.maxent.ParameterEstimatorBinary;
import elkfed.ml.maxent.ParameterEstimator;
import elkfed.ml.svm.SVMLightClassifier;
import elkfed.ml.weka.WEKAInstanceClassifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author yannick
 */
public class XMLClassifierBuilder {
    public static void buildClassifiers(CorefExperimentDocument doc)
    throws IOException {
        for (Classifier c:doc.getCorefExperiment().getSystem()
        .getClassifiers().getClassifierArray())
        {
            if ("weka".equalsIgnoreCase(c.getType())) {
                WEKAInstanceClassifier.runLearner(c.getModel(),
                        c.getLearner(),c.getOptions());
            }
            else if ("svmlight".equalsIgnoreCase(c.getType())) {
                SVMLightClassifier.runLearner(c.getModel(),
                        c.getOptions());
            }
            else if ("MaxEnt".equalsIgnoreCase(c.getType())) {
            	MaxEntClassifierFactory.getInstance().do_learning(c.getModel(),
            			c.getOptions(), c.getLearner());
            } else if ("libsvm".equalsIgnoreCase(c.getType())) {
            	LibSVMClassifierFactory.getInstance().do_learning(c.getModel(),
            			c.getOptions(), c.getLearner());
            } else {
                throw new RuntimeException(
                        "no learning package called "+c.getType());
            }
        }
        for (Ranker r:doc.getCorefExperiment().getSystem()
                .getClassifiers().getRankerArray()) {
            if ("MaxEnt".equalsIgnoreCase(r.getType())) {
                try {
                    ParameterEstimator.do_estimation(
                            new File(ConfigProperties.getInstance().getModelDir(),
                            r.getModel()).getAbsolutePath());
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Cannot do parameter estimation",ex);
                }
            } else {
                throw new RuntimeException(
                        "no learning package called "+r.getType());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            CorefExperimentDocument doc;
            if (args.length==0) {
                doc=CorefExperimentDocument.Factory.parse(
                        ClassLoader.getSystemResourceAsStream("elkfed/main/"+
                        ConfigProperties.getInstance().getDefaultSystem()+".xml"));
            } else {
                doc=CorefExperimentDocument.Factory.parse(
                        new FileInputStream(args[0]));
            }
            buildClassifiers(doc);
        } catch (Exception e) {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }
}
