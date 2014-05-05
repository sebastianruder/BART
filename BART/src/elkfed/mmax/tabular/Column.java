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

package elkfed.mmax.tabular;

import elkfed.mmax.importer.Importer;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.List;

/**
 *
 * @author yannick
 */
public interface Column {
    List<Importer.Tag> read_column(String[][] columns);
    void write_column(MiniDiscourse doc,
            String[][] columns);
    int max_column();
}
