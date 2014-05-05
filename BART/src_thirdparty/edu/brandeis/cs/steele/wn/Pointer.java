/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.wn;
import java.util.*;
import edu.brandeis.cs.steele.util.*;
/** A Pointer encodes a lexical or semantic relationship between WordNet entities.  A lexical
 * relationship holds between Words; a semantic relationship holds between Synsets.  Relationships
 * are <it>directional</it>:  the two roles of a relationship are the <it>source</it> and <it>target</it>.
 * Relationships are <it>typed</it>: the type of a relationship is a {@link PointerType}, and can
 * be retrieved via {@link Pointer#getType getType}.
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public class Pointer {
	/** This class is used to avoid paging in the target before it is required, and to prevent
	 * keeping a large portion of the database resident once the target has been queried.
	 */
	protected class TargetIndex {
		POS pos;
		long offset;
		int index;
		
		TargetIndex(POS pos, long offset, int index) {
			this.pos = pos;
			this.offset = offset;
			this.index = index;
		}
	}

	//
	// Instance variables
	//
	protected FileBackedDictionary dictionary;
	protected Synset synset;
	
	/** The index of this Pointer within the array of Pointer's in the source Synset.
	 * Used by <code>equal</code>.
	 */
	protected int index;
	protected PointerType pointerType;
	protected PointerTarget source;
	
	/** An index that can be used to retrieve the target.  The first time this is used, it acts as
	 * an external key; subsequent uses, in conjunction with FileBackedDictionary's caching mechanism,
	 * can be thought of as a weak reference.
	 */
	protected TargetIndex targetIndex;
	
	//
	// Constructor and initialization
	//
	Pointer(FileBackedDictionary dictionary, Synset synset, int index) {
		this.dictionary = dictionary;
		this.synset = synset;
		this.index = index;
	}
	
	Pointer initializeFrom(TokenizerParser tokenizer) {
		this.pointerType = PointerType.parseKey(tokenizer.nextToken());

		long targetOffset = tokenizer.nextLong();


		POS pos = POS.lookup(tokenizer.nextToken());
		int linkIndices = tokenizer.nextHexInt();
		int sourceIndex = linkIndices / 256;
		int targetIndex = linkIndices & 255;
		
		this.source = resolveTarget(synset, sourceIndex);
		this.targetIndex = new TargetIndex(pos, targetOffset, targetIndex);
		return this;
	}
	
    static Pointer parsePointer(FileBackedDictionary dictionary, Synset source, int index, TokenizerParser tokenizer) {
		return new Pointer(dictionary, source, index).initializeFrom(tokenizer);
	}


	//
	// Object methods
	//
	public boolean equals(Object object) {
		return (object instanceof Pointer)
			&& ((Pointer) object).source.equals(source)
			&& ((Pointer) object).index == index;
	}
	
	public int hashCode() {
		return source.hashCode() + index;
	}
	
	public String toString() {
		return "[Link #" + index + " from " + source + "]";
	}
	
	//
	// Accessors
	//
	public PointerType getType() {
		return pointerType;
	}
	
	public boolean isLexical() {
		return source instanceof Word;
	}
	
	//
	// Targets
	//
	protected PointerTarget resolveTarget(Synset synset, int index) {
		if (index == 0) {
			return synset;
		} else {
			return synset.getWord(index - 1);
		}
	}
	
	public PointerTarget getSource() {
		return source;
	}
	
    public PointerTarget getTarget() {

		return resolveTarget(dictionary.getSynsetAt(targetIndex.pos, targetIndex.offset), targetIndex.index);

	}

     
}
