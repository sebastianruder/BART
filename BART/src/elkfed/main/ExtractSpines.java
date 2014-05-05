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

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.lang.MentionType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author yannick
 */
public class ExtractSpines implements CorefTrainer {
    PrintWriter _out;
    ExtractSpines(PrintWriter out)
        throws IOException
    {
        _out=out;
    }
    
    public void encodeDocument(List<Mention> mentions)
    throws IOException {
        Set<String> seenNominals=new HashSet<String>();
        Set<String> seenNames=new HashSet<String>();
        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            if (m_i.mentionType()
                    .features.contains(MentionType.Features.isNominal))
            {
                String hd=m_i.getHeadString();
                if (seenNominals.contains(hd)) {
                    continue;
                }
                extract_nominal(hd,mentions);
                seenNominals.add(hd);
            } else if (m_i.isEnamex()) {
                String[] name_parts=m_i.getHeadOrName().split(" ");
                String best_name=null;
                int best_fom=-1;
                for (int j=0; j<name_parts.length; j++) {
                    if (name_parts[j].length()>best_fom) {
                        best_name=name_parts[j];
                        best_fom=name_parts[j].length();
                    }
                }
                if (seenNames.contains(best_name)) {
                    continue;
                }
                extract_name(best_name,mentions);
                seenNames.add(best_name);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            String corpusName=System.getProperty("elkfed.corpus",
                    "default");
            PrintWriter out=new PrintWriter("spines-"+
                    corpusName+".txt");
            ExtractSpines trainer=new ExtractSpines(out);
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
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void extract_name(String best_name, List<Mention> mentions) {
        List<Mention> spine=new ArrayList<Mention>();
        for (int i=0; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            if (m_i.isEnamex() &&
                    m_i.getHeadOrName().contains(best_name)) {
                spine.add(m_i);
            }
        }
        if (spine.size()>=2) {
            _out.format("## NAM %s\n", best_name);
            for (Mention m: spine) {
                            _out.format("%s\t%s\n",
                        m.getSetID(),
                        m.getMarkableString());
            }
        }
    }

    private void extract_nominal(String hd, List<Mention> mentions) {
        List<Mention> spine=new ArrayList<Mention>();
        for (int i=0; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            if (m_i.mentionType().features.contains(
                    MentionType.Features.isNominal) &&
                    m_i.getHeadString().equals(hd)) {
                spine.add(m_i);
            }
        }
        if (spine.size()>=2) {
            _out.format("## NOM %s\n", hd);
            for (Mention m: spine) {
                            _out.format("%s\t%s\n",
                        m.getSetID(),
                        m.getHighestProjection());
            }
        }
    }
}
