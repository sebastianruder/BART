/*
 * Cache utility classe
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.util;

/** A <code>Cache</code> is a collection of values that are indexed by keys and that are stored for an
 * unspecified amount of time (which the implementor of <code>Cache</code> may further specify).
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public interface Cache {
	/** Store <var>value</var> in the cache, indexed by <var>key</var>.  This operation makes
	 * it likely, although not certain, that a subsquent call to <code>get</code> with the
	 * same (<code>equal</code>) key will retrieve the same (<code>==</code>) value.
	 *
	 * <P>Multiple calls to <code>put</code> with the same <var>key</var> and <var>value</var>
	 * are idempotent.  A set of calls to <code>put</code> with the same <var>key</var> but
	 * different <var>value</var>s has only the affect of the last call (assuming there were
	 * no intervening calls to <code>get</code>).
	 */
	public void put(Object key, Object value);
	
	/** If <var>key</var> was used in a previous call to <code>put</code>, this call may
	 * return the <var>value</var> of that call.  Otherwise it returns <code>null</code>.
	 */
	public Object get(Object key);
	
	/** Remove all values stored in this cache.  Subsequent calls to <code>get</code>
	 * will return <code>null</code>.
	 */
	public void clear();
}