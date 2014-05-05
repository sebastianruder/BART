/*
 * Trainer.java
 *
 * Created on July 18, 2007, 5:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.algorithms.soon.SoonEncoder;
import elkfed.coref.features.pairs.FE_Alias;
import elkfed.coref.features.pairs.FE_Appositive;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.mentiontype_old.FE_MentionType;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.ml.InstanceWriter;
import elkfed.ml.weka.WEKAInstanceWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class Trainer {
    public static List<PairFeatureExtractor> getExtractors() {
        List<PairFeatureExtractor> fes=
                new ArrayList<PairFeatureExtractor>();
        // basic information about the anaphor
        fes.add(new FE_MentionType());
        // agreement features
        fes.add(new FE_Gender());
        fes.add(new FE_Number());
        // specialized features for aliases / appositive constructions
        fes.add(new FE_Alias());
        fes.add(new FE_Appositive());
        // string matching features
        fes.add(new FE_StringMatch());
        // fes.add(new FE_LeftRightMatch());
        // fes.add(new FE_HeadMatch());
        // semantic class agreement
        fes.add(new FE_SemanticClass());
        fes.add(new FE_SentenceDistance());
        return fes;
    }
    
    public static void main(String[] args) {
        try {
            InstanceWriter iw=
                    new WEKAInstanceWriter(
                        new FileWriter(
                            new File(
                                ConfigProperties.getInstance().getModelDir(),
                                ConfigProperties.getInstance().getTrainingDataSink()
                             )
                        )
                    );
            CorefTrainer trainer=new SoonEncoder(getExtractors(),iw);
            TrainerProcessor proc=new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            proc.createTrainingData();
            iw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
