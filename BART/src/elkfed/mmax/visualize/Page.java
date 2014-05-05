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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.io.JsonStringEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;

/**
 *
 * @author yannick
 */
public class Page {

    List<Sentence> sentences = new ArrayList<Sentence>();

    public static class LabelingFN {

        public String getName(Markable m) {
            return "m"+m.getIntID();
        }

        public String[] getClasses(Markable m) {
            return null;
        }

        JSONObject getAttributes(Markable m) {
            JSONObject result = new JSONObject();
            result.put("text", getName(m));
            String[] cls = getClasses(m);
            if (cls != null) {
                JSONArray cls_json=new JSONArray();
                for (String c: cls) {
                    cls_json.put(c);
                }
                result.put("cls", cls_json);
            }
            return result;
        }
    }

    public void dumpMarkables(MiniDiscourse doc,
            List<Markable> markables,
            LabelingFN labeler) {
        List<Markable> sentenceMarkables =
                doc.getMarkableLevelByName("sentence").getMarkables();
        for (Markable m : sentenceMarkables) {
            Sentence s = new Sentence(m, labeler);
            for (Markable m2 : markables) {
                if (m2.getLeftmostDiscoursePosition() >= m.getLeftmostDiscoursePosition() &&
                        m2.getLeftmostDiscoursePosition() <= m.getRightmostDiscoursePosition()) {
                    s.addMarkable(m2);
                }
            }
            sentences.add(s);
        }
    }

    public void addSentence(Sentence s) {
        sentences.add(s);
    }

    public void writeHeader(PrintWriter out) {
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n" +
                "\"http://www.w3.org/TR/html4/strict.dtd\">");
        out.println("<html><head><title> elkfed.mmax.visualize </title>");
        out.println("<script type=\"text/javascript\" src=\"prototype.js\"></script>");
        out.println("<script type=\"text/javascript\" src=\"jswrong.js\"></script>");
        out.println("<link type=\"text/css\" rel=\"stylesheet\" href=\"default.css\">");
        out.println("<script type=\"text/javascript\">");
        out.println("<!--");
    }

    public void writeHTML(PrintWriter out) {
        writeHeader(out);
        out.println("function init_page() {");
        int num_extents=0; int num_sent=0;
        for (Sentence s : sentences) {
            out.format("addLayered(%s,%s);\n",
                    JsonStringEncoder.getInstance().encodeAsUTF8(s.getName()),
                    s.toJSONString());
            num_sent+=1;
            if (num_sent==100) {
                out.format("addBarrier('init_page_%d()');\n}\n",
                        ++num_extents);
                out.format("function init_page_%d() {\nremoveBarrier();\n",
                        num_extents);
                num_sent=0;
            }
        }
        out.println("}");
        out.println("// -->");
        out.println("</script>");
        out.println("</head><body onload=\"init_page()\">");
        out.println("</body></html>");
    }

    public void writeHTML(String fname) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(
                        new FileOutputStream(new File(fname)),
                        Charset.forName("ISO-8859-15")));
        writeHTML(out);
        out.close();
    }
}
