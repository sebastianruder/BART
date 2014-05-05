/*
 * Lookahead utility classe
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.util;
import java.util.*;

/** A wrapper for objects that declared <code>Enumeration</code>s that don't fully implement
 * <code>hasMoreElements</code>, to bring them into conformance with the specification of that
 * function.
 *
 * It's sometimes difficult to determine whether a next element exists without trying to generate
 * it.  (This is particularly true when reading elements from a stream.)  Unfortunately, the
 * <code>Enumeration</code> protocol distributes the work of determining whether another
 * element exists, and supplying it, across two methods.  A call that implements an enumerator that terminates on
 * failure to generate must therefore cache the next result.  This class can be used as a
 * wrapper, to cache the result independently of the generator logic.  <code>LookaheadEnumeration.hasMoreElements</code>
 * returns false when <code>hasMoreElements</code> of the wrapped object returns false,
 * <it>or</it> when <code>nextElement</code> of the wrapped class 
 *
 * <P>An <code>Enumeration</code> that supplies the lines of a file until the file ends
 * can be written thus:
 * <PRE>
 * new LookaheadEnumeration(new Enumeration() {
 *   InputStream input = ...;
 *   public boolean hasMoreElements() { return true; }
 *   public Object nextElement() {
 *     String line = input.readLine();
 *     if (line == null) {
 *       throw new NoSuchElementException();
 *     }
 *     return line;
 *   }
 * }
 * </PRE>
 *
 * <P>An <code>Enumeration</code> that generates the natural numbers below the first with
 * the property <var>p</var> can be written thus:
 * <PRE>
 * new LookaheadEnumeration(new Enumeration() {
 *   int n = 0;
 *   public boolean hasMoreElements() { return true; }
 *   public Object nextElement() {
 *     int value = n++;
 *     if (p(value)) {
 *       throw new NoSuchElementException();
 *     }
 *     return value;
 *   }
 * }
 * </PRE>
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public class LookaheadEnumeration implements Enumeration {
	protected Enumeration ground;
	protected boolean peeked = false;
	protected Object nextObject;
	protected boolean more;
	
	public LookaheadEnumeration(Enumeration ground) {
		this.ground = ground;
	}
	
	protected void lookahead() {
		if (!peeked) {
			more = ground.hasMoreElements();
			if (more) {
				try {
					nextObject = ground.nextElement();
				} catch (NoSuchElementException e) {
					more = false;
				}
			}
			peeked = true;
		}
	}
		
	public boolean hasMoreElements() {
		lookahead();
		return more;
	}
	
	public Object nextElement() {
		lookahead();
		if (more) {
			Object result = nextObject;
			nextObject = null;	// to facilite GC
			peeked = false;
			return result;
		} else {
			throw new NoSuchElementException();
		}
	}
}