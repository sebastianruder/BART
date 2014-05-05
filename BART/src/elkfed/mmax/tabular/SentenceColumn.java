/*
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

import elkfed.mmax.importer.Importer.Tag;
import elkfed.mmax.minidisc.IMarkable;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class SentenceColumn implements Column {
    protected String levelname;
    protected int column;

    public SentenceColumn(String lvl, int col) {
        levelname=lvl;
        column=col;
    }

    public List<Tag> read_column(String[][] columns) {
        String[] vals = columns[column];
        List<Tag> result = new ArrayList<Tag>();
        Tag last_tag = null;
        int orderid=0;
        for (int i = 0; i < vals.length; i++) {
            String val = vals[i];
            if (last_tag==null) {
                last_tag = new Tag();
                last_tag.start = i;
                last_tag.end = i;
                last_tag.tag = levelname;
                ++orderid;
                last_tag.attrs.put("orderid",""+orderid);
                result.add(last_tag);
            } else {
                last_tag.end = i;
            }
            if (val.equals("<eos>")) {
                last_tag=null;
            }
        }
        return result;
    }

    public void write_column(MiniDiscourse doc, String[][] columns) {
        List<Markable> markables=doc.getMarkableLevelByName(levelname)
                .getMarkables();
        String[] vals = columns[column];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = "-";
        }
        for (IMarkable m : markables) {
            vals[m.getRightmostDiscoursePosition()] = "<eos>";
        }
    }

    public int max_column() {
        return column;
    }

}
