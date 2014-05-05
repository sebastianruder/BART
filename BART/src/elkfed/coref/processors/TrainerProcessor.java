/*
 * Copyright 2007 EML Research
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

package elkfed.coref.processors;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.mentions.DefaultMentionFactory;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;
import elkfed.mmax.Corpus;
import elkfed.mmax.CorpusFactory;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.Pipeline;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/** A class for processing training data
 *
 * @author ponzetsp
 */
public class TrainerProcessor {
    
    /** The logger */
    protected static final Logger PROCESSOR_LOGGER = Logger.getAnonymousLogger();
    
    /** The training data dir */
    protected final File _dataDir;
    
    /** The id of the corpus data */
    protected final String _dataId;
    
    /** The  CorefTrainer used */
    protected final CorefTrainer _encoder;
    
    /** the MentionFactory used */
    protected final MentionFactory _mfact;
    
    /** constructor.
     *  is given the <i>CorefTrainer</i> actually used and the
     *  directory where the training data is to be found */
    public TrainerProcessor(CorefTrainer encoder, File dataDir, String dataId) {
        this(encoder, dataDir, dataId, new DefaultMentionFactory());
    }
    
    public TrainerProcessor(CorefTrainer encoder, File dataDir, String dataId,
            MentionFactory mfact) {
        _encoder=encoder;
        _dataDir=dataDir;
        _dataId=dataId;
        _mfact=mfact;
    }
    
    /** Processes a training corpus -
     * default is not to run the preprocessing pipeline
     */
    public void createTrainingData() throws IOException {
        createTrainingData(false);
    }
    
    /** Processes a training corpus */
    public void createTrainingData(boolean withPipeline) throws IOException
    {
        Corpus trainingData = null;
        Pipeline pipeline=null;
        
        if (_dataDir==null || !_dataDir.exists())
        { throw new RuntimeException("_dataDir not found!!!"); }
        
        if (withPipeline) {
            pipeline = ConfigProperties.getInstance().getPipeline();
        }
        int docNumber = 0;
        File[] files=_dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE);
        Arrays.sort(files);
        // we work doc by doc to save memory...
        for (File mmaxFile : files)
        {    
            // 0. first load the data
            PROCESSOR_LOGGER.info("Loading " + mmaxFile.getName()+" ["+docNumber+"]");

            trainingData = CorpusFactory.getInstance().createCorpus(mmaxFile, _dataId);
            
            if (withPipeline) {
                
                // clean all non-gold markable levels!
                for (MiniDiscourse doc : trainingData) {
                    DiscourseUtils.deleteMarkableLevels(doc,pipeline);
                }
                
                // 1. then pre process, create markables and create instances
                pipeline.setData(trainingData);
                PROCESSOR_LOGGER.info("Creating markables (TRAINING DATA): please wait...");
                pipeline.annotateData();
            }
            
            PROCESSOR_LOGGER.info("Generating training instances");
            // iteration is syntactic sugar:
            // we know that we just one doc at this time...
            for (MiniDiscourse doc : trainingData) {
                List<Mention> mentions=
                        _mfact.extractMentions(doc);
                _encoder.encodeDocument(mentions);
            }
            
            docNumber++;
        }
    }

    public void createTrainingFold(int fold, int nFolds) throws IOException
    {
        int docNumber = 0;
        fold=fold%nFolds;
        if (_dataDir==null || !_dataDir.exists())
        { throw new RuntimeException("_dataDir not found!!!"); }
        File[] files=_dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE);
        Arrays.sort(files);
        for (File mmaxFile : files)
        {
            if (docNumber%nFolds!=fold) {
                PROCESSOR_LOGGER.info("Loading " + mmaxFile.getName()+" ["+docNumber+"]");
                MiniDiscourse doc=CorpusFactory.docFromFile(mmaxFile);
                List<Mention> mentions=_mfact.extractMentions(doc);
                _encoder.encodeDocument(mentions);
            }
            docNumber++;
        }
    }
}
