package elkfed.mmax.minidisc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import elkfed.ml.util.Alphabet;


public class RelationLevel {
	final class Index {
		int _idx;
		Map<String, List<Tuple>> _entries=new HashMap<String,List<Tuple>>();
		Index(int idx) {_idx=idx;}
		void add(Tuple t) {
			String val=t.get(_idx);
			add(t,val);
		}
		void add(Tuple t, String val) {
			if (val==null) return;
			List<Tuple> lst=_entries.get(val);
			if (lst==null) {
				lst=new ArrayList<Tuple>();
				_entries.put(val, lst);
			}
			lst.add(t);
		}
		void remove(Tuple t, String val) {
			if (val==null) return;
			List<Tuple> lst=_entries.get(val);
			if (lst==null) {
				throw new IllegalStateException(
						String.format("Indexing hiccup on level %s for attr %s",
								_name, _attrNames.lookupObject(_idx)));
			}
			lst.remove(t);
		}
		List<Tuple> find(String val) {
			return _entries.get(val);			
		}
	}
	private MiniDiscourse _doc;
	private boolean _dirty=false;
	private int _last_id=0;
	private final String _name;
	private Alphabet<String> _attrNames=new Alphabet<String>();
	private List<Tuple> _tuples =
		new ArrayList<Tuple>();
	private List<Index> _indices =
		new ArrayList<Index>();
	public RelationLevel(MiniDiscourse doc, String name) {
		_doc=doc;
		_name=name;
	}
	
	public String getName() {
		return _name;
	}
	
	public int numAttrs() {
		return _attrNames.size();
	}
	
	public int getAttrIdx(String attName) {
		return _attrNames.lookupIndex(attName);
	}
	
	Tuple createRelation(String[] attNames, String[] attVals) {
		int[] idx=new int[attNames.length];
		for (int i=0; i<attNames.length; i++) {
			idx[i]=_attrNames.lookupIndex(attNames[i]);
		}
		Tuple rel=new Tuple(this, _last_id++);
		for (int i=0; i<attNames.length; i++) {
			rel._set(idx[i], attVals[i]);
			if (idx[i]<_indices.size()) {
				Index searchIndex=_indices.get(idx[i]);
				if (searchIndex!=null) {
					searchIndex.add(rel);
				}
			}
		}
		_tuples.add(rel);
		return rel;
	}

	public List<Tuple> find(String attName, String attVal) {
		int idx=_attrNames.lookupIndex(attName);
		while (idx>=_indices.size()) {
			_indices.add(null);
		}
		Index searchIndex=_indices.get(idx);
		if (searchIndex==null) {
			searchIndex=new Index(idx);
			for (Tuple t: _tuples) {
				searchIndex.add(t);
			}
			_indices.set(idx,searchIndex);
		}
		return searchIndex.find(attVal);
	}
	
	public List<Tuple> find(String... atts) {
		if (atts.length%2==1) {
			throw new IllegalArgumentException("Need pairs of attribute and value");
		} else if (atts.length==0) {
			return _tuples;
		} else {
			List<Tuple> result0=find(atts[0], atts[1]);
			if (atts.length==2) {
				return result0;
			}
			List<Tuple> result=new ArrayList<Tuple>(result0.size());
			final int nAtts=atts.length/2-1;
			int[] attIdx=new int[atts.length/2-1];
			for (int i=0; i<nAtts; i++) {
				attIdx[i]=_attrNames.lookupIndex(atts[2+2*i]);
			}
			for (Tuple t: result0) {
				boolean wanted=true;
				for (int i=0; i<nAtts; i++) {
					String val1=atts[3+2*i];
					String val2=t.get(attIdx[i]);
					if (val1==null ? val2!=null :
						!val1.equals(val2)) {
						wanted=false;
						break;
					}
				}
				if (wanted) result.add(t);
			}
			return result;
		}
	}
	
	public void notifyChange(Tuple tuple, int idx,
			String oldVal, String newVal) {
		if (idx<_indices.size()) {
			Index searchIndex=_indices.get(idx);
			if (searchIndex!=null) {
				searchIndex.remove(tuple, oldVal);
				searchIndex.add(tuple, newVal);
			}
		}
	}
	
	public void loadTuples(InputStream is, String encoding) {
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
                if (eventType == XmlPullParser.START_TAG && "rel".equals(xpp.getName())) {
                	Tuple tup=new Tuple(this, _last_id++);
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        int attIdx =
                        	_attrNames.lookupIndex(xpp.getAttributeName(i));
                        String val = xpp.getAttributeValue(i);
                        tup.set(attIdx, val);
                    }
                }
                eventType = xpp.nextToken();
            } while (eventType != XmlPullParser.END_DOCUMENT);
            _dirty=false;
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("Cannot parse stream", ex);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("IOException while parsing level %s",_name),
                    ex);
        }
	}
	
    public void saveTuples(OutputStream os,String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                    System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            XmlSerializer serializer = factory.newSerializer();
            serializer.setOutput(os,encoding);
            serializer.startDocument(encoding, null);
            serializer.text("\n");
            serializer.startTag(null, "tuples");
            serializer.text("\n");
            for (Tuple m : _tuples) {
            	serializer.startTag(null, "rel");
                for (int i=0; i<_attrNames.size(); i++) {
                	String val=m.get(i);
                	if (val!=null) {
                		serializer.attribute(null,_attrNames.lookupObject(i), val);
                	}
                }
                serializer.endTag(null, "rel");
                serializer.text("\n");
            }
            serializer.endTag(null, "tuples");
            serializer.text("\n");
            serializer.endDocument();
        } catch (XmlPullParserException ex) {
            throw new RuntimeException("cannot serialize", ex);
        } catch (IOException ex) {
            throw new RuntimeException("IOError in saving stream", ex);
        }
    }
    
    public void saveTuples() {
        try {
            OutputStream os = _doc.openRelationOut(_name);
            saveTuples(os,MiniDiscourse.sensibleEncoding());
            _dirty=false;
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Cannot save markables on level " + _name, ex);
        }
    }

    
    public static void main(String[] args) {
    	RelationLevel rlevel=new RelationLevel(null, "foobar");
    	rlevel.createRelation(new String[]{"a","b"},
    			new String[]{"1","2"});
      	rlevel.createRelation(new String[]{"a","c"},
    	    			new String[]{"A","C"});
      	rlevel.createRelation(new String[]{"c","b"},
    			new String[]{"CC","2"});
      	rlevel.saveTuples(System.out, "UTF-8");
    }
}
