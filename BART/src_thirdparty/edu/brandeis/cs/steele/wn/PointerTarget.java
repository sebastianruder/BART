/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.wn;

import java.util.List;

/** A <code>PointerTarget</code> is the source or target of a <code>Pointer</code>.
 * The target of a semantic <code>PointerTarget</code> is a <code>Synset</code>;
 * the target of a lexical <code>PointerTarget</code> is a <code>Word</code>.
 *
 * @see Pointer
 * @see Synset
 * @see Word
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public interface PointerTarget {
	public POS getPOS();
	
	/** Return a description of the target.  For a <code>Word</code>, this is it's lemma;
	 * for a <code>Synset</code>, it's the concatenated lemma's of its <code>Word</code>s.
	 */
	public String getDescription();
	
	/** Return the long description of the target.  This is its description, appended by,
	 * if it exists, a dash and it's gloss.
	 */
	public String getLongDescription();
	
	/** Return the outgoing <code>Pointer</code>s from the target -- those <code>Pointer</code>s
	 * that have this object as their source.
	 */
	public Pointer[] getPointers();
	
	/** Return the outgoing <code>Pointer</code>s of type <var>type</var>. */
	public Pointer[] getPointers(PointerType type);
	
	/** Return the targets of the outgoing <code>Pointer</code>s. */
	public PointerTarget[] getTargets();
	
	/** Return the targets of the outgoing <code>Pointer</code>s that have type <var>type</var>. */
	public PointerTarget[] getTargets(PointerType type);


	//Added by Eric Bengtson for efficiency in some cases:

	/** Return the outgoing <code>Pointer</code>s from the target -- those <code>Pointer</code>s
	 * that have this object as their source.
	 */
	public List<Pointer> getPointersList();
	
	/** Return the outgoing <code>Pointer</code>s of type <var>type</var>. */
	public List<Pointer> getPointersList(PointerType type);
	
	/** Return the targets of the outgoing <code>Pointer</code>s. */
	public List<PointerTarget> getTargetsList();
	
	/** Return the targets of the outgoing <code>Pointer</code>s that have type <var>type</var>. */
	public List<PointerTarget> getTargetsList(PointerType type);

	//End added by Eric Bengtson.

}
