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
 *
 * @author samuel
 *
 * pre filter relative pronouns and reflexive pronouns that do not reside
 * in the same sentence
 *
 * -1:  discard
 *  0:  i is pronoun (w/o refl. and rel. pronouns j not in the same sentence)
 *  1:  i and j are non pronouns except j that are (possible cataphoric) reflexive pronouns
 *
 */
public class Splitting2Filtered implements Splitting {

    public int getInstanceType(Mention m_i, Mention m_j) {
        if (m_i.getPronoun()) {
            if((m_j.getRelPronoun() || m_j.getReflPronoun()) && m_i.getSentId() != m_j.getSentId()) {
                return -1;
            } else {
                return 0;
            }
        } else {
            //possible cataphoric reflexiv pronouns
            if(m_j.getReflPronoun() && m_i.getSentId() == m_j.getSentId()) {
                return 0;
            } else if (m_j.getPronoun()) {
                return -1;
            }
            return 1;
        }
    }

}
