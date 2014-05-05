/*
 * ExtractData.java
 *
 * Created on August 7, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.mentions.Mention;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.knowledge.SemanticTreeFeature;
import elkfed.knowledge.SemanticClass;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author yannick
 */
public class ListMentions implements CorefTrainer {
    PrintStream _os;
    ListMentions(PrintStream os) {
        _os=os;
    }
        
    public void encodeDocument(List<Mention> mentions)
    throws IOException {
        SemanticTreeFeature st=new SemanticTreeFeature();

//        _os.println("------------");
        for (int i=0; i<mentions.size(); i++) {
            Mention m_i=mentions.get(i);
/*
            _os.format("%-20s(%s) %s\n",m_i.getMarkableString(),
                    m_i.getMarkable().getID(),
                    m_i.getSetID());
*/
//            _os.format("%s\n",st.GetSemanticFeature(m_i));

            _os.format("------------- %s ---------\n",m_i.getMarkable().getID());
            _os.format("   %s\n",m_i.getMarkableString());
            System.out.format("------- %s -------- \n",m_i.getMarkableString());

            _os.format("  lowestNP: %s\n",m_i.getLowestNP().toString());
            _os.format("  highestNP: %s\n",m_i.getHighestNP().toString());

if (m_i.getMinParseTree()==null) {
            _os.format("  MinParse: NULL\n");
}else{
            _os.format("  MinParse: %s\n",m_i.getMinParseTree().toString());
}

if (m_i.getMinNPParseTree()==null) {
            _os.format("  MinNPParse: NULL\n");
}else{
            _os.format("  MinNPParse: %s\n",m_i.getMinNPParseTree().toString());
}
if (m_i.getMaxNPParseTree()==null) {
            _os.format("  MaxNPParse: NULL\n");
}else{
            _os.format("  MaxNPParse: %s\n",m_i.getMaxNPParseTree().toString());
}



            _os.format("  SEMFEATURE  %s\n",st.GetSemanticFeature(m_i));
            _os.format("  START     %d\n",m_i.getStartWord());
            _os.format("  END     %d\n",m_i.getEndWord());
            _os.format("  HEAD     %s\n",m_i.getHeadString());
            _os.format("  HLEMMA     %s\n",m_i.getHeadLemma());
            _os.format("  ETYPE     %s\n",m_i.getSemanticClass().toString());
            if (m_i.getNumber())
              _os.format("  NUMBER     SG\n");
            else
              _os.format("  NUMBER     PL\n");

            _os.format("  MID     %s\n",m_i.getMarkable().getAttributeValue("id"));
            if (m_i.isCoord()) _os.format("  COORD ");
            if (m_i.getProperName()) _os.format(" NE  ");
            if (m_i.isEnamex()) _os.format(" ENAMEX  ");
            if (m_i.getPronoun()) _os.format(" PRONOUN ");
            if (m_i.getDefinite()) _os.format("  DEFINITE ");
            if (m_i.getIndefinite()) _os.format("  INDEFINITE ");

            _os.format("\n");





//            _os.format("%s\n",st.GetSemanticFeature(m_i));
        
        }
        _os.flush();
    }
    
    public static void main(String[] args) {
        try {
            PrintStream out=new PrintStream(new FileOutputStream(
                    System.getProperty("elkfed.corpus","data")+".out"));
            ListMentions trainer=new ListMentions(out);
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
