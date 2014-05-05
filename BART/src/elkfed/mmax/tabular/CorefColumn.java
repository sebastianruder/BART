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
import elkfed.mmax.minidisc.MarkableHelper;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import elkfed.lang.AbstractLanguagePlugin;

/** 
 * Analyzes a column with annotated coreference info (entity_id, min)
 * (based on BIO columns)
 * @author olga
 */

public class CorefColumn implements Column {
    protected String levelname;
    protected String attribute;
    protected int column;
    protected static final boolean use_min = true;
    //    public static final String MIN_IDS = "min_ids";
     public CorefColumn(String lvl, String att, int col) {
        levelname=lvl;
        attribute=att;
        column=col;
    }

    public List<Tag> read_column(String[][] columns) {
	String[] vals = columns[column];
        List<Tag> result = new ArrayList<Tag>();
        Tag last_tag = null;
        for (int i = 0; i < vals.length; i++) {
            int wid=i+1;
            String val = vals[i];
            // ignore TextPro's @confidence values
            if (val.contains("@")) {
                val=val.substring(0,val.indexOf("@"));
            }
            if (val.equals("O")) {
                last_tag = null;
            } else if (val.startsWith("B-")) {
                last_tag = new Tag();
                last_tag.start = i;
                last_tag.end = i;
                last_tag.tag = levelname;
                int nextind=-1;
		//ignore mention id attribute
		nextind=val.indexOf("=");
                if (nextind>=0) {
		    val= val.substring(nextind+1);
		}

		// fill coref_set attribute
		nextind=val.indexOf("=");
                if (nextind>=0) {
		    last_tag.attrs.put(attribute, val.substring(2,nextind));
		    val= val.substring(nextind+1);
		}
		// fill mtype argument
		nextind=val.indexOf("=");
                if (nextind>=0) {
                    last_tag.attrs.put(AbstractLanguagePlugin.MARKABLE_TYPE, 
				       val.substring(0,nextind));
                    val=val.substring(nextind+1);
                }
		//fill etype argument
		nextind=val.indexOf("=");
                if (nextind>=0) {
                    last_tag.attrs.put(AbstractLanguagePlugin.SEM_TYPE, 
				       val.substring(0,nextind));
                    val=val.substring(nextind+1);
                }
		
		//fill min argument
		if (use_min && val!=null) {
		    if (val.equals("B-MIN")) {
			last_tag.attrs.put(AbstractLanguagePlugin.MIN_IDS,
					   "word_"+wid);
		    } else if (val.equals("I-MIN")) {
			String[] minrange = 
			    MarkableHelper.parseRanges(last_tag.attrs.get(AbstractLanguagePlugin.MIN_IDS));
			minrange[1]="word_"+wid;
			last_tag.attrs.put(AbstractLanguagePlugin.MIN_IDS,minrange[0] + ".." + minrange[1]);
		    }
		}
		result.add(last_tag);
	    } else if (val.startsWith("I-")) {
                if (!last_tag.attrs.get(attribute).equals(val.substring(2,val.indexOf("=")))) {
                    throw new IllegalArgumentException(
                            String.format("B-%s followed by %s",
                            last_tag.attrs.get(attribute), val));
                }
                last_tag.end = i;
                if (use_min) {
                  if (val.endsWith("B-MIN")) {
		      last_tag.attrs.put(AbstractLanguagePlugin.MIN_IDS,"word_"+wid);
                  } else if (val.endsWith("I-MIN")) {
		      String[] minrange = MarkableHelper.parseRanges(last_tag.attrs.get(AbstractLanguagePlugin.MIN_IDS));
		      minrange[1]="word_"+wid;
		      last_tag.attrs.put(AbstractLanguagePlugin.MIN_IDS,minrange[0] + ".." + minrange[1]);
                  }
                }
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
        for (Markable m : markables) {
            String val = val = m.getAttributeValue(attribute);
            
            if (attribute.equals("coref_set")) {
// add more info -- mtype, semclass NB: if you change this, change the read_column so that they are always in accordance

              val+= '=' + m.getAttributeValue(AbstractLanguagePlugin.MARKABLE_TYPE);
              val+= '=' + m.getAttributeValue(AbstractLanguagePlugin.SEM_TYPE);

	    }


	    String[] ranges = MarkableHelper.parseRanges(m.getAttributeValue("span"));
	    int[] positions = doc.getPositions(ranges);

	    int minstart=doc.getDiscoursePositionFromDiscourseElementID(ranges[0]);
	    int minend=doc.getDiscoursePositionFromDiscourseElementID(ranges[1]);

	    if (use_min && m.getAttributeValue(AbstractLanguagePlugin.MIN_IDS) != null ) {
		String[] minranges = MarkableHelper.parseRanges(m.getAttributeValue(AbstractLanguagePlugin.MIN_IDS));
		minstart=doc.getDiscoursePositionFromDiscourseElementID(minranges[0]);
		minend=doc.getDiscoursePositionFromDiscourseElementID(minranges[1]);
	    }
                
	    
            int ilast=-1;
            for (int i : positions) {
               
		String sttag="I-";
		if (i-ilast>1) sttag="M-";
                if (i==m.getLeftmostDiscoursePosition()) sttag="B-";
                sttag+="M_"+m.getIntID()+"=";
                if (vals[i].equals("O")) 
		    vals[i]="";
                else
		    vals[i]+='@';
                vals[i] += sttag + val;
//                if (use_min) {
                    vals[i]+="=";
                    if (i<minstart) vals[i]+="O";
		    if (i==minstart) vals[i]+="B-MIN";
		    if (i>minstart && i<=minend) vals[i]+="I-MIN";
                    if (i>minend) vals[i]+="O";
//                }
                    
                ilast=i;
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
	   columns.add(new CorefColumn("coref", "coref_set", 3));
//	        columns.add(new CorefColumn("response", "coref_set", 3));
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
