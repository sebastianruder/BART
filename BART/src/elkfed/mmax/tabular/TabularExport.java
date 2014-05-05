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

import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.NullPointerException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class TabularExport {

    int n_columns;
    int token_column;
    int offset_column;
    List<Column> columns;

    public TabularExport(int token_col, List<Column> cols, int ocolumn) {
        token_column=token_col;
        columns=cols;
        offset_column=ocolumn;
        n_columns=token_col;
        if (offset_column>=0) {
            if (offset_column+1>n_columns) {
                n_columns=offset_column+1;
            }
        }
        for (Column col:cols) {
            int c=col.max_column();
            if (c>n_columns) n_columns=c;
        }
        n_columns++;
    }

    public TabularExport(int token_col, List<Column> cols)
    {
        this(token_col,cols,-1);
    }

    public static void write_table(String[][] result, PrintWriter out) {
        for (int i=0; i<result[0].length; i++) {
            for (int j=0; j<result.length; j++) {
                if (j>0) {
                    out.print("\t");
                }
                out.print(result[j][i]);
            }
            out.println();
        }
    }

    public void do_export(MiniDiscourse doc,
            PrintWriter out) throws IOException {
        String[][] result=make_tabular(doc);
        write_table(result,out);
    }

    public String[][] make_tabular(MiniDiscourse doc) {
        String[][] result=new String[n_columns][doc.getDiscourseElementCount()];
        for (int i=0; i<doc.getDiscourseElementCount(); i++) {
            result[token_column][i]=doc.getDiscourseElementAtDiscoursePosition(i);
            if (offset_column>=0) {
                try {
                    result[offset_column][i]=""+doc.leftmostTextPosition(i);
                    result[offset_column+1][i]=""+doc.rightmostTextPosition(i);
                } catch(NullPointerException ex) {
                    result[offset_column][i]=result[offset_column+1][i]="-";
                }
            }
        }
        for (Column col:columns) {
            col.write_column(doc, result);
        }
        return result;
    }

    public static void main(String args[]) {
        TabularExport te;
        List<Column> columns=new ArrayList<Column>();

        columns.add(new TagColumn("pos","tag",3));
//        columns.add(new BIOColumn("chunk","tag",4));
        columns.add(new BIOColumn("enamex","tag",4));
        columns.add(new BIOColumn("response","coref_set",5));

/* olga: do not export any coref/enamex columns, add them at the next step 
*  (to support overlapping or embedded mentions
*/
/*
        columns.add(new TagColumn("pos","tag",3));
        columns.add(new SentenceColumn("sentence", 4));
*/ 
        te=new TabularExport(0,columns,1);
        try {
            te.do_export(MiniDiscourse.load(new File(args[0]),
                args[1]),new PrintWriter(System.out,true));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
