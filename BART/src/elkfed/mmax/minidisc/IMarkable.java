/*
 *  Copyright 2008 Yannick Versley / Univ. Tuebingen
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

package elkfed.mmax.minidisc;

/** abstract interface to markable-like things
 *
 * @author yannick
 */
public interface IMarkable {

    void adjustSpan(int new_start, int new_end);

    String getAttributeValue(String key);
    
    int getLeftmostDiscoursePosition();

    int getRightmostDiscoursePosition();

    void setAttributeValue(String key, String val);

}
