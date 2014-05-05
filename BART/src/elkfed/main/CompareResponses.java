/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.IMarkable;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yannick.versley
 */
public class CompareResponses {

    private static String make_descr(int start, int end,
            MiniDiscourse doc) {
        if (start==-1) {
            return "*NULL*";
        } else {
            StringBuffer buf=new StringBuffer();
            buf.append(String.format("[%d,%d]",start,end));
            for (int i=start;i<end+1;i++) {
                buf.append(" ");
                buf.append(doc.getDiscourseElementAtDiscoursePosition(i));
            }
            return buf.toString();
        }
    }
    private static void report(LinkItem item1, LinkItem item2,
            String rname1, String rname2, MiniDiscourse doc) {
        String item_desc;
        String ante1_desc;
        String ante2_desc;
        if (item1!=null && item1.equals(item2)) {
            // yay! these actually match!
            return;
        }
        if (item1==null) {
            ante1_desc="*NONE*";
            item_desc=make_descr(item2.start_m,item2.end_m,doc);
            ante2_desc=make_descr(item2.start_a,item2.end_a,doc);
        } else {
            item_desc=make_descr(item1.start_m, item1.end_m,doc);
            ante1_desc=make_descr(item1.start_a, item1.end_a, doc);
            if (item2==null) {
                ante2_desc="*NONE*";
            } else {
                ante2_desc=make_descr(item2.start_a, item2.end_a,doc);
            }
        }
        System.out.println("Different Antecedents: "+item_desc);
        System.out.println(rname1+": "+ante1_desc);
        System.out.println(rname2+": "+ante2_desc);
    }
    public static class LinkItem {
        int start_m, end_m;
        int start_a, end_a;
        LinkItem(IMarkable m1, IMarkable m2) {
            start_m=m1.getLeftmostDiscoursePosition();
            end_m=m1.getRightmostDiscoursePosition();
            if (m2==null) {
                start_a=end_a=-1;
            } else {
                start_a=m2.getLeftmostDiscoursePosition();
                end_a=m2.getRightmostDiscoursePosition();
            }
        }
        @Override
        public boolean equals(Object other) {
            LinkItem o;
            if (!(other instanceof LinkItem)) {
                return false;
            }
            o=(LinkItem) other;
            return (start_m==o.start_m &&
                    end_m==o.end_m &&
                    start_a==o.start_a &&
                    end_a==o.end_a);
        }

        public int compareAnchor(LinkItem other) {
            if (start_m<other.start_m) {
                return -1;
            } else if (start_m>other.start_m) {
                return 1;
            } else if (end_m<other.end_m) {
                return -1;
            } else if (end_m>other.end_m) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + this.start_m;
            hash = 97 * hash + this.end_m;
            hash = 97 * hash + this.start_a;
            hash = 97 * hash + this.end_a;
            return hash;
        }
    }

    public static List<LinkItem> make_links(MarkableLevel lvl) {
        Map<String,Markable> last_from_set=new HashMap<String,Markable>();
        List<LinkItem> result=new ArrayList<LinkItem>();
        for (Markable m: lvl.getMarkables()) {
            String dir_ante=m.getAttributeValue("dir_antecedent");
            String set_id=m.getAttributeValue("set_id");
            if (dir_ante!=null) {
                Markable m2=lvl.getMarkableByID(dir_ante);
                result.add(new LinkItem(m,m2));
            } else if (set_id!=null) {
                Markable m2=last_from_set.get(set_id);
                if (m2!=null) {
                    result.add(new LinkItem(m,m2));
                }
            }
            if (set_id!=null) {
                last_from_set.put(set_id, m);
            }
        }
        return result;
    }
    public static void compareMarkables(MarkableLevel lvl1,
            MarkableLevel lvl2)
    {
        List<LinkItem> items1=make_links(lvl1);
        List<LinkItem> items2=make_links(lvl2);
        MiniDiscourse doc=lvl1.getDocument();
        int idx1=0, idx2=0;
        while (idx1<items1.size() && idx2<items2.size()) {
            int cmp=items1.get(idx1).compareAnchor(items2.get(idx2));
            if (cmp<0) {
                report(items1.get(idx1),null,
                        lvl1.getName(),lvl2.getName(),doc);
                idx1+=1;
            } else if (cmp>0) {
                report(null,items2.get(idx2),
                        lvl1.getName(),lvl2.getName(),doc);
                idx2+=1;
            } else {
                report(items1.get(idx1),items2.get(idx2),
                        lvl1.getName(),lvl2.getName(),doc);
                idx1+=1;
                idx2+=1;
            }
        }
        while (idx1<items1.size()) {
            report(items1.get(idx1),null,
                        lvl1.getName(),lvl2.getName(),doc);
            idx1++;
        }
        while (idx2<items2.size()) {
            report(items2.get(idx2),null,
                        lvl1.getName(),lvl2.getName(),doc);
            idx2++;
        }
        
    }

    public static void main(String[] args) {
        File dataDir = ConfigProperties.getInstance().getTestData();
        String resp1, resp2;
        if (args.length > 1) {
            resp1 = args[0];
            resp2 = args[1];
        } else {
            resp1 = "response";
            resp2 = args[0];
        }
        for (File mmaxFile : dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE)) {
            String fname = mmaxFile.getName();
            if (fname.endsWith(".mmax")) {
                fname = fname.substring(0, fname.length() - 5);
            }
            MiniDiscourse doc = MiniDiscourse.load(mmaxFile.getParentFile(),
                    fname);
            MarkableLevel response1 = doc.getMarkableLevelByName(resp1);
            MarkableLevel response2 = doc.getMarkableLevelByName(resp2);
            System.out.println("*** "+fname+" ***");
            compareMarkables(response1, response2);
        }
    }
}
