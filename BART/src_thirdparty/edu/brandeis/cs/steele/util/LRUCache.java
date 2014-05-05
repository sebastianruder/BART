/*
 * LRUCache utility class
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.util;
import java.util.*;

/** A fixed-capacity <code>Cache</code> that stores the <var>n</var> most recently used
 * keys.
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public class LRUCache implements Cache {
	protected int capacity;
	
	//Modified by Eric Bengtson: Made Generic
	protected Vector<Object> keys;
	protected Hashtable<Object,Object> map;
	
	public LRUCache(int capacity) {
		this.capacity = capacity;
		//Modified by Eric: Made Generic:
		keys = new Vector<Object>(capacity);
		map = new Hashtable<Object,Object>(capacity);
	}
	
	public synchronized void put(Object key, Object value) {
		remove(key);
		keys.insertElementAt(key, 0);
		map.put(key, value);
		if (keys.size() >= capacity) {
			remove(keys.elementAt(keys.size() - 1));
		}
	}
	
	public synchronized Object get(Object key) {
		Object value = map.get(key);
		if (value != null) {
			keys.removeElement(key);
			keys.insertElementAt(key, 0);
		}
		return value;
	}
	
	public synchronized void remove(Object key) {
		if (map.remove(key) != null) {
			keys.removeElement(key);
			--capacity;
		}
	}
	
	public synchronized void clear() {
		keys.removeAllElements();
		map.clear();
	}
}
