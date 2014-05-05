/*
 *  Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.util.CorefDocuments;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author yannick
 */
public class ExtractSemClass implements CorefTrainer{
    PrintStream _out;
    
    public ExtractSemClass(PrintStream out) {
        _out=out;
    }

    public void encodeDocument(List<Mention> mentions) throws IOException {
        for (Mention m: mentions) {
            if (!m.getPronoun()) {
                Markable msys=m.getMarkable();
                MiniDiscourse doc=msys.getMarkableLevel().getDocument();                       
                Markable mgold = CorefDocuments.getInstance()
                        .markableIsaCorefElement(doc,msys);
                if (mgold!=null) {
                    String semclass=mgold.getAttributeValue("sem_class");
                    if (semclass!=null) {
                        _out.format("%s\t%s\n", m.getHeadOrName(),
                                semclass);
                    }
                }
            }
        }
    }


    
    public static void main(String[] args) {
        try {
            PrintStream out=new PrintStream(new FileOutputStream(
                    System.getProperty("elkfed.corpus","data")+"-semcls.out"),
                    true,"ISO-8859-15");
            ExtractSemClass trainer=new ExtractSemClass(out);
            TrainerProcessor proc=new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
                    );
            proc.createTrainingData();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
