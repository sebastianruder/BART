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

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import elkfed.ml.util.Alphabet;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author versley
 */
public class MiniDiscourse implements Externalizable {

    public static final Pattern xml_decl =
            Pattern.compile("<\\?xml version=\"1.0\" +encoding=\"([A-Za-z0-9-]+)\" *\\?>");
    public static final DiscourseOrderMarkableComparator DISCOURSEORDERCMP =
            new DiscourseOrderMarkableComparator();
    protected static Logger _logger = Logger.getLogger("elkfed.mmax.minidisc");
    boolean autoload_markables = true;
    File _corpusDir;
    String _docId;
    String[] _tokens;
    int[] _start_pos;
    int[] _end_pos;
    protected Alphabet<String> _tokenIDs;
    protected Map<String, MarkableLevel> _markableLevels;
    protected Map<String, RelationLevel> _relationLevels;

    private MiniDiscourse() {
        _tokenIDs = new Alphabet<String>();
        _markableLevels = new HashMap<String, MarkableLevel>();
        _relationLevels = new HashMap<String, RelationLevel>();
    }

    public static MiniDiscourse load(File directory, String docId) {
        MiniDiscourse doc = new MiniDiscourse();
        doc._corpusDir = directory;
        doc._docId = docId;
        doc.loadBasedata(new File(directory,
                String.format("Basedata/%s_words.xml", docId)));
        return doc;
    }

    public static MiniDiscourse createFromTokens(File directory, String docId,
            String[] tokens) {
        MiniDiscourse doc = new MiniDiscourse();
        doc._corpusDir = directory;
        doc._docId = docId;
        doc._tokens = tokens;
        for (int i = 0; i < tokens.length; i++) {
            doc._tokenIDs.lookupIndex("word_" + (i + 1));
        }
        doc._tokenIDs.stopGrowth();
        doc.writeMMAX(new File(directory,
                String.format("%s.mmax", docId)));
        doc.writeBasedata(new File(directory,
                String.format("Basedata/%s_words.xml", docId)),
                sensibleEncoding());
        doc.autoload_markables = false;
        return doc;
    }
    
    public static MiniDiscourse createFromTokens(File directory, String docId,
            String[] tokens, String[] token_ids) {
        MiniDiscourse doc = new MiniDiscourse();
        doc._corpusDir = directory;
        doc._docId = docId;
        doc._tokens = tokens;
        for (int i = 0; i < tokens.length; i++) {
            doc._tokenIDs.lookupIndex(token_ids[i]);
        }
        doc._tokenIDs.stopGrowth();
        doc.writeMMAX(new File(directory,
                String.format("%s.mmax", docId)));
        doc.writeBasedata(new File(directory,
                String.format("Basedata/%s_words.xml", docId)),
                sensibleEncoding());
        doc.autoload_markables = false;
        return doc;
    }

    public static MiniDiscourse createFromTokensAndPositions(File directory, String docId,
            String[] tokens, int[] start, int[] end) {
        MiniDiscourse doc = new MiniDiscourse();
        doc._corpusDir = directory;
        doc._docId = docId;
        doc._tokens = tokens;
        for (int i = 0; i < tokens.length; i++) {
            doc._tokenIDs.lookupIndex("word_" + (i + 1));
        }
        doc._tokenIDs.stopGrowth();
        doc._start_pos = start;
        doc._end_pos = end;
        doc.writeMMAX(new File(directory,
                String.format("%s.mmax", docId)));
        doc.writeBasedata(new File(directory,
                String.format("Basedata/%s_words.xml", docId)),
                sensibleEncoding());
        doc.autoload_markables = false;
        return doc;
    }

    public int DiscoursePositionFromDiscourseElementID(String id) {
        return _tokenIDs.lookupIndex(id);
    }

    public String getDiscourseElementAtDiscoursePosition(int token) {
        return _tokens[token];
    }

    public int leftmostTextPosition(int token) {
        return _start_pos[token];
    }

    public int rightmostTextPosition(int token) {
        return _end_pos[token];
    }

    public int getDiscourseElementCount() {
        return _tokens.length;
    }

    public String[] getDiscourseElementIDs(int start, int end) {
        assert end>=start;
        String[] a = new String[end - start + 1];
        for (int i = start; i <= end; i++) {
            a[i - start] = _tokenIDs.lookupObject(i);
        }
        return a;
    }
    
    public String[] getDiscourseElementIDs(int[] positions) {
    	String[] a = new String[positions.length];
    	for (int i=0; i<positions.length; i++) {
    		a[i]=_tokenIDs.lookupObject(positions[i]);
    	}
    	return a;
    }

    // takes a (potentially discontinuous) range specification
    // and returns the positions associated with it
    public int[] getPositions(String[] idRange) {
        TIntArrayList posns = new TIntArrayList();
        for (int i = 0; i < idRange.length; i += 2) {
            int start = getDiscoursePositionFromDiscourseElementID(idRange[i]);
            int end = getDiscoursePositionFromDiscourseElementID(idRange[i + 1]);
            for (int j = start; j <= end; j++) {
                posns.add(j);
            }
        }
        return posns.toArray();
    }

    public String[] getDiscourseElements(int start, int end) {
        String[] a = new String[end - start + 1];
        for (int i = start; i <= end; i++) {
            a[i - start] = _tokens[i];
        }
        return a;
    }

    public String getNameSpace() {
        return _docId;
    }

    public String[] getTokens() {
        return _tokens;
    }

    public int getDiscoursePositionFromDiscourseElementID(java.lang.String id) {
        int result=_tokenIDs.lookupIndex(id);
        if (result==-1) throw new IndexOutOfBoundsException("DiscourseElementID not found: " +id);
        return result;
    }

    public String getDiscourseElementIDAtDiscoursePosition(int pos) {
        return _tokenIDs.lookupObject(pos);
    }

    /** returns a sensible encoding for XML files.
     * If file.encoding has a sensible value (i.e., non-ASCII),
     * we use that, otherwise we use UTF-8.
     * @return the name of an encoding
     */
    public static String sensibleEncoding() {
        String encoding = System.getProperty("file.encoding");
        if (encoding.equals("ANSI_X3.4-1968")) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    public MarkableLevel getMarkableLevelByName(String name) {
        MarkableLevel lvl = _markableLevels.get(name);
        if (lvl != null) {
            return lvl;
        } else {
            File fname = new File(_corpusDir,
                    String.format("markables/%s_%s_level.xml", _docId, name));
            lvl = new MarkableLevel(this, name);
            if (autoload_markables) {
                try {
                    lvl.loadMarkables(new FileInputStream(fname),
                            guessEncoding(fname));
                } catch (FileNotFoundException ex) {
                    _logger.fine(String.format("markable level %s not on disk. Creating new.",
                            name));
                } catch (IOException ex) {
                    throw new RuntimeException(
                            String.format("error loading markable level %s", name),
                            ex);
                }
            }
            _markableLevels.put(name, lvl);
            return lvl;
        }
    }
    
    public RelationLevel getRelationLevelByName(String name) {
    	RelationLevel lvl = _relationLevels.get(name);
        if (lvl != null) {
            return lvl;
        } else {
            File fname = new File(_corpusDir,
                    String.format("markables/%s_%s_rels.xml", _docId, name));
            lvl = new RelationLevel(this, name);
            if (autoload_markables) {
                try {
                    lvl.loadTuples(new FileInputStream(fname),
                            guessEncoding(fname));
                } catch (FileNotFoundException ex) {
                    _logger.fine(String.format("relation level %s not on disk. Creating new.",
                            name));
                } catch (IOException ex) {
                    throw new RuntimeException(
                            String.format("error loading markable level %s", name),
                            ex);
                }
            }
            _relationLevels.put(name, lvl);
            return lvl;
        }
    }

    OutputStream openMarkableOut(String name) throws FileNotFoundException {
        File fname = new File(_corpusDir,
                String.format("markables/%s_%s_level.xml", _docId, name));
        return new FileOutputStream(fname);
    }
    
    OutputStream openRelationOut(String name) throws FileNotFoundException {
    	File fname = new File(_corpusDir,
    			String.format("markables/%s_%s_rels.xml", _docId, name));
    	return new FileOutputStream(fname);
    }

    public static String guessEncoding(File file) throws FileNotFoundException, IOException {
        String encoding = "ISO-8859-15";
        BufferedReader fr = new BufferedReader(new FileReader(file));
        String firstLine = fr.readLine();
        fr.close();
        if (firstLine != null) {
            Matcher m = xml_decl.matcher(firstLine);
            if (m.find()) {
                encoding = m.group(1);
                _logger.fine("detected encoding:" + encoding);
            }
        }
        return encoding;
    }

    private void loadBasedata(File file) {
        List<String> tokens = new ArrayList<String>();
        List<String> tokenIDs = new ArrayList<String>();
        TIntArrayList tokStart = new TIntArrayList();
        TIntArrayList tokEnd = new TIntArrayList();
        try {
            String encoding = "ISO-8859-15";
            BufferedReader fr = new BufferedReader(new FileReader(file));
            String firstLine = fr.readLine();
            fr.close();
            Matcher m = xml_decl.matcher(firstLine);
            if (m.find()) {
                encoding = m.group(1);
                _logger.fine("detected encoding:" + encoding);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                    System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new FileInputStream(file), guessEncoding(file));
            int eventType = xpp.getEventType();
            do {
                if (eventType == xpp.START_TAG && "word".equals(xpp.getName())) {
                    String word_id = null;
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        String att = xpp.getAttributeName(i);
                        String val = xpp.getAttributeValue(i);
                        if ("id".equals(att)) {
                            word_id = val;
                        } else if ("start".equals(att)) {
                            tokStart.add(Integer.parseInt(val));
                        } else if ("end".equals(att)) {
                            tokEnd.add(Integer.parseInt(val));
                        }
                    }
                    String word = xpp.nextText();
                    assert word_id != null;
                    tokens.add(word);
                    tokenIDs.add(word_id);
                }
                eventType = xpp.nextToken();
            } while (eventType != xpp.END_DOCUMENT);
            _tokens = new String[tokens.size()];
            _tokens = tokens.toArray(_tokens);
            _tokenIDs = new Alphabet<String>();
            for (String id : tokenIDs) {
                _tokenIDs.lookupIndex(id);
            }
            _tokenIDs.stopGrowth();
            if (tokens.size() == tokStart.size() &&
                    tokens.size() == tokEnd.size()) {
                _start_pos = tokStart.toArray();
                _end_pos = tokEnd.toArray();
            } else {
                assert tokStart.size() == 0;
                assert tokEnd.size() == 0;
            }
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("Error in serializing", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error in serializing", ex);
        }
    }

    public void saveAllLevels() {
        for (MarkableLevel lvl : _markableLevels.values()) {
            lvl.saveMarkables();
        }
        for (RelationLevel lvl: _relationLevels.values()) {
        	lvl.saveTuples();
        }
    }

    private void writeBasedata(File file, String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                    System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            XmlSerializer serializer = factory.newSerializer();
            serializer.setOutput(new FileOutputStream(file), encoding);
            serializer.startDocument(encoding, null);
            serializer.text("\n");
            serializer.docdecl(" words SYSTEM \"words.dtd\"");
            serializer.text("\n");
            serializer.startTag("", "words").text("\n");
            for (int i = 0; i < _tokens.length; i++) {
                serializer.startTag(null, "word");
                serializer.attribute(null, "id", _tokenIDs.lookupObject(i));
                if (_start_pos != null) {
                    serializer.attribute(null, "start",
                            Integer.toString(_start_pos[i]));
                    serializer.attribute(null, "end",
                            Integer.toString(_end_pos[i]));
                }
                serializer.text(_tokens[i]);
                serializer.endTag(null, "word").text("\n");
            }
            serializer.endTag("", "words");
            serializer.endDocument();
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("Error in serializing", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error in serializing", ex);
        }
    }

    private void writeMMAX(File file) {
        try {
            PrintStream wr = new PrintStream(new FileOutputStream(file));
            wr.println("<?xml version=\"1.0\"?>");
            wr.println("<mmax_project>");
            wr.println("<turns></turns>");
            wr.format("<words>%s_words.xml</words>\n", _docId);
            wr.println("<gestures></gestures>");
            wr.println("<keyactions></keyactions>");
            wr.println("<views>");
            wr.println("<stylesheet>muc_style.xsl</stylesheet>");
            wr.println("</views>");
            wr.println("</mmax_project>");
            wr.close();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot write .mmax file", ex);
        }
    }

    public static void main(String[] args) {
        String[] discourse = new String[]{"This", "is", "a", "test", "text", ".",
            "It", "contains", "several", "boring", "sentences", ".",
            "But", "it", "gets", "even", "worse", ":", "Look", "!"
        };
        MiniDiscourse disc = createFromTokens(new File(args[0]), args[1], discourse);
        MarkableLevel lvl = disc.getMarkableLevelByName("chunk");
        try {
            disc.writeBasedata(new File("/tmp/bla"),sensibleEncoding());
            lvl.saveMarkables(new FileOutputStream("/tmp/blu"),
                    sensibleEncoding());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.err.println("file.encoding=" + System.getProperty("file.encoding"));
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_corpusDir);
        out.writeObject(_docId);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _corpusDir = (File) in.readObject();
        _docId = (String) in.readObject();
        loadBasedata(new File(_corpusDir,
                String.format("Basedata/%s_words.xml", _docId)));
    }

    public void deleteAll() {
        new File(_corpusDir,
                String.format("Basedata/%s_words.xml", _docId)).delete();
        new File(_corpusDir, _docId + ".mmax").delete();
        File markable_dir = new File(_corpusDir, "markables");
        for (File markables_file : markable_dir.listFiles(
                new FilenameFilter() {

                    public boolean accept(File basedir, String fname) {
                        return (fname.startsWith(_docId + "_") &&
                                fname.endsWith("_level.xml"));
                    }
                })) {
            markables_file.delete();
        }
    }
}
