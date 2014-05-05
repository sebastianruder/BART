package elkfed.mmax.minidisc;

public class Tuple {
	private final RelationLevel _level;
	private String[] _attrs;
	final int _id;
	
	Tuple(RelationLevel lvl, int id) {
		_level=lvl;
		_id=id;
		_attrs=new String[lvl.numAttrs()];
	}
	
	final void _set(int idx, String val) {
		if (_attrs==null || _attrs.length<=idx) {
			String[] newattrs=new String[idx+1];
			for (int i=0;i<_attrs.length;i++) {
				newattrs[i]=_attrs[i];
			}
			_attrs=newattrs;
		}
		_attrs[idx]=val;
	}
	public void set(int idx, String val) {
		_set(idx,val);
		_level.notifyChange(this, idx, _attrs[idx], val);
	}
	
	public void set(String attName, String val) {
		set(_level.getAttrIdx(attName),val);
	}

	public String get(int idx) {
		if (_attrs==null || idx>=_attrs.length) {
			return null;
		} else {
			return _attrs[idx];
		}
	}
	
	public String get(String attName) {
		return get(_level.getAttrIdx(attName));
	}
}
