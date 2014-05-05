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
  * 0 = ana is pro12
 * 1 = ana is pro3
 * 2 = ana is name
* 3 = ana is nominal/misc
* @author olga
 */
public class Splitting3a implements Splitting {

    public int getInstanceType(Mention m_i, Mention m_j) {
        int type;
        if (m_i.getPronoun()) {
           if (m_i.getIsFirstSecondPerson()) 
             return 0;
           return 1;
        } 
        if (m_i.getProperName())
          return 2;
        return 3;
     
    }

}
