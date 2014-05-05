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

import java.util.List;
import elkfed.mmax.Corpus;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.Set;

/** An abstract pipeline.  Instantiatons of this class will define the 
 * PipelineComponents which will be executed in the pipeline.
 *
 * @author ajern
 */
public abstract class Pipeline {

    protected Corpus data;
    
    private final List<PipelineComponent> components;
    
    /** Create a new Pipeline with a provided corpus */
    public Pipeline() {
        this.components = createComponents();
    }
    
    /* Execute a list of pipeline components */
    public void annotateData() {
        for (PipelineComponent c : components)  {
            for (MiniDiscourse doc: data) {
                c.annotate(doc);
            }
        }
    }

    public void checkLevels(Set<String> processedLevels, Set<String> goldLevels) {
        for (PipelineComponent c: components) {
            c.checkLevels(processedLevels, goldLevels);
        }
    }
    
    /** Gets the data <b>this</b> operates on */
    public Corpus getData()
    { return this.data; }

    /** Sets the data <b>this</b> operates on */
    public void setData(Corpus data)
    { this.data = data; }
    
    /** Returns a List of PipelineComponents which will be
     * executed in order to annotate a corpus.
     */
    protected abstract List<PipelineComponent> createComponents();
    
}
