/*
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.FE_Appositive;
import elkfed.coref.features.pairs.FE_Appositive_iCab;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.visualize.Page;
import elkfed.mmax.visualize.Page.LabelingFN;
import elkfed.mmax.visualize.Sentence;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author yannick
 */
public class ExtractApposition implements CorefTrainer {

    Page html_output = new Page();

    class MyLabelingFN extends LabelingFN {

        int status = 0;
        String[][] cls = {{"grey"}, {"red"}, {"blue"}, {"green"}};

        @Override
        public String[] getClasses(Markable m) {
            return cls[status];
        }
    };
    MyLabelingFN labeler = new MyLabelingFN();

    public Markable getSentence(Mention m) {
        Markable mm = m.getMarkable();
        MiniDiscourse doc = mm.getMarkableLevel().getDocument();
        MarkableLevel sentLevel = doc.getMarkableLevelByName("sentence");
        for (Markable ms : sentLevel.getMarkablesAtDiscoursePosition(mm.getLeftmostDiscoursePosition())) {
            return ms;
        }
        return null;
    }

    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (int i = 0; i < mentions.size(); i++) {
            Mention m_i = mentions.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Mention m_j = mentions.get(j);
                if (m_i.getSentId() == m_j.getSentId()) {
                    if (m_i.getMarkable().getLeftmostDiscoursePosition() -
                            m_j.getMarkable().getRightmostDiscoursePosition() > 3)
                        continue;
                    PairInstance inst = new PairInstance(m_i, m_j);
                    boolean is_coref = m_i.isCoreferent(m_j);
                    boolean is_apposition = FE_Appositive.getAppositive(inst) ||
                            FE_Appositive_iCab.getAppositive(inst);
                    if (is_coref || is_apposition) {
                        Markable sentMarkable = getSentence(m_i);
                        labeler.status = (is_apposition ? 1 : 0) +
                                (is_coref ? 2 : 0);
                        Sentence s = new Sentence(sentMarkable, labeler);
                        s.addMarkable(m_i.getMarkable());
                        s.addMarkable(m_j.getMarkable());
                        html_output.addSentence(s);
                    }
                } else {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            ExtractApposition trainer = new ExtractApposition();
            TrainerProcessor proc = new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory());
            proc.createTrainingData();
//            proc=new TrainerProcessor(
//                    trainer,
//                    ConfigProperties.getInstance().getTestData(),
//                    ConfigProperties.getInstance().getTestDataId(),
//                    ConfigProperties.getInstance().getMentionFactory()
//            );
//            proc.createTrainingData();
            trainer.html_output.writeHTML("diagnosticOutput/appositions.html");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
