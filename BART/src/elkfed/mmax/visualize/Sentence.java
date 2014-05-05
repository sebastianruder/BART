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
package elkfed.mmax.visualize;

import org.json.JSONArray;
import org.json.JSONObject;

import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.visualize.Page.LabelingFN;

/**
 *
 * @author yannick
 */
public class Sentence //implements JSONAware
{

    protected int start;
    protected int end;
    protected MiniDiscourse doc;
    protected JSONArray tokens;
    protected JSONArray markables=new JSONArray();
    private String name;
    protected LabelingFN labeler;
    private static final String ORDERID = "orderid";

    public Sentence(Markable m, LabelingFN fn) {
        String orderid = m.getAttributeValue(ORDERID);
        end=m.getRightmostDiscoursePosition();
        start=m.getLeftmostDiscoursePosition();
        doc = m.getMarkableLevel().getDocument();
        labeler=fn;
        MarkableLevel posLevel = doc.getMarkableLevelByName("pos");
        name = doc.getNameSpace() + "_s" + orderid;
        tokens = new JSONArray();
        for (int i = m.getLeftmostDiscoursePosition();
                i <= m.getRightmostDiscoursePosition(); i++) {
            JSONArray lst = new JSONArray();
            lst.put(doc.getDiscourseElementAtDiscoursePosition(i));
            try {
                lst.put(posLevel.getMarkablesAtDiscoursePosition(i).get(0).getAttributeValue("tag"));
            } catch (IndexOutOfBoundsException ex) {
                lst.put("*UNK*");
            }
            tokens.put(lst);
        }
    }

    public void addMarkable(Markable m) {
        JSONObject m_json = labeler.getAttributes(m);
        JSONArray span = new JSONArray();
        span.put(m.getLeftmostDiscoursePosition() - start);
        if (m.getRightmostDiscoursePosition() >= end) {
            span.put(end - start);
        } else {
            span.put(m.getRightmostDiscoursePosition() - start);
        }
        m_json.put("span", span);
        String min_ids = m.getAttributeValue("min_ids");
        if (min_ids != null) {
            String[] range = MarkableHelper.parseRanges(min_ids);
            //System.out.println(Arrays.asList(range));
            int min_start = doc.getDiscoursePositionFromDiscourseElementID(range[0]);
            int min_end = doc.getDiscoursePositionFromDiscourseElementID(range[range.length - 1]);
            JSONArray min_span = new JSONArray();
            min_span.put(min_start - start);
            if (min_end >= end) {
                min_span.put(end-start);
            } else {
                min_span.put(min_end-start);
            }
            m_json.put("min_ids", min_span);
        }
        markables.put(m_json);
    }

    public String toJSONString() {
        return String.format("[%s,%s]", tokens, markables);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
