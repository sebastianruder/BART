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
package elkfed.coref.algorithms.soon.split;

import elkfed.coref.mentions.Mention;

/**
 * splits the decisions 4-way:
 * 0 = same-sentence pronoun (normal or cataphoric)
 * 1 = non-same-sentence pronoun
 * 2 = same-sentence non-pronoun (apposition/copula)
 * 3 = non-same-sentence non-pronoun
 * @author versley
 */
public class Splitting4 implements Splitting {

    public int getInstanceType(Mention m_i, Mention m_j) {
        if (m_i.getPronoun()) {
            if (m_i.getSentId()==m_j.getSentId()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (m_i.getSentId()==m_j.getSentId()) {
                if (m_j.getPronoun()) {
                    return 0;
                } else {
                    return 2;
                }
            } else if (m_j.getPronoun()) {
                return -1;
            } else {
                return 3;
            }
        }
    }

}
