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

package elkfed.coref.eval;

import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.List;

/** A MarkableSet-like structure which contains also a pointer to
 *  the MMAX2Discourse this MarkableSet comes from
 *
 * @author ponzetsp
 */
public class EvaluationMarkableSet
{    
    /** The MarkableSet itself */
    private List<Markable> markableSet;

    /** The MMAX2Discourse it comes from */
    private MiniDiscourse document;
    
    /** Creates a new instance of EvaluationMarkableSet */
    public EvaluationMarkableSet(List<Markable> markableSet, MiniDiscourse document)
    {
        this.markableSet = markableSet;
        this.document = document;
    }
    
    /** Gets the MarkableSet */
    public List<Markable> getMarkableSet()
    { return this.markableSet; }
    
    /** Gets the MMAX2Discourse the MarkableSet comes from */
    public MiniDiscourse getDocument()
    { return this.document; }
    
}
