/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long
 * as you preserve the copyright notice and this restriction, and label your
 * changes.
 */
package edu.brandeis.cs.steele.wn;
import java.util.*;
import edu.brandeis.cs.steele.util.*;

/**
  * An <code>IndexWord</code> represents a line of the
  * <var>pos</var><code>.index</code> file.  An <code>IndexWord</code> is
  * created retrieved or retrieved via
  * {@link DictionaryDatabase#lookupIndexWord}, and has a <i>lemma</i>, a
  * <i>pos</i>, and a set of <i>senses</i>, which are of type {@link Synset}.
  *
  * @author Oliver Steele, steele@cs.brandeis.edu
  * @version 1.0
 **/
public class IndexWord {
	protected FileBackedDictionary dictionary;
	protected POS pos;
	protected long offset;
	protected String lemma;
	protected int taggedSenseCount;
	// senses are initially stored as offsets, and paged in on demand.
	protected long[] synsetOffsets;
	/** This is null until getSenses has been called. */
	protected Synset[] synsets;

        protected PointerType[] ptrTypes = null;
	//
	// Initialization
	//
	IndexWord(FileBackedDictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	IndexWord initializeFrom(String line) {
		TokenizerParser tokenizer = new TokenizerParser(line, " ");
		this.lemma = tokenizer.nextToken().replace('_', ' ');
		this.pos = POS.lookup(tokenizer.nextToken());

		tokenizer.nextToken();	// poly_cnt
		int p_cnt = tokenizer.nextInt();
                ptrTypes = new PointerType[p_cnt];
		for (int i = 0; i < p_cnt; i++) {
                        try {
                            ptrTypes[i] = PointerType.parseKey(tokenizer.nextToken());
                        } catch (java.util.NoSuchElementException exc) {
                                exc.printStackTrace();
                        }
		}

		int senseCount = tokenizer.nextInt();
		this.taggedSenseCount = tokenizer.nextInt();
		this.synsetOffsets = new long[senseCount];
		for (int i = 0; i < senseCount; i++) {
			synsetOffsets[i] = tokenizer.nextLong();
		}
		
		return this;
	}

	static IndexWord parseIndexWord(FileBackedDictionary dictionary, String line) {
		//try {
			return new IndexWord(dictionary).initializeFrom(line);
		//} catch (RuntimeException e) {
			//System.err.println("IndexWord: while parsing " + line);
      //e.printStackTrace();
			//throw e;
		//}
	}


	//
	// Object methods
	//
	public boolean equals(Object object) {
		return (object instanceof IndexWord)
			&& ((IndexWord) object).pos.equals(pos)
			&& ((IndexWord) object).offset == offset;
	}
	
	public int hashCode() {
		return pos.hashCode() ^ (int) offset;
	}
	
	public String toString() {
		return "[IndexWord " + offset + "@" + pos.getLabel() + ": \"" + lemma + "\"]";
	}

	//
	// Accessors
	//
	public POS getPOS() {
		return pos;
	}

        /**
            The pointer types available for this indexed word.  May not apply to all senses of the word.
        */
        public PointerType[] getPointerTypes() {
            return ptrTypes;
        }
	
	/** Return the word's <it>lemma</it>.  Its lemma is its orthographic representation, for
	 * example <code>"dog"</code> or <code>"get up"</code>.
	 */
	public String getLemma() {
		return lemma;
	}
	
	public int getTaggedSenseCount() {
		return taggedSenseCount;
	}

	public Synset[] getSenses() {
		if (synsets == null) {
			synsets = new Synset[synsetOffsets.length];
			for (int i = 0; i < synsetOffsets.length; ++i) {
				synsets[i] = dictionary.getSynsetAt(pos, synsetOffsets[i]);
			}
		}
		return synsets;
	}
}
