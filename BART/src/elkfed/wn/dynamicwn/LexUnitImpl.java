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

/**
 *
 * @author yannick
 */
public class LexUnitImpl {
    int _obj_id;
    SynsetImpl _synset;
    String _word;
    int _offset_in_word;
    int _offset_in_synset;

    LexUnitImpl(SynsetImpl synset, String word) {
        _synset=synset; _word=word;
    }

    String getWord() { return _word; }
    SynsetImpl getSynset() {return _synset; }
}
