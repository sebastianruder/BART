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
package elkfed.expletives;

import edu.stanford.nlp.trees.BobChrisTreeNormalizer;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.crash.FE_Expletive;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.InstanceWriter;
import elkfed.ml.svm.SVMLightInstanceWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author versley
 */
public class TrainingData {
    public static void extractExamples(String file,
            Set<String> anaphoricPronouns, List<ExpletiveInstance> instances)
            throws FileNotFoundException, IOException {
        TreeReader tr = new PennTreeReader(new FileReader(file),
                new LabeledScoredTreeFactory(),
                new BobChrisTreeNormalizer());
        Tree t;
        String file_id = file.substring(file.length() - 8, file.length() - 4);
        int sent_idx = 1;
        while ((t = tr.readTree()) != null) {
            //t.pennPrint();
            int word_idx = 1;
            for (Tree t1 : t.getLeaves()) {
                String s = t1.toString();
                if ("it".equals(s) || "It".equals(s)) {
                    String id = String.format("%s:S%d:%d-%d",
                            file_id, sent_idx, word_idx, word_idx);
                    ExpletiveInstance inst=new ExpletiveInstance(t, t1, id);
                    boolean is_positive=anaphoricPronouns.contains(id);
                    inst.setFeature(PairInstance.FD_POSITIVE, !is_positive);
                    instances.add(inst);
                    String cls=is_positive? "+1": "-1";
                    System.out.format("%s\t%s\t(%s)\n",
                            s, id, cls);
                }
                word_idx++;
            }
            //System.out.println();
            //System.out.println(t);
            sent_idx++;
        }

    }

    public static Set<String> readAnaphoric(String fname)
            throws FileNotFoundException, IOException {
        Set<String> result = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        while ((line = br.readLine()) != null) {
            result.add(line);
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            Set<String> anaphoric = readAnaphoric("/space/versley/pron.txt");
            List<ExpletiveInstance> data = new ArrayList<ExpletiveInstance>();
            for (String arg : args) {
                extractExamples(arg, anaphoric, data);
            }
            InstanceWriter iw=new SVMLightInstanceWriter(new FileWriter("expletive.data"),
                    new File("expletive.dict"));
            List<FeatureDescription> fds=new ArrayList<FeatureDescription>();
            List<FeatureExtractor<ExpletiveInstance>> fes=FE_Expletive.getExtractors();
            for (FeatureExtractor ef: fes) {
                ef.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            iw.setHeader(fds);
            for (ExpletiveInstance inst: data) {
                for (FeatureExtractor ef: fes) {
                    ef.extractFeatures(inst);
                }
                iw.write(inst);
            }
            iw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
