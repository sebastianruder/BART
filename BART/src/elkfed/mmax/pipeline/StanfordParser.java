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

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import elkfed.mmax.DiscourseUtils;

/**
 * Uses the LexicalizedParser in stanford's nlp library
 *
 * @author jrsmith
 */
public class StanfordParser extends Parser 
{
    
    private LexicalizedParser lp;
    
    /** Creates a new instance of StanfordParser */
    public StanfordParser()
    {
        super();

        lp = LexicalizedParser.loadModel("./models/parser/wsjPCFG.ser.gz");
        lp.setOptionFlags(new String[]{"-maxLength", "200", "-retainTmpSubcategories"});
    }
    
    /** Adds a parse tree to forest for each sentence in the document */
    protected void annotateDocument()
    {
        String[][] sentences = null;
        try
        { sentences = DiscourseUtils.getSentenceTokens(currentDocument); }
        catch (Exception mmax2e)
        { mmax2e.printStackTrace(); }

        for (int sentence = 0; sentence < sentences.length; sentence++)
        {
        	List<Word> words = new ArrayList<Word>();
            String[] tempSent = new String[sentences[sentence].length];
            int i = 0;
            for (String tok : sentences[sentence])
            {
            	String s=tok.replaceAll("\\(", "-LRB-");
            	s=s.replaceAll("\\)", "-RRB-");
            	words.add(new Word(s));
            }
            Tree parse = (Tree) lp.apply(words);
            forest.add(normalizeTree(parse));
        }
    }
    
    public String normalizeTree(Tree t)
    { return normalizeTree(t, new StringBuffer()); }
    
    /**
    * Creates the printed form of a parse tree as a bracketed <code>String</code>
    *
    * @return String returns the <code>String</code>
    */
    public String normalizeTree(Tree t, StringBuffer sb) {
        sb.append("(");
        sb.append(t.label().toString());
        Tree[] daughterTrees = t.children();
        for (int i = 0; i < daughterTrees.length; i++) {
            sb.append(" ");
            normalizeTree(daughterTrees[i], sb);
        }
        return sb.append(")").toString();
    }
}
