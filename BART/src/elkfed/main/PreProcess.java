/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
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
import elkfed.coref.CorefResolver;
import elkfed.coref.CorefTrainer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.AnnotationProcessor;
import elkfed.coref.processors.TrainerProcessor;
import java.util.List;
import java.util.Map;
import net.cscott.jutil.DisjointSet;

/**
 *
 * @author versley
 */
public class PreProcess {
    static class NullResolver implements CorefResolver, CorefTrainer {
        public DisjointSet<Mention> decodeDocument(List<Mention> mentions,
                Map<Mention,Mention> antecedents) {
            return new DisjointSet<Mention>(); }
        public void printStatistics() {}
        public void encodeDocument(List<Mention> mentions) { }
    }
    
    public static void main(String[] args) {
        try {
            // don't do anything real, but run with preprocessing
            TrainerProcessor proc=new TrainerProcessor(
                    new NullResolver(),
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            proc.createTrainingData(true);
            AnnotationProcessor cap=new AnnotationProcessor(
                    new NullResolver(),
                    ConfigProperties.getInstance().getTestData(),
                    ConfigProperties.getInstance().getTestDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            cap.processCorpus(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
