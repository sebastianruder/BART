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

import gnu.trove.list.array.TByteArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author versley
 */
public class MarkableQuery {
    public static final byte OP_EQ=0;
    public static final byte OP_NE=1;
    String markableLevel;
    private final List<String> re_att=new ArrayList<String>();
    private final List<Pattern> re_pat=new ArrayList<Pattern>();
    private final List<String> str_att=new ArrayList<String>();
    private final List<String> str_val=new ArrayList<String>();
    private final TByteArrayList str_op=new TByteArrayList();
    
    public MarkableQuery(MarkableLevel lvl) {
        markableLevel=lvl.getName();
    }
    
    public void addAttCondition(String attribute,String val,byte cond) {
        str_att.add(attribute);
        str_val.add(val);
        str_op.add(cond);
    }
    
    public void addAttRE(String attribute, Pattern pat) {
        re_att.add(attribute);
        re_pat.add(pat);
    }
    
    public List<Markable> execute(MarkableLevel lvl) {
        assert lvl.getName().equals(markableLevel);
        List<Markable> result=new ArrayList<Markable>();
        MARKABLES: for (Markable m: lvl.getMarkables()) {
            for (int i=0; i<str_att.size(); i++) {
                String val1=m.getAttributeValue(str_att.get(i));
                String val2=str_val.get(i);
                byte op=str_op.get(i);
                if (op==OP_EQ && !val2.equals(val1)) continue MARKABLES;
                if (op==OP_NE && val2.equals(val1)) continue MARKABLES;
            }
            for (int i=0; i<re_att.size(); i++) {
                String val1=m.getAttributeValue(re_att.get(i));
                Pattern pat=re_pat.get(i);
                if (val1==null) continue MARKABLES;
                if (!pat.matcher(val1).matches()) continue MARKABLES;
            }
            result.add(m);
        }
        return result;
    }

    public List<Markable> execute(MarkableLevel level, Comparator<Markable> comp) {
        List<Markable> result=execute(level);
        Collections.sort(result,comp);
        return result;
    }
}
