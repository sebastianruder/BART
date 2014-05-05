/*
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
import static elkfed.mmax.MarkableLevels.DEFAULT_COREF_LEVEL;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.mmax.minidisc.Markable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


/**
 *
 * @author yannick
 */
public class ExtractCounts implements CorefTrainer {
    int n_matched_markables;
    int n_found_markables;
    int n_gold_markables;
    int n_pronouns;
    int n_matched_pronouns;
    int n_names;
    int n_matched_names;

    ExtractCounts() {
    }
    
    public boolean matchingSubstring(Mention m1, Mention m2)
    {
        String[] s1=m1.getHeadOrName().split(" ");
        String[] s2=m2.getHeadOrName().split(" ");
        for (int i=0; i< s1.length; i++)
        {
            for (int j=0; j< s2.length; j++)
            {
                if (s1[i].equalsIgnoreCase(s2[j]))
                    return true;
            }
        }
        return false;
    }
    
    public boolean hasSameHead(List<Mention> mentions, int anaphor)
    {
        Mention m_i=mentions.get(anaphor);
        for (int k=0;k<anaphor;k++)
        {
            if (matchingSubstring(m_i,mentions.get(k)))
            {
                 return true;
            }
        }
        return false;
    }
    
    public void encodeDocument(List<Mention> mentions)
    throws IOException {
        n_found_markables += mentions.size();
        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            boolean is_gold=(m_i.getSetID()!=null);
            if (is_gold) n_matched_markables++;
            if (m_i.getPronoun())
            {
                n_pronouns++;
                if (is_gold) n_matched_pronouns++;
            } else if (m_i.isEnamex()) {
                n_names++;
                if (is_gold) n_matched_names++;
            }
        }
        if (mentions.isEmpty()) {
            System.err.println("No markables generated for this document. Weird!");
        } else {
            List<Markable> goldM=
                    mentions.get(0).getDocument()
                        .getMarkableLevelByName(DEFAULT_COREF_LEVEL).getMarkables();
            n_gold_markables+=goldM.size();
        }
    }

    private static double make_percentage(double a, double b) {
        if (b==0.0) return 0.0;
        return 100.0*(a/b);
    }
    void print_statistics() {
        System.out.format("Mentions identified: %d/%d (%.1f%%)\n",
                n_matched_markables,n_gold_markables,
                make_percentage(n_matched_markables,n_gold_markables));
        System.out.format("Mentions generated: %d (%.1f%% overgeneration)\n",
                n_found_markables,
                make_percentage(n_found_markables-n_matched_markables,
                n_gold_markables));
        System.out.format("Pronouns: %d (%.1f%% gold)\n",
                n_pronouns,
                make_percentage(n_matched_pronouns,n_pronouns));
        System.out.format("Names: %d (%.1f%% gold)\n",
                n_names,
                make_percentage(n_matched_names,n_names));
    }
    
    public static void main(String[] args) {
        try {
            ExtractCounts trainer=new ExtractCounts();
            TrainerProcessor proc=new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
                    );
            proc.createTrainingData();
//            proc=new TrainerProcessor(
//                    trainer,
//                    ConfigProperties.getInstance().getTestData(),
//                    ConfigProperties.getInstance().getTestDataId(),
//                    ConfigProperties.getInstance().getMentionFactory()
//            );
//            proc.createTrainingData();
            trainer.print_statistics();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
