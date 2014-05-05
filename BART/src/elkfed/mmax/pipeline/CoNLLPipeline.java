/*
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

import elkfed.config.ConfigProperties;
import elkfed.mmax.pipeline.taggers.MorphoAnalyser;
import elkfed.mmax.pipeline.taggers.NER;
import java.util.Arrays;
import java.util.List;

/**
* pipeline for CoNLL: uses precompiled pos and parse, creates chunks
 */
public class CoNLLPipeline extends Pipeline {
    
    /** Returns a list of pipeline components to be executed in order on the
     * corpus.
     *
     * @return an List of pipeline components
     */
    protected List<PipelineComponent> createComponents() {
        return
            Arrays.asList(
                new PipelineComponent[]{
//     new SentenceDetector(), // sentences provided in the original data
                    new P2Chunker(), //parses provided, only chunks needed
                    new MorphoAnalyser(),
                    new NER(),
                    new Merger(),


//                    new CoordinationIdentifier(), //from "A and B" adds "A", "B" as markables
//                    new CompoundIdentifier(), //from "N1 N2" adds N1 as markable -- think twoce before using this!
                    new PossessiveIdentifier() //from "his A" adds "his" as markable

            }
        );
    }
}
