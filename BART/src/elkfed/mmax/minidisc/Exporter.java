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

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * An Exporter object can be used to feed an MMAX document into
 * a SAX ContentHandler. This is useful if one wants to either
 * directly create an XML document or use XSLT transformations
 * to create an XML document in a specific format.
 * 
 * @author yannick
 */
public class Exporter {

    private final static char[] blanks =
            "                     ".toCharArray();
    private final static char[] ac_newline="\n".toCharArray();

    private static void indent(ContentHandler hd, int size)
            throws SAXException {
        while (size >= blanks.length) {
            hd.ignorableWhitespace(blanks, 0, blanks.length);
            size -= blanks.length;
        }
        hd.ignorableWhitespace(blanks, 0, size);
    }
    
    private static void newline(ContentHandler hd) throws SAXException {
        hd.ignorableWhitespace(ac_newline, 0, ac_newline.length);
    }
    private List<String> _levelOrder;

    public Exporter(List<String> levels) {
        _levelOrder = levels;
    }

    private interface DiscourseEvent extends Comparable<DiscourseEvent> {

        int getKind();

        int getPosition();

        void actOn(ContentHandler hd, List<Markable> stack)
                throws SAXException;
    }

    private class TokenEvent implements DiscourseEvent {

        final int discoursePosition;
        final String token;

        private TokenEvent(int i, String string) {
            discoursePosition = i;
            token = string;
        }

        public int getKind() {
            return 2;
        }

        public int getPosition() {
            return discoursePosition;
        }

        public int compareTo(DiscourseEvent other) {
            if (other.getPosition() != getPosition()) {
                return getPosition() - other.getPosition();
            }
            if (other.getKind() != getKind()) {
                return getKind() - other.getKind();
            }
            return 0;
        }

        public void actOn(ContentHandler hd, List<Markable> stack)
                throws SAXException {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "f", "f", "CDATA", token);
            indent(hd, stack.size() * 2 + 2);
            hd.startElement("", "word", "word", atts);
            hd.endElement("", "word", "word");
        }
    }

    private class MarkableStart implements DiscourseEvent {

        final Markable markable;
        final String level;

        public MarkableStart(Markable m, String lvl) {
            markable = m;
            level = lvl;
        }

        public int getKind() {
            return 1;
        }

        public int getPosition() {
            return markable.getLeftmostDiscoursePosition();
        }

        public void actOn(ContentHandler hd, List<Markable> stack) throws SAXException {
            stack.add(markable);
            AttributesImpl atts = new AttributesImpl();
            Map<String, String> att_map = markable.getAttributes();
            indent(hd, stack.size() * 2);
            for (String key : att_map.keySet()) {
                if (!key.equals("mmax_level")) {
                    atts.addAttribute("", key, key, "CDATA", att_map.get(key));
                }
            }
            hd.startElement("", level, level, atts);
        }

        public int compareTo(DiscourseEvent other) {
            if (other.getPosition() != getPosition()) {
                return getPosition() - other.getPosition();
            }
            if (other.getKind() != getKind()) {
                return getKind() - other.getKind();
            }
            MarkableStart oth = (MarkableStart) other;
            Markable m2 = oth.markable;
            if (m2.getRightmostDiscoursePosition() !=
                    markable.getRightmostDiscoursePosition()) {
                return m2.getRightmostDiscoursePosition() -
                        markable.getRightmostDiscoursePosition();
            }
            int ord1 = _levelOrder.indexOf(level);
            int ord2 = _levelOrder.indexOf(oth.level);
            if (ord1 != ord2) {
                return ord1 - ord2;
            }
            return 0;
        }
    }

    private class MarkableEnd implements DiscourseEvent {

        final Markable markable;
        final String level;

        public MarkableEnd(Markable m, String lvl) {
            markable = m;
            level = lvl;
        }

        public int getKind() {
            return 0;
        }

        public int getPosition() {
            return markable.getRightmostDiscoursePosition() + 1;
        }

        public void actOn(ContentHandler hd, List<Markable> stack) throws SAXException {
            if (stack.isEmpty() || markable != stack.get(stack.size() - 1)) {
                int idx = stack.indexOf(markable);
                if (idx != -1) {
                    while (markable != stack.get(stack.size() - 1)) {
                        Markable m = stack.get(stack.size() - 1);
                        String lvl=m.getMarkableLevel().getName();
                        hd.processingInstruction("concur",
                                String.format("cut=%s:%s",
                                lvl,
                                m.hashCode()));
                        hd.endElement("", lvl, lvl);
                        newline(hd);
                        stack.remove(stack.size() - 1);
                    }
                    stack.remove(stack.size() - 1);
                    indent(hd, stack.size() * 2 + 2);
                    hd.endElement("", level, level);
                } else {
                    hd.processingInstruction("concur",
                            String.format("here=%s:%s",
                            level,
                            markable.hashCode()));
                }
            } else {
                stack.remove(stack.size() - 1);
                indent(hd, stack.size() * 2 + 2);
                hd.endElement("", level, level);
            }
        }

        public int compareTo(DiscourseEvent other) {
            if (other.getPosition() != getPosition()) {
                return getPosition() - other.getPosition();
            }
            if (other.getKind() != getKind()) {
                return getKind() - other.getKind();
            }
            MarkableEnd oth = (MarkableEnd) other;
            Markable m2 = oth.markable;
            if (m2.getLeftmostDiscoursePosition() !=
                    markable.getLeftmostDiscoursePosition()) {
                return m2.getLeftmostDiscoursePosition() -
                        markable.getLeftmostDiscoursePosition();
            }
            int ord1 = _levelOrder.indexOf(level);
            int ord2 = _levelOrder.indexOf(oth.level);
            if (ord1 != ord2) {
                return ord2 - ord1;
            }
            return 0;
        }
    }

    /** feeds a MiniDiscourse into the ContentHandler.
     * In the case of overlapping markables,
     * special processing instructions (&lt;?concur cut=ID?&gt;
     * and &lt;?concur here=ID?&gt;) are created.
     * 
     * @param doc the MiniDiscourse document
     * @param hd  the ContentHandler that receives the SAX events
     * @throws org.xml.sax.SAXException
     */
    public void convertFile(MiniDiscourse doc,
            ContentHandler hd)
            throws SAXException {
        char[] newline = "\n".toCharArray();
        List<DiscourseEvent> des = new ArrayList<DiscourseEvent>();
        List<Markable> stack = new ArrayList<Markable>();
        String[] toks = doc.getTokens();
        for (int i = 0; i < toks.length; i++) {
            des.add(new TokenEvent(i, toks[i]));
        }
        for (String level_name : _levelOrder) {
            for (Markable m : doc.getMarkableLevelByName(level_name).getMarkables()) {
                des.add(new MarkableStart(m, level_name));
                des.add(new MarkableEnd(m, level_name));
            }
        }
        Collections.sort(des);
        hd.startDocument();
        AttributesImpl atts = new AttributesImpl();
        hd.startElement("", "", "discourse", atts);
        for (DiscourseEvent de : des) {
            de.actOn(hd, stack);
            hd.ignorableWhitespace(newline, 0, newline.length);
        }
        hd.endElement("", "discourse", "discourse");
        hd.ignorableWhitespace(newline, 0, newline.length);
        hd.endDocument();
    }

    /** converts the document that is indicated by the
     * first two arguments (arg0=directory, arg1=document id)
     * into XML and writes the result to the standard output.
     */
    public static void main(String[] args) {
        try {
            MiniDiscourse disc = MiniDiscourse.load(
                    new File(args[0]), args[1]);
            PrintWriter out = new PrintWriter(System.out);
            StreamResult streamResult = new StreamResult(out);
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler hd = tf.newTransformerHandler();
            hd.getTransformer().setOutputProperty(OutputKeys.ENCODING,
                    System.getProperty("file.encoding"));
            hd.setResult(streamResult);
            String[] wanted_levels;
            
            if (args.length>2) {
                wanted_levels=args[2].split(",");
            } else {
                wanted_levels = new String[]{
                "coref", "sentence"
                };
            }
            Exporter ex = new Exporter(Arrays.asList(wanted_levels));
            ex.convertFile(disc, hd);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
