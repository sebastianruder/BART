/*
 * AnnotationProcessor.java
 *
 * Created on July 18, 2007, 6:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.processors;

import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.DefaultMentionFactory;
import elkfed.coref.mentions.MentionFactory;
import elkfed.coref.CorefResolver;
import elkfed.coref.mentions.Mention;
import elkfed.coref.eval.MUCScorer;
import elkfed.coref.eval.CEAFScorer;
import elkfed.coref.eval.CEAFAggrScorer;
import elkfed.coref.util.Clustering;
import elkfed.mmax.Corpus;
import elkfed.mmax.CorpusFactory;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.Pipeline;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.cscott.jutil.DisjointSet;

/** A class for processing and annotating test data
 *
 * @author ponzetsp
 */
public class AnnotationProcessor {
    
    /** The logger */
    protected static final Logger PROCESSOR_LOGGER = Logger.getAnonymousLogger();
    
    /** The test data dir */
    protected final File _dataDir;
    
    /** The id of the corpus data */
    protected final String _dataId;
    
    /** The CorefResolver used */
    protected final CorefResolver _decoder;
    
    /** the MentionFactory used */
    protected final MentionFactory _mfact;
    
    /** constructor.
     *  is given the <i>CorefTrainer</i> actually used and the
     *  directory where the training data is to be found */
    public AnnotationProcessor(CorefResolver decoder, File testDataDir, String dataId) {
        this(decoder, testDataDir, dataId, new DefaultMentionFactory());
    }
    
    public AnnotationProcessor(CorefResolver decoder, File dataDir, String dataId,
            MentionFactory mfact) {
        _decoder=decoder;
        _dataDir=dataDir;
        _dataId=dataId;
        _mfact=mfact;
    }
    
    /** Processes a training corpus -
     * default is not to run the preprocessing pipeline
     */
    public void processCorpus() throws IOException {
        processCorpus(false); }
    
    /** Processes a training corpus */
    public void processCorpus(boolean withPipeline) throws IOException {
        
        Corpus testData = null;
        Pipeline pipeline=null;
        if (withPipeline) {
            pipeline = ConfigProperties.getInstance().getPipeline();
        }
        
        if (_dataDir==null || !_dataDir.exists())
        { throw new RuntimeException("_dataDir "+_dataDir+" not found!!!"); }
        
        int docNumber = 0;
        File[] files=_dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE);
        Arrays.sort(files);
        // we work doc by doc to save memory...
        for (File mmaxFile : files)
        {
            // 0. first load the data
            PROCESSOR_LOGGER.info("Loading "+mmaxFile.getName()+" ["+docNumber+"]");

            testData = CorpusFactory.getInstance().createCorpus(mmaxFile, _dataId);
            
            if (withPipeline) {                
                // clean markable levels!
                for (MiniDiscourse doc : testData) {
                    DiscourseUtils.deleteMarkableLevels(doc,pipeline);
                    DiscourseUtils.deleteResponses(doc);
                }
                
                // 1. then pre process, create markables and create instances
                pipeline.setData(testData);
                PROCESSOR_LOGGER.info("Creating markables (TEST DATA): please wait...");
                pipeline.annotateData();
            }
            else
            {
                // simply remove previous responses
                for (MiniDiscourse doc : testData) {
                    DiscourseUtils.deleteResponses(doc);
                }
            }

            PROCESSOR_LOGGER.info("Annotating files");
            // iteration is syntactic sugar:
            // we know that we just one doc at this time...
            for (MiniDiscourse doc : testData) {
                List<Mention> mentions=
                        _mfact.extractMentions(doc);
                Map<Mention,Mention> antecedents=new HashMap<Mention,Mention>();
                DisjointSet<Mention> partition=
                          _decoder.decodeDocument(mentions,antecedents);
                Clustering.addClustersToMMAX(partition,antecedents,doc);

            }
/*            
            PROCESSOR_LOGGER.info("Scoring files (MUC)");
            MUCScorer.getInstance().computeScores(mmaxFile);
            PROCESSOR_LOGGER.info("Scoring files (CEAF)");
            CEAFScorer.getInstance().computeScores(mmaxFile);
            PROCESSOR_LOGGER.info("Scoring files (CEAF-AGGR)");
            CEAFAggrScorer.getInstance().computeScores(mmaxFile);
*/            
            docNumber++;
        }
    }
    public void annotateFold(int fold, int nFolds) throws IOException {
        fold=fold%nFolds;
        if (_dataDir==null || !_dataDir.exists())
        { throw new RuntimeException("_dataDir not found!!!"); }

        int docNumber = 0;
        File[] files=_dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE);
        Arrays.sort(files);
        for (File mmaxFile : files)
        {
            if (docNumber%nFolds==fold) {
                PROCESSOR_LOGGER.info("Annotating " + mmaxFile.getName()+" ["+docNumber+"]");
                MiniDiscourse doc=CorpusFactory.docFromFile(mmaxFile);
                DiscourseUtils.deleteResponses(doc);
                List<Mention> mentions=
                        _mfact.extractMentions(doc);
                Map<Mention,Mention> antecedents=new HashMap<Mention,Mention>();
                DisjointSet<Mention> partition=
                          _decoder.decodeDocument(mentions,antecedents);
                Clustering.addClustersToMMAX(partition,antecedents,doc);
            }
            docNumber++;
        }
    }

    /** Scores a corpus using the MUC scorer */
    public static void scoreMUC(File corpusDir)
    { MUCScorer.getInstance().computeScores(corpusDir); }
    
    /** Scores a corpus using the CEAF scorer */
     public static void scoreCEAF(File corpusDir)
    { CEAFScorer.getInstance().computeScores(corpusDir); }

     public static void scoreCEAFAggr(File corpusDir, CEAFAggrScorer.Metric metric)
    {
         try {
            CEAFAggrScorer.getInstance().setMetric(metric).computeScores(corpusDir);
         } catch (UnsatisfiedLinkError ex) {
             System.out.println("lp_solve not installed correctly. CEAF scoring skipped.");
         }
     }
}
