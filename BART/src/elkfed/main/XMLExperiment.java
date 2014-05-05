/*
 * XMLExperiment.java
 *
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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
import elkfed.main.xml.CorefExperimentDocument;
import java.io.FileInputStream;

/**
 * runs a whole experiment including training, classifier building,
 * testing and quantitative evalutation on a corpus.
 * @author yannick
 */
public class XMLExperiment {

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
            if ("stacked".equalsIgnoreCase(doc.getCorefExperiment().getSystem().getType()))
            {
                System.err.println("---> Creating first-stage classifier <---");
                XMLRankingTrainer t=new XMLRankingTrainer(doc.getCorefExperiment());
                t.run();
                t.do_estimation();
            }
            System.err.println("---> Training instance creation <---");
            new XMLTrainer(doc.getCorefExperiment()).run();
            System.err.println("---> Building classifiers <---");
            XMLClassifierBuilder.buildClassifiers(doc);
            System.err.println("---> Testing <---");
            new XMLAnnotator(doc.getCorefExperiment()).run();
            //TODO: run evaluation
        }
        catch (Exception e)
        {
            e.printStackTrace();
            java.lang.System.exit(1);
        }
    }  
}
