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
import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yannick
 */
public class TabularImport {
    int n_columns;
    int token_column;
    List<Column> columns;
    int offset_column;

    boolean ignore_txp_comments=true;

    public TabularImport(int token_col, List<Column> cols, int off_col) {
        token_column=token_col;
        offset_column=off_col;
        columns=cols;
        n_columns=token_col;
        for (Column col:cols) {
            int c=col.max_column();
            if (c>n_columns) n_columns=c;
        }
        if (offset_column >n_columns) n_columns=offset_column+1;
        n_columns++;
    }

    public TabularImport(int token_col, List<Column> cols) {
        this(token_col,cols,-1);
    }

    public MiniDiscourse do_import(File directory, String doc_id,
            String[][] column_data) {
        MiniDiscourse doc;
        if (offset_column==-1) {
            doc=MiniDiscourse.createFromTokens(directory, doc_id,
                    column_data[token_column]);
        } else {
            String[] s_start=column_data[offset_column];
            String[] s_end=column_data[offset_column+1];
            int start_offsets[]=new int[s_start.length];
            int end_offsets[]=new int[s_end.length];
            for (int i=0; i<column_data[offset_column].length; i++){
                start_offsets[i]=Integer.parseInt(s_start[i]);
                end_offsets[i]=Integer.parseInt(s_end[i]);
            }
            doc=MiniDiscourse.createFromTokensAndPositions(directory, doc_id,
                    column_data[token_column],
                    start_offsets, end_offsets);
        }
        for (Column col: columns) {
            List<Tag> tags=col.read_column(column_data);
            for (Tag t: tags) {
                doc.getMarkableLevelByName(t.tag).addMarkable(t.start, t.end, t.attrs);
            }
        }
        doc.saveAllLevels();
        return doc;
    }

    public void do_import(MiniDiscourse doc, String[][] column_data) {
        if (column_data[0].length!=doc.getDiscourseElementCount()) {
            throw new UnsupportedOperationException("basedata does not fit");
        }
        for (Column col: columns) {
            List<Tag> tags=col.read_column(column_data);
            for (Tag t: tags) {
                doc.getMarkableLevelByName(t.tag).addMarkable(t.start, t.end, t.attrs);
            }
        }
    }

    public static String[][] read_table(BufferedReader rd, int n_columns,
            boolean ignore_txp)
        throws IOException
    {
        List<List<String>> col_data=new ArrayList<List<String>>();
        for (int i=0; i<n_columns; i++) {
            col_data.add(new ArrayList<String>());
        }
        String line;
        while ((line=rd.readLine())!=null) {
            if (ignore_txp && line.startsWith("# ")) {
                continue;
            }
            String[] vals=line.split("[ \t]+");
            if (vals.length==0) {
                break;
            }
            for (int i=0;i<n_columns; i++) {
                col_data.get(i).add(vals[i]);
            }
        }
        String[][] column_data=new String[n_columns][];
        for (int i=0; i<n_columns; i++) {
            String[] col=new String[col_data.get(i).size()];
            column_data[i]=col_data.get(i).toArray(col);
        }
        return column_data;
    }

    public MiniDiscourse do_import(File directory, String doc_id, BufferedReader rd)
            throws IOException
    {
        String[][] column_data=read_table(rd,n_columns,ignore_txp_comments);
        return do_import(directory, doc_id, column_data);
    }

    public MiniDiscourse do_import(MiniDiscourse doc, BufferedReader rd)
            throws IOException
    {
        String[][] column_data=read_table(rd,n_columns,ignore_txp_comments);
        do_import(doc,column_data);
        return doc;
    }

    public static TabularImport readTPHeader(BufferedReader rd)
            throws IOException
    {
        String line;
        String[] fields=null;
        int token_column=-1, offset_column=-1;
        List<Column> content_columns=new ArrayList<Column>();
        while ((line=rd.readLine())!=null) {
            if (line.startsWith("# ")) {
                if (line.startsWith("# FIELDS: ")) {
//System.out.println("We do it!\n");
                    fields=line.substring(10).split("[ \t]+");
                    break;
                }
            } else {
                throw new IllegalArgumentException("no header found");
            }
        }
        for (int i=0;i<fields.length; i++) {
            String fld=fields[i];
            if (fld.equals("token")) {
                token_column=i;
            } else if (fld.equals("tokenstart")) {
                offset_column=i;
            } else if (fld.equals("tokenend")) {
                // ignore
            } else if (fld.equals("pos")) {
                content_columns.add(new TagColumn("pos", "tag", i));
            } else if (fld.equals("sentence")) {
                content_columns.add(new SentenceColumn("sentence", i));
            } else if (fld.equals("lemma")) {
                content_columns.add(new TagColumn("lemma","tag",i));
            } else if (fld.equals("entity")) {
                content_columns.add(new BIOColumn("enamex", "tag", i));
            } else if (fld.equals("enamex")) {
                content_columns.add(new CorefColumn("enamex", "tag", i));
            } else if (fld.equals("coref")) {
                content_columns.add(new CorefColumn("coref", "coref_set", i));
            } else {
                System.err.format("Unknown TextPro field:%s\n", fld);
            }
        }
        return new TabularImport(token_column, content_columns, offset_column);
    }

    public static void main(String args[]) {
        TabularImport ti;
        List<Column> columns=new ArrayList<Column>();
//        columns.add(new TagColumn("pos","tag",1));
//        columns.add(new BIOColumn("chunk","tag",2));
            columns.add(new TagColumn("pos","tag",2));
            columns.add(new TagColumn("lemma","tag",3));
            columns.add(new SentenceColumn("sentence",1));
            columns.add(new TagColumn("morph","tag",4));

            columns.add(new CorefColumn("enamex","tag",5));  //1
            columns.add(new CorefColumn("coref","coref_set",6));

/*
            columns.add(new CorefColumn("enamex","tag",4));  //1
            columns.add(new CorefColumn("coref","coref_set",5));
            columns.add(new CorefColumn("enamex","tag",6)); //2
            columns.add(new CorefColumn("coref","coref_set",7));
            columns.add(new CorefColumn("enamex","tag",8)); //3
            columns.add(new CorefColumn("coref","coref_set",9));
            columns.add(new CorefColumn("enamex","tag",10));  //4
            columns.add(new CorefColumn("coref","coref_set",11));
            columns.add(new CorefColumn("enamex","tag",12)); //5
            columns.add(new CorefColumn("coref","coref_set",13));
            columns.add(new CorefColumn("enamex","tag",14));  //6
            columns.add(new CorefColumn("coref","coref_set",15));
            columns.add(new CorefColumn("enamex","tag",16));  //7
            columns.add(new CorefColumn("coref","coref_set",17));
        ti=new TabularImport(0,columns,18);
*/ 
         ti=new TabularImport(0,columns,7);
       try {
//            BufferedReader br=new BufferedReader(new InputStreamReader(               new FileInputStream(txpName), "ISO-8859-15"));

           BufferedReader br=new BufferedReader(new FileReader(args[2]));
//           TabularImport ti=TabularImport.readTPHeader(br);
             ti.do_import(new File(args[0]),args[1],br);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
