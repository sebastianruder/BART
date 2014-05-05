/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
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
package elkfed.expletives;

import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;
import elkfed.ml.AbstractInstance;

/**
 *
 * @author versley
 */
public class ExpletiveInstance extends AbstractInstance {
    private Tree _root;
    private Tree _pronoun;
    private String _id;
    
    private String[] _words;
    private String[] _pos;
    private int _idx=-1;
    
    public ExpletiveInstance(Tree root, Tree pronoun, String id) {
        _root=root;
        _pronoun=pronoun;
        _id=id;
        
        List<Tree> wordsT = root.getLeaves();
        List<Label> posT = root.preTerminalYield();
        // get words and POS into an array so that
        // we get an idea of the pronoun's surrounding
        String[] words = new String[wordsT.size()];
        String[] pos = new String[wordsT.size()];
        if (!root.dominates(pronoun)) {
            System.err.format("%s does not dominate %s. WTF?", root, pronoun);
        }
        for (int here=0; here<wordsT.size(); here++) {
            Tree w1=wordsT.get(here);
            Label p1=posT.get(here);
            words[here] = w1.toString();
            pos[here] = p1.value();
            if (w1 == pronoun) {
                _idx = here;
            } else if (pronoun.dominates(w1)) {
                _idx=here;
                pronoun=w1;
            }
        }
        assert _idx>=0 : String.format("wanted %s in %s",pronoun,root);
        assert pos[_idx].equals("PRP"):String.format("wanted PRP got '%s'",pos[_idx]);
        _words=words;
        _pos=pos;
    }

    public String getId() {
        return _id;
    }

    public void setIdx(String id) {
        this._id = id;
    }

    public Tree getRoot() {
        return _root;
    }

    public void setRoot(Tree root) {
        this._root = root;
    }

    public Tree getPronoun() {
        return _pronoun;
    }

    public void setPronoun(Tree pronoun) {
        this._pronoun = pronoun;
    }

    public String[] getWords() {
        return _words;
    }

    public String[] getPOS() {
        return _pos;
    }

    public int getIdx() {
        return _idx;
    }
    
}
