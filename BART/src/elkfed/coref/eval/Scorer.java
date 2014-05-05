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

import elkfed.mmax.Corpus;
import elkfed.mmax.CorpusFactory;
import java.io.File;
import java.util.List;

/** A scorer calculates performance measures from a corpus,
 *  i.e. precision and recall
 *
 * @author ponzetsp
 */
public abstract class Scorer
{
    /** The corpus to score */
    protected Corpus corpus;

    /** Computes the score from a Corpus */
    public List<Score> computeScores(File corpus)
    { return computeScores(CorpusFactory.getInstance().createCorpus(corpus)); }
    
    /** Computes the score from a Corpus */
    public List<Score> computeScores(Corpus corpus)
    {
        setCorpus(corpus);
        return computeScores();
    }
    
    /** Computes the score from a Corpus */
    public abstract List<Score> computeScores();
    
    /** Sets the corpus to score */
    public void setCorpus(Corpus corpus)
    { this.corpus = corpus; }

    
}
