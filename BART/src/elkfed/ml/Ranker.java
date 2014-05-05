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
package elkfed.ml;

import java.util.List;

/**
 *
 * @author versley
 */
@SuppressWarnings("unchecked")
public interface Ranker {
    public void setHeader(List<FeatureDescription> fds);
    public double getScore(final Instance inst);
    public <T extends Instance> T getHighestRanked(List<T> cands);
    public <T extends Instance> List<T> getRanking(List<T> cands);
}
