/*
 * Copyright 2009 Yannick Versley / Univ. Tuebingen
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

package elkfed.wn.dynamicwn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yannick
 */
public class WordnetImpl {
    final List<SynsetImpl> _synsets=new ArrayList<SynsetImpl>();
    final List<LexUnitImpl> _lexUnits=new ArrayList<LexUnitImpl>();
    
    //auxiliary indices
    final Map<String,List<LexUnitImpl>> lexunits_by_word
            =new HashMap<String,List<LexUnitImpl>>();

    Iterable<LexUnitImpl> lexUnitsForWord(String word) {
        return lexunits_by_word.get(word);
    }

    void addSynset(SynsetImpl s) {
        s._obj_id=_synsets.size();
        _synsets.add(s);
    }

    void addLexUnit(LexUnitImpl lu) {
        SynsetImpl s=lu._synset;
        lu._offset_in_synset=s._lexUnits.size();
        s._lexUnits.add(lu);
        List<LexUnitImpl> lus=lexunits_by_word.get(lu.getWord());
        if (lus==null) {
            lus=new ArrayList<LexUnitImpl>();
            lexunits_by_word.put(lu.getWord(),lus);
        }
        lus.add(lu);
    }

    void addSynsetRelation(Relation rel) {
        rel._from._rels.add(rel);
        rel._to._invRels.add(rel);
    }
}
