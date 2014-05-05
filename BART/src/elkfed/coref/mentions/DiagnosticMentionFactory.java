/*
 * DefaultMentionFactory.java
 *
 * Created on July 17, 2007, 12:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.mentions;

import elkfed.mmax.DiscourseUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.util.CorefDocuments;

import elkfed.mmax.visualize.Page;
import elkfed.mmax.visualize.Page.LabelingFN;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static elkfed.mmax.MarkableLevels.DEFAULT_COREF_LEVEL;

/** Given a MMAX2Discourse, extracts all of its mentions *INCLUDED IN TEXT*
 *  (i.e. between the <T(E)XT></T(E)XT>* tags of a SGML/XML document
 *  and returns them in a List.
 *
 * @author brett.shwom
 */
public class DiagnosticMentionFactory extends DefaultMentionFactory {

    //TODO: move the constant to MarkableLevels
    public static String DIAGNOSTIC_LEVEL = "diagnostic";
    public static final String ORDERID = "orderid";
    private static final String[] copy_attributes={"min_ids","markable_type"};
    private MiniDiscourse _current_doc;

    public static void copyAttributes(Map<String,String> orig,
            Map<String,String> forNew) {
        for (String key: copy_attributes) {
            if (orig.containsKey(key) && (!key.equals("id"))) {
                forNew.put(key, orig.get(key));
            }
        }
    }

    @Override
    protected void reportMapping(Markable m_markable,
            Markable m_coref) {
        MiniDiscourse doc;
        doc = _current_doc;
        MarkableLevel diagnosticLevel = doc.getMarkableLevelByName(DIAGNOSTIC_LEVEL);
        if (m_markable != null && m_coref != null) {
            Markable m_markable2 =
                    CorefDocuments.getInstance().corefElementIsaMarkable(doc, m_markable);
            if (m_markable2 == null) {
                System.out.println("Correct (dubious): " + m_markable.toString() +
                        "(markable:" + m_markable.getID() + "/coref:" + m_coref.getID() + " " + m_coref.toString());
            //BUG: we have to compare IDs instead of object identity?
            } else if (m_markable2 == m_markable) {
                System.out.println("Correct: " + m_markable.toString() +
                        "(markable:" + m_markable.getID() + "/coref:" + m_coref.getID() + ")");
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put("type", "correct");
                copyAttributes(m_markable.getAttributes(),attrs);
                diagnosticLevel.addMarkable(m_markable.getLeftmostDiscoursePosition(),
                        m_markable.getRightmostDiscoursePosition(),
                        attrs);
            } else {
                System.out.println("Duplicate: " + m_markable.toString() +
                        "(markable:" + m_markable.getID() + "/coref:" + m_coref.getID() + " " + m_coref.toString() + "; duplicate of " + m_markable2.getID() + " " + m_markable2.toString());
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put("type", "duplicate");
                attrs.put("correctWords", m_coref.toString());
                copyAttributes(m_markable.getAttributes(),attrs);
                diagnosticLevel.addMarkable(m_markable.getLeftmostDiscoursePosition(),
                        m_markable.getRightmostDiscoursePosition(),
                        attrs);
            }
        } else if (m_markable != null) {
            System.out.println("Non-Gold: " + m_markable.toString() +
                    "(markable:" + m_markable.getID() + ")");
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put("type", "nongold");
            copyAttributes(m_markable.getAttributes(),attrs);
            diagnosticLevel.addMarkable(m_markable.getLeftmostDiscoursePosition(),
                    m_markable.getRightmostDiscoursePosition(),
                    attrs);
        } else if (m_coref != null) {
            System.out.println("Missed: " + m_coref.toString() +
                    "(coref:" + m_coref.getID() + ")");
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put("type", "missed");
            copyAttributes(m_coref.getAttributes(),attrs);
            diagnosticLevel.addMarkable(m_coref.getLeftmostDiscoursePosition(),
                    m_coref.getRightmostDiscoursePosition(),
                    attrs);
        }
    }

    @Override
    public List<Mention> extractMentions(MiniDiscourse doc) throws IOException {
        _current_doc = doc;
        List<Mention> result = super.extractMentions(doc);

        for (Markable m_coref : DiscourseUtils.getMarkables(doc, DEFAULT_COREF_LEVEL)) {
            Markable m_markable = CorefDocuments.getInstance().corefElementIsaMarkable(doc, m_coref);
            if (m_markable == null) {
                reportMapping(null, m_coref);
            }
        }
        doc.getMarkableLevelByName(DIAGNOSTIC_LEVEL).saveMarkables();
        dumpDiagnosticHTML();
        _current_doc = null;
        return result;
    }

    public static class MarkablesByLength implements Comparator<Markable> {

        public int compare(Markable m0, Markable m1) {
            int len0 = m0.getRightmostDiscoursePosition() -
                    m0.getLeftmostDiscoursePosition();
            int len1 = m1.getRightmostDiscoursePosition() -
                    m1.getLeftmostDiscoursePosition();
            if (len0 < len1) {
                return -1;
            } else if (len1 < len0) {
                return +1;
            } else {
                int delta_left = m0.getLeftmostDiscoursePosition() - m1.getLeftmostDiscoursePosition();
                int delta_right = m0.getRightmostDiscoursePosition() - m1.getRightmostDiscoursePosition();
                if (delta_left != 0) {
                    return delta_left;
                } else if (delta_right != 0) {
                    return -delta_right;
                }
                return 0;
            }
        }
    }

    private void dumpDiagnosticHTML() {
        File diagnosticDir = new File("diagnosticOutput");
        if (diagnosticDir.isDirectory()) {
            try {
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(
                        new FileOutputStream(new File(diagnosticDir, _current_doc.getNameSpace() + ".html")),
                        Charset.forName("ISO-8859-15")));
                List<Markable> diagnosticMarkables =
                        _current_doc.getMarkableLevelByName(DIAGNOSTIC_LEVEL).getMarkables(new MarkablesByLength());
                Page page=new Page();
                Page.LabelingFN labeler = new LabelingFN() {
                    @Override
                    public String getName(Markable m) {
                        return m.getAttributeValue("type","???");
                    }

                    @Override
                    public String[] getClasses(Markable m) {
                        final String[] redcls={"red"};
                        final String[] bluecls={"blue"};
                        String type=m.getAttributeValue("type");
                        if ("nongold".equals(type)) {
                            return redcls;
                        } else if ("missed".equals(type)) {
                            return bluecls;
                        } else {
                            return null;
                        }
                    }
                };
                page.dumpMarkables(_current_doc, diagnosticMarkables, labeler);
                page.writeHTML(out);
                out.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
