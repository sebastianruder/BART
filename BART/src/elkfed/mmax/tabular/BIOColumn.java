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

import elkfed.mmax.importer.Importer.Tag;
import elkfed.mmax.minidisc.IMarkable;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.ArrayList;
import java.util.List;

/** maps BIO-style span annotation from column format
 * to spans
 *
 * @author yannick
 */
public class BIOColumn implements Column {
    protected String levelname;
    protected String attribute;
    protected int column;
    private boolean allow_textpro_bugs;

    public BIOColumn(String lvl, String att, int col) {
        levelname=lvl;
        attribute=att;
        column=col;
        allow_textpro_bugs=true;
    }

    public List<Tag> read_column(String[][] columns) {
        String[] vals = columns[column];
        List<Tag> result = new ArrayList<Tag>();
        Tag last_tag = null;
        for (int i = 0; i < vals.length; i++) {
            String val = vals[i];
            // ignore TextPro's @confidence values
            if (val.contains("@")) {
                val=val.substring(0,val.indexOf("@"));
            }
            if (val.equals("O")) {
                last_tag = null;
/* // this is a small hack to fix textpro bug with the first tag
            } else if (val.startsWith("B-")|| (val.startsWith("I-") && i==0)) {
*/
            } else if (val.startsWith("B-")) {
                last_tag = new Tag();
                last_tag.start = i;
                last_tag.end = i;
                last_tag.tag = levelname;
                last_tag.attrs.put(attribute, val.substring(2));
                result.add(last_tag);
            } else if (val.startsWith("I-")) {

                if (last_tag==null || !last_tag.attrs.get(attribute).equals(val.substring(2))) {

if (allow_textpro_bugs) {
                last_tag = new Tag();
                last_tag.start = i;
                last_tag.end = i;
                last_tag.tag = levelname;
                last_tag.attrs.put(attribute, val.substring(2));
                result.add(last_tag);

}else{
                    throw new IllegalArgumentException(
                            String.format("B-%s followed by %s",
                            last_tag.attrs.get(attribute), val));
}

                }
                last_tag.end = i;
            }
        }
        return result;
    }

    public void write_column(MiniDiscourse doc,
            String[][] columns) {
        List<Markable> markables=doc.getMarkableLevelByName(levelname)
                .getMarkables();
        String[] vals = columns[column];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = "O";
        }
        for (IMarkable m : markables) {
            String val = m.getAttributeValue(attribute);
            vals[m.getLeftmostDiscoursePosition()] = "B-" + val;
            for (int i = m.getLeftmostDiscoursePosition() + 1;
                    i <= m.getRightmostDiscoursePosition(); i++) {
                vals[i] = "I-" + val;
            }
        }
    }

    public int max_column() {
        return column;
    }
}
