/*
 *  Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

package elkfed.lang;

/**
 *
 * @author yannick
 */
public enum NodeCategory {
    NP(true), CN(true), PN(true), PRO(true),
    PP,PREP,
    S(false,true), VP(false,true),
    DT, DT2, ADJ, ADV, CC, PUNCT, OTHER;
    //DT2 are determiners and predeterminers that
    // should be treated more like premodifiers (e.g, [his] car, la [sua] macchina)
    NodeCategory(boolean isNom, boolean isSent) {
        _isNominal=isNom;
        _isSentential=isSent;
    }
    NodeCategory(boolean isNom) {
        _isNominal=isNom;
        _isSentential=false;
    }
    NodeCategory() {
        _isNominal=false;
        _isSentential=false;
    }
    final private boolean _isNominal;
    final private boolean _isSentential;
    public boolean isNominal() { return _isNominal; }
    public boolean isSentential() { return _isSentential; }
}
