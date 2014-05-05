/*
 *  Copyright 2009 Yannick Versley / Univ. Tuebingen
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

import elkfed.ml.util.Alphabet;
import elkfed.mmax.importer.Importer.Tag;
import elkfed.mmax.minidisc.IMarkable;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * bracketing-based coreference column as we need it for
 * SemEval conversion
 *
 * TODO: calculate min_ids in some way for imported data?
 */
public class SECorefColumn implements Column {

    protected String levelname;
    protected String attribute;
    protected int column;
    protected Alphabet<String> set_ids=new Alphabet<String>();
    final static Pattern re_open = Pattern.compile("\\(([A-Za-z_0-9]+)");
    final static Pattern re_close = Pattern.compile("([A-Za-z_0-9]+)\\)");
    final static Pattern re_single = Pattern.compile("\\(([A-Za-z_0-9]+)\\)");

    public SECorefColumn(String lvl, String att, int col) {
        levelname = lvl;
        attribute = att;
        column = col;
    }

    public List<Tag> read_column(String[][] columns) {
        String[] vals = columns[column];
        List<String> opening = new ArrayList<String>();
        List<String> closing = new ArrayList<String>();
        List<Tag> stack = new ArrayList<Tag>();
        List<Tag> result = new ArrayList<Tag>();
        Tag last_tag = null;
        for (int i = 0; i < vals.length; i++) {
            int wid = i + 1;
            String val = vals[i];
            if ("_".equals(val)) {
                // ignore
            } else {
                opening.clear();
                closing.clear();
                String[] tags = val.split("\\|");
                for (String s : tags) {
                    Matcher m_single = re_single.matcher(s);
                    Matcher m_opening = re_open.matcher(s);
                    Matcher m_closing = re_close.matcher(s);
                    if (m_single.matches()) {
                        Tag t = new Tag();
                        t.start = t.end = i;
                        t.tag = levelname;
                        t.attrs.put(attribute, m_single.group(1));
                    } else if (m_opening.matches()) {
                        opening.add(m_opening.group(1));
                    } else if (m_closing.matches()) {
                        closing.add(m_closing.group(1));
                    }
                }
                for (String s : opening) {
                    Tag t = new Tag();
                    t.start = t.end = i;
                    t.tag = levelname;
                    t.attrs.put(attribute, s);
                    stack.add(t);
                }
                for (String s : closing) {
                    Tag t = stack.remove(stack.size() - 1);
                    if (!s.equals(t.attrs.get(attribute))) {
                        throw new RuntimeException("opening and closing tags do not" +
                                "match: should be " + s + " but stack has " + t.attrs.get(attribute));
                    }
                    t.end = i;
                    result.add(t);
                }
            }
        }
        if (!stack.isEmpty()) {
            throw new RuntimeException("closing coref tag missing");
        }
        return result;
    }

    private static class Bracket implements Comparable<Bracket> {

        final static int OPEN = 0;
        final static int CLOSE = 1;
        int kind;
        int len;
        String label;

        Bracket(int k, int l, String s) {
            kind = k;
            len = l;
            label = s;
        }

        public int compareTo(Bracket other) {
            int val;
            int order;
            val = kind-other.kind;
            if (val != 0) {
                return val;
            }
            if (kind == OPEN) {
                order = +1;
            } else {
                order = -1;
            }
            val = order * (other.len - len);
            if (val != 0) {
                return val;
            }
            return order * label.compareTo(other.label);
        }
    }

    public void write_column(MiniDiscourse doc,
            String[][] columns) {
        List<Markable> markables = doc.getMarkableLevelByName(levelname).getMarkables();
        String[] vals = columns[column];
        List<Bracket>[] brackets = new List[vals.length];
        for (int i = 0; i < vals.length; i++) {
            brackets[i] = new ArrayList<Bracket>();
        }

        for (IMarkable m : markables) {
            String val = m.getAttributeValue(attribute);
            String val_n = ""+(1+set_ids.lookupIndex(val));
            int m_len = m.getRightmostDiscoursePosition() -
                    m.getLeftmostDiscoursePosition();
            if (m_len == 0) {
                brackets[m.getLeftmostDiscoursePosition()].add(
                        new Bracket(Bracket.CLOSE, m_len, "(" + val_n + ")"));
            } else {
                brackets[m.getLeftmostDiscoursePosition()].add(
                        new Bracket(Bracket.OPEN, m_len, "(" + val_n));
                brackets[m.getRightmostDiscoursePosition()].add(
                        new Bracket(Bracket.CLOSE, m_len, val_n + ")"));
            }
        }
        for (int i = 0; i < vals.length; i++) {
            if (brackets[i].isEmpty()) {
                vals[i] = "_";
            } else {
                Collections.sort(brackets[i]);
                StringBuffer b=new StringBuffer();
                for (Bracket br: brackets[i]) {
                    b.append("|");
                    b.append(br.label);
                }
                vals[i]=b.substring(1);
            }
        }
    }

    public int max_column() {
        return column;
    }

    public static void main(String args[]) {
        TabularExport te;
        List<Column> columns = new ArrayList<Column>();
//        columns.add(new SentenceColumn("sentence", 1));
//        columns.add(new TagColumn("pos", "tag", 2));
//        columns.add(new SECorefColumn("coref", "coref_set", 3));
        columns.add(new SECorefColumn("response", "coref_set", 1));
        te = new TabularExport(0, columns, -1);
        try {
            PrintWriter pw=new PrintWriter(
                    new OutputStreamWriter(System.out, "ISO-8859-15"), true);
            for (int i=1; i<args.length; i++) {
                System.out.println("#begin document "+args[i]);
                te.do_export(MiniDiscourse.load(new File(args[0]),
                        args[i]), pw);
                System.out.println("#end document "+args[i]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
