/*
 * Copyright 2007 EML Research
 * Copyright 2007 Project ELERFED
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


package elkfed.mmax.pipeline;

import elkfed.mmax.pipeline.taggers.Chunker;
import elkfed.mmax.pipeline.taggers.MorphoAnalyser;
import elkfed.mmax.pipeline.taggers.NER;
import elkfed.mmax.pipeline.taggers.POSTagger;
import java.util.List;
import java.util.Arrays;

/** The default pipeline. Includes a sentence detector, pos tagger,
 *  chunker, ner, etc.
 *
 * @author ponzo
 */
public class DefaultPipeline extends Pipeline {

    /** Returns a list of pipeline components to be executed in order on the
     * corpus.
     *
     * @return an List of pipeline components
     */
    protected List<PipelineComponent> createComponents() {
        return
            Arrays.asList(
                new PipelineComponent[]{
                    new SentenceDetector(),
                    new POSTagger(),
                    new MorphoAnalyser(),
                    new Chunker(),
                    new NER(),
                    new Merger(),
                    new CoordinationIdentifier(),
                    new CompoundIdentifier(),
                    new PossessiveIdentifier()
            }
        );
    }
    
}
