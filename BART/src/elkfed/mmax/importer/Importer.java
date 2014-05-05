/*
 *  Copyright 2007 Yannick Versley / Univ. Tuebingen
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

package elkfed.mmax.importer;

import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.minidisc.IMarkable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yannick
 */
public class Importer {
    public static class Tag implements IMarkable {
        public int start;
        public int end;
        public String tag;
        public Map<String, String> attrs = new HashMap<String, String>();

        public void adjustSpan(int new_start, int new_end) {
            start=new_start;
            end=new_end;
        }

        public String getAttributeValue(String key) {
            return attrs.get(key);
        }

        public int getLeftmostDiscoursePosition() {
            return start;
        }

        public int getRightmostDiscoursePosition() {
            return end;
        }

        public void setAttributeValue(String key, String val) {
            attrs.put(key,val);
        }
    }
    protected File _dir;
    protected String _docId;
    protected List<String> tokens = new ArrayList<String>();
    protected List<Tag> tags=new ArrayList<Tag>();
    protected List<Tag> tag_stack=new ArrayList<Tag>();
    
    public Importer(File dir, String docId) {
        _dir=dir;
        _docId=docId;
    }
    
    int pos() {
        return tokens.size();
    }
    
    void add_token(String tok) {
        tokens.add(tok);
    }
    
    Tag push_tag(String level, List<Tag> stack) {
        Tag t=new Tag();
        t.start=tokens.size();
        t.tag=level;
        stack.add(t);
        return t;
    }

    /** buggy push_tag for Ontonotes, because they
     *  serialize <A><B>bla</B> bla</A>
     *  as <B><A>bla</B> bla</A> instead.
     * @param level
     * @param stack
     * @return
     */
    Tag buggy_push_tag(String level, List<Tag> stack) {
        Tag t=new Tag();
        t.start=tokens.size();
        t.tag=level;
        int stack_top=stack.size()-1;
        while (stack_top>=0 &&
                stack.get(stack_top).start==t.start) {
            stack_top--;
        }
        stack.add(stack_top+1, t);
        return t;
    }

    Tag push_tag(String level) {
        return push_tag(level,tag_stack);
    }
    
    Tag pop_tag(String level, List<Tag> stack) {
        Tag t=stack.remove(stack.size()-1);
        assert t.tag.equals(level);
        t.end=tokens.size()-1;
        tags.add(t);
        return t;        
    }

    Tag pop_tag(String level) {
        return pop_tag(level, tag_stack);
    }
    
    MiniDiscourse create() {
        String[] tokensA = new String[tokens.size()];
        tokensA = tokens.toArray(tokensA);
        MiniDiscourse disc = MiniDiscourse.createFromTokens(_dir, _docId, tokensA);
        for (Tag t: tags) {
            disc.getMarkableLevelByName(t.tag).addMarkable(t.start, t.end, t.attrs);
        }
        disc.saveAllLevels();
        return disc;
    }
}
