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

import elkfed.wn.RelationType;

/**
 *
 * @author yannick
 */
public class Relation {
    public final SynsetImpl _from;
    public final SynsetImpl _to;
    public final RelationType _type;
    public Relation(SynsetImpl from,
            SynsetImpl to,
            RelationType type) {
        _from=from; _to=to; _type=type;
    }
}
