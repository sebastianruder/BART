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
package elkfed.mmax.minidisc;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author versley
 */
public class Markable implements IMarkable {
    
    private int _start;
    private int _end;
    private int _id;
    private MarkableLevel _level;
    
    private Map<String,String> _attributeValues;
    private int[] _holes;
    
    /** this is internally used by MarkableLevel.addMarkable */
    Markable(int start, int end, MarkableLevel lvl,
            Map<String,String> attrs, int id) {
        _start=start;
        _end=end;
        assert _start<=_end;
        _level=lvl;
        _id=id;
        _attributeValues=attrs;
    }
    
    public void adjustSpan(int new_start, int new_end) {
        _level.adjustSpan(this,new_start,new_end);
        _start=new_start;
        _end=new_end;
    }
    
    public int getLeftmostDiscoursePosition() {
        return _start;
    }
    public int getLeftmostTextPosition() {
        return _level.getDocument()._start_pos[_start];
    }
    
    public int getRightmostDiscoursePosition() {
        return _end;
    }
    public int getRightmostTextPosition() {
        return _level.getDocument()._end_pos[_end];
    }

    public Map<String,String> getAttributes() {
        return _attributeValues;
    }
    
    public String getAttributeValue(String key) {
        return _attributeValues.get(key);
    }

    public String getAttributeValue(String key, String def_val) {
        String val=_attributeValues.get(key);
        if (val==null) return def_val;
        return val;
    }
    
    public void setAttributeValue(String key, String val) {
        _attributeValues.put(key,val);
    }
    
    public String[] getDiscourseElements() {
        return _level.getDocument().getDiscourseElements(_start,_end);
    }
    
    public String[] getDiscourseElementIDs() {
        return _level.getDocument().getDiscourseElementIDs(_start,_end);
    }
    
    public String getID() {
        return "markable_"+_id;
    }
    
    public int getIntID() {
        return _id;
    }
    
    public MarkableLevel getMarkableLevel() {
        return _level;
    }
    
    /** returns the tokens covered by the markable */
    @Override
    public String toString() {
        StringBuffer buf=new StringBuffer("[");
        for (String s: getDiscourseElements()) {
            buf.append(s).append(" ");
        }
        buf.setCharAt(buf.length()-1, ']');
        return buf.toString();
    }

    Markable copy(MarkableLevel lvl) {
        // makes a copy of this markable that can be added to
        // MarkableLevel lvl
        Markable other=new Markable(_start,_end,lvl,
                new HashMap(_attributeValues),
                _id);
        if (_holes!=null) {
            other._holes=new int[_holes.length];
            System.arraycopy(_holes, 0, other._holes, 0, _holes.length);
        }
        return other;
    }

    void setHoles(int[] holes) {
        _holes=holes;
    }
    int[] getHoles() {
        return _holes;
    }
}
