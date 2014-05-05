/*
 * XMLCrossValidate.java
 *
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
import elkfed.coref.eval.CEAFAggrScorer;
import elkfed.coref.eval.MUCScorer;
import elkfed.coref.eval.Score;
import elkfed.coref.eval.Scorer;
import elkfed.main.xml.CorefExperimentDocument;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * runs a whole experiment including training, classifier building,
 * testing and quantitative evalutation on a corpus.
 * @author yannick
 */
public class XMLCrossValidate {
    public static final int N_FOLDS=5;

    private static void runFolds(CorefExperimentDocument doc) throws IOException, ClassNotFoundException {
       for (int fold=0; fold<N_FOLDS; fold++) {
            System.err.println("---> Training instance creation (fold "+fold+") <---");
            new XMLTrainer(doc.getCorefExperiment()).runFold(fold, N_FOLDS);
            System.err.println("---> Building classifiers (fold "+fold+") <---");
            XMLClassifierBuilder.buildClassifiers(doc);
            System.err.println("---> Annotating (fold "+fold+") <---");
            new XMLAnnotator(doc.getCorefExperiment()).runFold(fold,N_FOLDS);
        }
    }
    
    public static List<Score> run(CorefExperimentDocument doc, Scorer scorer)
            throws IOException, ClassNotFoundException
    {
        runFolds(doc);
        return scorer.computeScores(ConfigProperties.getInstance().getTrainingData());
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
            runFolds(doc);
            List<Score> result;
            result = MUCScorer.getInstance().computeScores(ConfigProperties.getInstance().getTrainingData());
            System.err.format("FOM: %f\n",result.get(result.size()-1).getScore());
            result = CEAFAggrScorer.getInstance().setMetric(CEAFAggrScorer.Metric.PHI3).computeScores(ConfigProperties.getInstance().getTrainingData());
            System.err.format("FOM: %f\n",result.get(result.size()-1).getScore());
            result = CEAFAggrScorer.getInstance().setMetric(CEAFAggrScorer.Metric.PHI4).computeScores(ConfigProperties.getInstance().getTrainingData());
            System.err.format("FOM: %f\n",result.get(result.size()-1).getScore());
          }
        catch (Exception e)
        {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }  
}
