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
 * similar to split2, but nod discardting for nonpro-pro
 * @author olga
 */
public class Splitting2b implements Splitting {

    public int getInstanceType(Mention m_i, Mention m_j) {
        int type;
        if (m_i.getPronoun()) 
            return 0;
        return 1;
     }
    

}
