/*
 * ExtractData.java
 *
 * Created on August 7, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import edu.stanford.nlp.trees.Tree;
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
public class DumpMentions implements CorefTrainer {
    PrintStream _os;
    DumpMentions(PrintStream os) {
        _os=os;
    }
        
    public void encodeDocument(List<Mention> mentions)
    throws IOException {
        _os.println("------------");
        for (int i=1; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
            Markable m=m_i.getMarkable();
            StringBuffer buf=new StringBuffer();
            for (String s: m_i.getLeftContext(1)) {
                buf.append(s).append(" ");
            }
            buf.append(m.toString());
            for (String s: m_i.getRightContext(1)) {
                buf.append(" ").append(s);
            }
            _os.format("%-20s(%s) %s\n",
                    buf.toString(),
                    m_i.getMarkable().getID(),
                    m_i.getSetID());
            _os.format("Features: %s\n",m_i.getFeatures());
            _os.format("headOrName: %s\n",m_i.getHeadOrName());
            _os.format("markableString: %s\n",m_i.getMarkableString());
            _os.format("Premodifiers:\n");
            try {
                for (Tree n: m_i.getPremodifiers()) {
                    _os.format("\t%s\n",n.toString());
                }
                _os.format("Postmodifiers:\n");
                for (Tree n: m_i.getPostmodifiers()) {
                    _os.format("\t%s\n",n.toString());
                }
            } catch (NullPointerException ex) {
                _os.format("(Null)\n");
            }
            _os.format("Markable attributes:\n");
            for (String k: m.getAttributes().keySet()) {
                _os.format("\t%s=%s\n", k, m.getAttributeValue(k));
            }
            _os.println();
        }
        _os.flush();
    }
    
    public static void main(String[] args) {
        try {
            PrintStream out=new PrintStream(new FileOutputStream(
                    System.getProperty("elkfed.corpus","data")+"-dump.out"),
                    true, "ISO-8859-15");
            DumpMentions trainer=new DumpMentions(out);
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
}
