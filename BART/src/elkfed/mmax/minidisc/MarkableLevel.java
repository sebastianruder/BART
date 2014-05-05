/*
 * Copyright 2007-2008 Yannick Versley / Univ. Tuebingen
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

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 *
 * @author versley
 */
public class MarkableLevel {

    private static final Logger _logger = Logger.getLogger("elkfed.mmax.minidisc");
    //TODO: add more efficient data structure for accessing markables
    // by their start/end position
    // e.g. minLength, maxLength for markables and indexing by start
    public static Comparator<Markable> default_order =
            MiniDiscourse.DISCOURSEORDERCMP;
    private final SortedSet<Markable> _markables =
            new TreeSet<Markable>(default_order);
    private final TIntObjectHashMap<Markable> _markables_by_id =
            new TIntObjectHashMap<Markable>();
    private MiniDiscourse _doc;
    private String _name;
    int _lastId = 0;
    private boolean _isDirty;

    MarkableLevel(MiniDiscourse doc, String name) {
        _doc = doc;
        _name = name;
        _isDirty = false;
    }

    public void deleteAllMarkables() {

        _markables.clear();
        _markables_by_id.clear();
        _lastId = 0;
        _isDirty = true;
        _logger.info("deleting markables on level " + _name);
    }

    public Markable getMarkableByID(String id) {
        int id_i = MarkableHelper.parseId(id, "markable");
        return _markables_by_id.get(id_i);
    }

    //TODO: use TreeSet to speed this up
    public List<Markable> getMarkablesAtDiscourseElementID(String string) {
        int pos = _doc.getDiscoursePositionFromDiscourseElementID(string);
        return getMarkablesAtDiscoursePosition(pos);
    }

    public List<Markable> getMarkablesAtDiscourseElementID(String string,
            java.util.Comparator<Markable> comp) {
        int pos = _doc.getDiscoursePositionFromDiscourseElementID(string);
        List<Markable> result = getMarkablesAtDiscoursePosition(pos);
        if (comp != null && !comp.equals(default_order)) {
            Collections.sort(result, comp);
        }
        return result;
    }

    public MiniDiscourse getDocument() {
        return _doc;
    }

    public Markable addMarkable(int start, int end, Map<String, String> attributes) {
        HashMap<String, String> attrs = new HashMap<String, String>(attributes);
        Markable m = new Markable(start, end, this, attrs, _lastId);
        assert start < _doc.getDiscourseElementCount() :
                String.format("%s : end %d >= %d",
                this._name, start, _doc.getDiscourseElementCount());
        assert end < _doc.getDiscourseElementCount() :
                String.format("%s : end %d >= %d",
                this._name, end, _doc.getDiscourseElementCount());
        addMarkable(m);
        _lastId++;
        _isDirty = true;
        return m;
    }

    public void copyMarkables(MarkableLevel lvl2) {
        // adds all markables from lvl2 as copies
        for (Markable m: lvl2._markables) {
            if (_markables_by_id.containsKey(m.getIntID())) {
                throw new UnsupportedOperationException("Already have " +
                        "a markable "+m.getIntID());
            }
            Markable m2=m.copy(this);
            addMarkable(m2);
        }
    }
    
    private void addMarkable(Markable m) {
        _markables_by_id.put(m.getIntID(), m);
        _markables.add(m);
    }

    public void deleteMarkable(Markable markable) {
        _markables_by_id.remove(markable.getIntID());
        _markables.remove(markable);
        _isDirty = true;
    }

    public List<Markable> getMarkables() {
        return getMarkables(MiniDiscourse.DISCOURSEORDERCMP);
    }

    public List<Markable> getMarkables(Comparator comp) {
        List<Markable> result = new ArrayList<Markable>(_markables);
        if (comp != null && !comp.equals(default_order)) {
            Collections.sort(result, comp);
        }
        return result;
    }

    public void setIsDirty(boolean b) {
        _isDirty = b;
    }

    void adjustSpan(Markable m, int new_start, int new_end) {
        //TODO: adjust span-based indices
        _isDirty = true;
    }

    public String getName() {
        return _name;
    }

    public List<Markable> getMarkablesAtDiscoursePosition(int pos) {
        List<Markable> result = new ArrayList<Markable>();
        for (Markable m : _markables) {
            if (m.getLeftmostDiscoursePosition() <= pos &&
                    m.getRightmostDiscoursePosition() >= pos) {
                result.add(m);
            }
        }
        return result;
    }

    public List<Markable> getMarkablesAtDiscoursePosition(int pos,
            Comparator<Markable> order) {
        List<Markable> result = getMarkablesAtDiscoursePosition(pos);
        if (!order.equals(default_order)) {
            Collections.sort(result, order);
        }
        return result;
    }

    public Markable[] getMarkablesAtSpan(int start,int end) {
        Markable[] result=new Markable[end-start+1];
        for (Markable m: _markables) {
            int mpos=m.getLeftmostDiscoursePosition();
            if (mpos>=start && mpos<=end) {
                result[mpos-start]=m;
            }
        }
        return result;
    }

    /** returns markables grouped by their attributes.
     *  nonexistant attribute values will be counted as
     *  like a unique value (i.e. not equal to anything else)
     * @param attName
     * @return
     */
    public List<List<Markable>> getGroupedBy(String attName) {
        List<List<Markable>> result = new ArrayList<List<Markable>>();
        TObjectIntHashMap<String> posns = new TObjectIntHashMap<String>();
        for (Markable m : _markables) {
            String val1 = m.getAttributeValue(attName);
            if (val1 == null) {
                Markable[] a = new Markable[]{m};
                List<Markable> ms = Arrays.asList(a);
            } else {
                List<Markable> ms;
                int pos = posns.get(val1);
                if (pos == 0) {
                    pos = result.size() + 1;
                    posns.put(val1, pos);
                    ms = new ArrayList<Markable>();
                    result.add(ms);
                } else {
                    ms = result.get(pos - 1);
                }
                ms.add(m);
            }
        }
        return result;
    }

    public void loadMarkables(InputStream is) {
        loadMarkables(is,"ISO-8859-15");
    }

    public void loadMarkables(InputStream is, String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                    System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            XmlPullParser xpp = factory.newPullParser();
            // by default, assume that markables files are ISO encoded
            // this is needed because MXParser ignores the encoding
            // in the XML header.
            xpp.setInput(is,encoding);
            int eventType = xpp.getEventType();
            do {
                if (eventType == xpp.START_TAG && "markable".equals(xpp.getName())) {
                    int start = -1, end = -1;
                    int id = -1;
                    int[] holes = null;
                    HashMap<String, String> attrs = new HashMap<String, String>();
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        String att = xpp.getAttributeName(i);
                        String val = xpp.getAttributeValue(i);
                        if ("id".equals(att)) {
                            id = MarkableHelper.parseId(val, "markable");
                            attrs.put(att, val); //olga: store the original id as attr, reason -- ids get messed up, no retrieval possible
                        } else if ("span".equals(att)) {
                            String[] range = MarkableHelper.parseRanges(val);
                            //System.out.println(Arrays.asList(range));
                            start = _doc.getDiscoursePositionFromDiscourseElementID(range[0]);
                            end = _doc.getDiscoursePositionFromDiscourseElementID(range[range.length - 1]);
                            if (start>end) {
                                throw new RuntimeException(
                                        String.format("Markable %s on level %s: invalid span %s",
                                            id, _name, val));
                            }
                            if (range.length > 2) {
         //                       holes = Arrays.copyOfRange(holes, 1, range.length - 1);


         holes = new int[range.length-2];
 for (int k=1; k<range.length-1; k++) {
 holes[k-1]=_doc.getDiscoursePositionFromDiscourseElementID(range[k]);

//        System.out.println("K: " + k);
//System.out.println("RANGE K-1:" + range[k-1]);
//System.out.println("RANGE K:" + range[k]);
//        System.out.println("HOLES K-1: " + holes[k-1]);
//        System.out.println("HOLES K: " + holes[k]);
}

                            }
                            attrs.put(att, val); //keep span as attr as well
                        } else if ("mmax_level".equals(att)) {
                        // ignore
                        } else {
                            attrs.put(att, val);
                        }
                    }
                    assert start >= 0;
                    assert id >= 0;
                    if (id > _lastId) {
                        _lastId = id;
                    }
                    Markable m = new Markable(start, end, this, attrs, id);
                    if (holes != null) {
                        m.setHoles(holes);
                    }
                    addMarkable(m);
                }
                eventType = xpp.nextToken();
            } while (eventType != xpp.END_DOCUMENT);
            setIsDirty(false);
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("Cannot parse stream", ex);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("IOException while parsing level %s",_name),
                    ex);
        }
    }

    public void saveMarkables() {
        try {
            OutputStream os = _doc.openMarkableOut(this._name);
            saveMarkables(os,MiniDiscourse.sensibleEncoding());
            setIsDirty(false);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Cannot save markables on level " + _name, ex);
        }
    }

    void saveMarkables(OutputStream os,String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                    System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            XmlSerializer serializer = factory.newSerializer();
            serializer.setOutput(os,encoding);
            serializer.startDocument(encoding, null);
            serializer.text("\n");
            serializer.docdecl(" markables SYSTEM \"markables.dtd\"");
            serializer.text("\n");
            String defaultNS = "www.eml.org/NameSpaces/" + _name;
            serializer.setPrefix("", defaultNS);
            serializer.startTag(defaultNS, "markables");
            serializer.text("\n");
            for (Markable m : _markables) {
                serializer.startTag(null, "markable");
                serializer.attribute(null, "id", m.getID());
                int start = m.getLeftmostDiscoursePosition();
                int end = m.getRightmostDiscoursePosition();
                int[] holes = m.getHoles();
                StringBuffer buf = new StringBuffer();
                if (holes != null) {
                    for (int holeOff = 0; holeOff < holes.length; holeOff += 2) {
                        int pos = holes[holeOff];
                        if (start == pos) {
                            buf.append(_doc.getDiscourseElementIDAtDiscoursePosition(start));
                        } else {
                            buf.append(_doc.getDiscourseElementIDAtDiscoursePosition(start)).append("..").append(_doc.getDiscourseElementIDAtDiscoursePosition(pos));
                        }
                        buf.append(",");
                        start = holes[holeOff + 1];
                    }
                }
                if (start == end) {
                    buf.append(_doc.getDiscourseElementIDAtDiscoursePosition(
                            start));
                } else {
                    buf.append(
                            _doc.getDiscourseElementIDAtDiscoursePosition(
                            start)).append("..").append(
                            _doc.getDiscourseElementIDAtDiscoursePosition(
                            end));
                }
                serializer.attribute(null, "span",
                        buf.toString());


                Map<String, String> atts = m.getAttributes();
                for (String att : atts.keySet()) {
                    if (!"mmax_level".equals(att)) {
                        serializer.attribute(null, att, atts.get(att));
                    }
                }
                serializer.attribute(null, "mmax_level", _name);
                serializer.endTag(null, "markable");
                serializer.text("\n");
            }
            serializer.endTag(defaultNS, "markables");
            serializer.text("\n");
            serializer.endDocument();
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("cannot serialize", ex);
        } catch (IOException ex) {
            throw new RuntimeException("IOError in saving stream", ex);
        }
    }
    /**
     *
     *
     *
     * @param attributeName the attributes name usually "tag"
     * @param attributeValue the attribute value that is searched for
     * @param start left discourse position
     * @param end right discrouse position
     * @return true if the attribute value is in range, false otherwise
     */
    public boolean isLevelAttributeValueInRange(String attributeName, String attributeValue, int start, int end) {
        Markable[] markables = getMarkablesAtSpan(start - 1, end - 1);
        for (Markable markable : markables) {
            if(markable==null) {
                continue;
            }
            if (markable.getAttributeValue(attributeName).equalsIgnoreCase(attributeValue)) {
                return true;
            }
        }
        return false;
    }
}
