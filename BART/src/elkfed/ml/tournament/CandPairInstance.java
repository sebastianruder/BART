/*
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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
package elkfed.ml.tournament;

import elkfed.ml.AbstractInstance;
import elkfed.ml.Instance;

/**
 *
 * @author versley
 */
public class CandPairInstance extends AbstractInstance {
    public final Instance inst1;
    public final Instance inst2;
    public CandPairInstance(Instance i1, Instance i2) {
        inst1=i1; inst2=i2;
    }
}
