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
package elkfed.mmax.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** reads Ontonotes' craptacular SGML format
 *
 * @author yannick.versley
 */
public class OntonotesReader {

    private static Charset charset = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder = charset.newDecoder();
    private static Pattern tokenPattern = Pattern.compile(
            "([\\p{L}\\d,_@/#\\.`'\"\\*\\$\\?!\\\\:;%=-]+)( *)");
    private static Pattern openTagPattern = Pattern.compile("<([A-Za-z]+) *");
    private static Pattern closeTagPattern = Pattern.compile("</([A-Za-z]+) *> *");
    private static Pattern tagAttrPattern = Pattern.compile(
            "([A-Za-z]+)=\"([^\"]*)\" *");
    private static Pattern endTagPattern = Pattern.compile("> *");
    private static Pattern newlinePattern = Pattern.compile("[\r\n]+");
    private static Pattern entityPattern = Pattern.compile(
            "&([a-z]+;|#[0-9]+;|)( *)");
    private static Pattern unwanted_token =
            Pattern.compile("^(?:0|\\*(?:ICH|PRO|T|RNR|U|EXP|NOT|PPA|\\?)\\*(?:-[0-9]+)?|\\*(?:-[0-9]+)?)$");
    public static final int TOKEN = 0;
    public static final int START_TAG = 1;
    public static final int END_TAG = 2;
    public static final int NEWLINE = 3;
    public static final int END_DOCUMENT = 4;
    CharBuffer cb;
    int pos;
    String last_token = null;
    boolean trace_flag = false;
    Map<String, String> attrs = new HashMap<String, String>();

    public OntonotesReader(File f) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
        // Decode the file into a char buffer
        cb = decoder.decode(bb);
        pos = 0;
    }

    private Matcher lookingAt(Pattern p) {
        Matcher m = p.matcher(cb.subSequence(pos, cb.length()));
        if (m.lookingAt()) {
            return m;
        }
        return null;
    }

    public String decode_entity(String what) {
        if ("".equals(what)) {
            return "&";
        } else if ("amp;".equals(what)) {
            return "&";
        } else if ("quot;".equals(what)) {
            return "\"";
        } else if ("#48;".equals(what)) {
            return "0";
        } else {
            System.err.format("Unknown entity: &%s", what);
            return "&" + what;
        }
    }

    public String getToken() {
        return last_token;
    }

    public String getName() {
        return last_token;
    }

    public boolean isTrace() {
        return trace_flag;
    }

    public String getAttribute(String key) {
        return attrs.get(key);
    }

    Iterable<String> getAttributes() {
        return attrs.keySet();
    }

    public int getNextEvent() {
        Matcher m;
        if (pos == cb.length()) {
            return END_DOCUMENT;
        } else if ((m = lookingAt(tokenPattern)) != null) {
            String tok = m.group(1);
            trace_flag = unwanted_token.matcher(tok).matches();
            pos += m.end();
            while ("".equals(m.group(2)) && pos != cb.length()) {
                m = lookingAt(entityPattern);
                if ((m = lookingAt(entityPattern)) != null) {
                    trace_flag = false;
                    tok += decode_entity(m.group(1));
                    pos += m.end();
                } else if ((m = lookingAt(tokenPattern)) != null) {
                    trace_flag = false;
                    tok += m.group(1);
                    pos += m.end();
                } else {
                    break;
                }
            }
            last_token = tok;
            return TOKEN;
        } else if ((m = lookingAt(entityPattern)) != null) {
            String tok = decode_entity(m.group(1));
            trace_flag = false;
            pos += m.end();
            while ("".equals(m.group(2)) && pos != cb.length()) {
                m = lookingAt(entityPattern);
                if ((m = lookingAt(entityPattern)) != null) {
                    tok += decode_entity(m.group(1));
                    pos += m.end();
                } else if ((m = lookingAt(tokenPattern)) != null) {
                    tok += m.group(1);
                    pos += m.end();
                }
            }
            last_token = tok;
            return TOKEN;
        } else if ((m = lookingAt(openTagPattern)) != null) {
            String tag = m.group(1);
            last_token = tag;
            attrs.clear();
            pos += m.end();
            while ((m = lookingAt(tagAttrPattern)) != null) {
                attrs.put(m.group(1), m.group(2));
                pos += m.end();
            }
            m = lookingAt(endTagPattern);
            if (m == null) {
                throw new IllegalArgumentException("unclosed tag");
            }
            pos += m.end();
            return START_TAG;
        } else if ((m = lookingAt(closeTagPattern)) != null) {
            String tag = m.group(1);
            last_token = tag;
            pos += m.end();
            return END_TAG;
        } else if ((m = lookingAt(newlinePattern)) != null) {
            pos += m.end();
            return NEWLINE;
        } else {
            throw new IllegalStateException(
                    String.format("Unrecognized character: '%c'\n", cb.get(pos)));
        }
    }

    public String describeEvent(int evt) {
        if (evt == TOKEN) {
            if (isTrace()) {
                return String.format("Trace: '%s'\n",
                        getToken());
            } else {
                return String.format("Token: '%s'\n",
                        getToken());
            }
        } else if (evt == START_TAG) {
            StringBuffer buf = new StringBuffer();
            buf.append(String.format("Open tag: %s\n", getName()));
            for (String k : getAttributes()) {
                buf.append(String.format("  attribute: %s = %s\n",
                        k, getAttribute(k)));
            }
            return buf.toString();
        } else if (evt == END_TAG) {
            return String.format("Close tag: %s\n", getName());
        } else if (evt == NEWLINE) {
            return String.format("newline\n");
        } else {
            return "Unknown event "+evt;
        }
    }

    void printTokens() {
        int evt;
        while (pos!=cb.length()) {
            evt=getNextEvent();
            System.out.print(describeEvent(evt));
        }
    }

    public static void main(String[] args) {
        try {
            for (String arg : args) {
                System.out.println("*** " + arg + " ***");
                OntonotesReader r = new OntonotesReader(new File(arg));
                r.printTokens();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
