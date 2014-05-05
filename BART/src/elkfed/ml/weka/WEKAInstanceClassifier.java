/*
 * WEKAInstanceClassifier.java
 *
 * Created on July 16, 2007, 4:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import elkfed.config.ConfigProperties;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;

/**
 *
 * @author brett.shwom
 */
public class WEKAInstanceClassifier implements OfflineClassifier {

    /** Creates a new instance of WEKAInstanceClassifier */

    private final Classifier classifier;
    private final Instances dataset;
    private List<FeatureDescription> _fds;


    public WEKAInstanceClassifier(String filename)
            throws IOException, ClassNotFoundException
    {
        ObjectInputStream f = new ObjectInputStream(new FileInputStream(filename));
        classifier = (Classifier) f.readObject();
        dataset = (Instances) f.readObject();
    }

    public WEKAInstanceClassifier(File file)
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream f = new ObjectInputStream(new FileInputStream(file));
        classifier = (Classifier) f.readObject();
        dataset = (Instances) f.readObject();
    }

    public void classify(List<? extends Instance> problems, List output) {

        for (Instance i : problems) {

            weka.core.Instance instance = new weka.core.Instance(dataset.numAttributes());
            instance.setDataset(dataset);

            for (int c = 0; c < _fds.size()-1; c++) {
                FeatureDescription fd = _fds.get(c);

                if (fd.type == FeatureType.FT_SCALAR) {
                    instance.setValue(c, ((Number) i.getFeature(fd)).doubleValue());
                } else {
                    instance.setValue(c, i.getFeature(fd).toString());
                }
            }
            try {
                double retVal = classifier.classifyInstance(instance);
                FeatureDescription des = _fds.get(_fds.size()-1);

                switch (des.type) {
                    case FT_BOOL:
                        if (retVal < 0.5 )
                            output.add(Boolean.FALSE);
                        else
                            output.add(Boolean.TRUE);
                        break;
                    case FT_NOMINAL_ENUM:
                       output.add(des.cls.getEnumConstants()[(int)retVal]);
                       break;
                    case FT_SCALAR:
                        output.add(retVal);
                        break;
                    default: throw new RuntimeException("Feature Type Unknown");

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error", e); }
        }
    }

    public void classify(List<? extends Instance> problems, List output,
            List<Double> confidence) {

        throw new RuntimeException("not implemented");

    }

    public static void runLearner(String modelName, String classifier,
            String options)
        throws IOException
    {
        System.err.println(new File(ConfigProperties.getInstance().getModelDir(),
                        modelName+".arff").getCanonicalPath());
        Instances trainingSet=new Instances(new FileReader(
                new File(ConfigProperties.getInstance().getModelDir(),
                    modelName+".arff")));
        trainingSet.setClassIndex(trainingSet.numAttributes()-1);

        Instances filteredTrainingSet = trainingSet;

        StringToNominal stringToNominal;

        // convert the string attributes to nominal attributes
        try {
            for(int attIndex = 0; attIndex < trainingSet.numAttributes()-1; attIndex++) {
                if(trainingSet.attribute(attIndex).isString()) {
                    stringToNominal = new StringToNominal();
                    //stringToNominal.setAttributeIndex(Integer.toString(attIndex + 1));
                    stringToNominal.setInputFormat(filteredTrainingSet);
                    filteredTrainingSet = Filter.useFilter(filteredTrainingSet, stringToNominal);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("String to nominal conversion failed", ex );
        }

        Classifier cls;
        try {
            try {
                Class.forName(classifier);
            }
            catch(ClassNotFoundException e)
            {
                System.err.format("Class %s not found, using J48\n",classifier);
                classifier="weka.classifiers.trees.J48";
            }
            String[] args=Utils.splitOptions(options);
            cls=Classifier.forName(classifier,args);
            cls.buildClassifier(filteredTrainingSet);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Weka learner failed",e);
        }
        ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(
                new File(ConfigProperties.getInstance().getModelDir(),
                    modelName+".obj")));
        oos.writeObject(cls);
        trainingSet.delete();
        oos.writeObject(trainingSet);
    }

    public void setHeader(List<FeatureDescription> fds) {
        _fds = fds;
    }
}
