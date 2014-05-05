/*
 * Copyright 2007 Project ELERFED
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

package elkfed.coref.mentions;

import elkfed.mmax.minidisc.Markable;

/** Given a MMAX2Discourse, extracts all of its mentions from 
 *  the entire document and returns them in a List.
 *
 * @author ponzo
 */
public class FullDocMentionFactory extends AbstractMentionFactory {

    /** By default, we keep all markables */
    protected boolean keepMarkable(Markable markable)
    { return true; }
}
