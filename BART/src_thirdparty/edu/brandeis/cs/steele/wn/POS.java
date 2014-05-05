/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.wn;
import java.util.NoSuchElementException;

/** Instances of this class enumerate the possible major syntactic categories, or
 * <b>p</b>art's <b>o</b>f <b>s</b>peech.  Each <code>POS</code> has
 * a human-readable label that can be used to print it, and a key by which it can be looked up.
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public class POS {
	//
	// Class variables
	//
	public static final POS NOUN = new POS("noun", "n");
	public static final POS VERB = new POS("verb", "v");
	public static final POS ADJ = new POS("adjective", "a");
	public static final POS ADV = new POS("adverb", "r");
	
	/** A list of all <code>POS</code>s. */
	public static final POS[] CATS = {NOUN, VERB, ADJ, ADV};

	//
	// Instance implementation
	//
	protected String label;
	protected String key;

	protected POS(String label, String key) {
		this.label = label;
		this.key = key;
	}
	
	//
	// Object methods
	//
	public String toString() {
		return "[POS " + label + "]";
	}

	public boolean equals(Object object) {
		return (object instanceof POS) && key.equals(((POS) object).key);
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	//
	// Accessor
	//
	/** Return a label intended for textual presentation. */
	public String getLabel() {
		return label;
	}
  	
	/** Return the <code>PointerType</code> whose key matches <var>key</var>.
	 * @exception NoSuchElementException If <var>key</var> doesn't name any <code>POS</code>.
	 */
	public static POS lookup(String key) {
		for (int i = 0; i < CATS.length; ++i) {
			if (key.equals(CATS[i].key)) {
				return CATS[i];
			}
		}
		throw new NoSuchElementException("unknown POS " + key);
	}
}
