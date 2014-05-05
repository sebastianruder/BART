/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long
 * as you preserve the copyright notice and this restriction, and label your
 * changes.
 */
package edu.brandeis.cs.steele.wn;
import java.io.*;
import java.util.*;

/**
  * A <code>Synset</code>, or <b>syn</b>onym <b>set</b>, represents a line of
  * a WordNet <var>pos</var><code>.data</code> file.  A <code>Synset</code>
  * represents a concept, and contains a set of <code>Word</code>s, each of
  * which has a sense that names that concept (and each of which is therefore
  * synonymous with the other words in the <code>Synset</code>).
  *
  * <code>Synset</code>'s are linked by {@link Pointer}s into a network of
  * related concepts; this is the <i>Net</i> in WordNet.
  * {@link Pointer#getTarget()} retrieves the targets of these links, and
  * {@link Pointer#parsePointer(FileBackedDictionary,Synset,int,TokenizerParser) parsePointer}
  * retrieves the pointers themselves.
  *
  * @see Word
  * @see Pointer
  * @author Oliver Steele, steele@cs.brandeis.edu
  * @version 1.0
 **/
public class Synset implements PointerTarget {
	/*
	 * Instance implementation
	 */
	protected FileBackedDictionary dictionary;
	protected POS pos;
	protected long offset;
	protected boolean isAdjectiveCluster;
	protected Word[] words;
	protected Pointer[] pointers;
	protected String gloss;

	//
	// Object initialization
	//
	Synset(FileBackedDictionary dictionary) {
		this.dictionary = dictionary;
	}

	Synset initializeFrom(String line) {
		TokenizerParser tokenizer = new TokenizerParser(line, " ");

		this.offset = tokenizer.nextLong();
		tokenizer.nextToken();	// lex_filenum
		String ss_type = tokenizer.nextToken();
		this.isAdjectiveCluster = false;
		if (ss_type.equals("s")) {
			ss_type = "a";
			this.isAdjectiveCluster = true;
		}
		this.pos = POS.lookup(ss_type);

		int wordCount = tokenizer.nextHexInt();
		this.words = new Word[wordCount];
		for (int i = 0; i < wordCount; i++) {
			String lemma = tokenizer.nextToken();
			int id = tokenizer.nextHexInt();
			int flags = Word.NONE;
			// strip the syntactic marker
			if (lemma.charAt(lemma.length() - 1) == ')' && lemma.indexOf('(') > 0) {
				int lparen = lemma.indexOf('(');
				String marker = lemma.substring(lparen + 1, lemma.length() - 1);
				lemma = lemma.substring(0, lparen - 1);
				if (marker.equals("p")) {
					flags |= Word.PREDICATIVE;
				} else if (marker.equals("a")) {
					flags |= Word.ATTRIBUTIVE;
				} else if (marker.equals("ip")) {
					flags |= Word.IMMEDIATE_POSTNOMINAL;
				} else {
					throw new RuntimeException("unknown syntactic marker " + marker);
				}
			}
			words[i] = new Word(this, i, lemma.replace('_', ' '), flags);
		}

		int pointerCount = tokenizer.nextInt();
		this.pointers = new Pointer[pointerCount];
		for (int i = 0; i < pointerCount; i++) {
			pointers[i] = Pointer.parsePointer(dictionary, this, i, tokenizer);
		}

		if (pos == POS.VERB) {
			int f_cnt = tokenizer.nextInt();
			for (int i = 0; i < f_cnt; i++) {
				tokenizer.nextToken();	// "+"
				int f_num = tokenizer.nextInt();
				int w_num = tokenizer.nextInt();
				if (w_num > 0) {
					words[w_num - 1].setVerbFrameFlag(f_num);
				} else {
					for (int j = 0; j < words.length; ++j) {
						words[j].setVerbFrameFlag(f_num);
					}
				}
			}
		}

		this.gloss = null;
		int index = line.indexOf('|');
		if (index > 0) {
			this.gloss = line.substring(index + 2).trim();
		}
		return this;
	}

	static Synset parseSynset(FileBackedDictionary dictionary, String line) {
		try {
			return new Synset(dictionary).initializeFrom(line);
		} catch (RuntimeException e) {
			System.err.println("Synset: while parsing " + line);
      e.printStackTrace();
			throw e;
		}
	}
	
	//
	// Object methods
	//
	public boolean equals(Object object) {
		return (object instanceof Synset)
			&& ((Synset) object).pos.equals(pos)
			&& ((Synset) object).offset == offset;
	}
	
	public int hashCode() {
		return pos.hashCode() ^ (int) offset;
	}
	
	public String toString() {
		return "[Synset " + offset + "@" + pos +": \""+ getDescription() +"\"]";
	}
	
	
	//
	// Accessors
	//
	public POS getPOS() {
		return pos;
	}
	
	public String getGloss() {
		return gloss;
	}

	public Word[] getWords() {
		return words;
	}

	public Word getWord(int index) {
		return words[index];
	}


	//
	// Description
	//
	public String getDescription() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < words.length; ++i) {
			if (i > 0) {
				buffer.append(", ");
			}
			buffer.append(words[i].lemma);
		}
		return buffer.toString();
	}
	
	public String getLongDescription() {
		String description = getDescription();
		String gloss = getGloss();
		if (gloss != null) {
			description += " -- (" + gloss + ")";
		}
		return description;
	}


	//
	// Pointers
	//
	//Added modified versions by Eric Bengtson to use lists for efficiency:
	protected static List<PointerTarget> collectTargetsList(
	    List<Pointer> pointersIn) {
		List<PointerTarget> targets = new ArrayList<PointerTarget>();
		for (Pointer p : pointersIn) {
			targets.add(p.getTarget());
		}
		return targets;
	}
	public List<PointerTarget> getTargetsList() {
		return collectTargetsList(getPointersList());
	}
	
	public List<PointerTarget> getTargetsList(PointerType type) {
		return collectTargetsList(getPointersList(type));
	}

	public List<Pointer> getPointersList() {
		return Arrays.asList(pointers);
	}

	public List<Pointer> getPointersList(PointerType type) {
		List<Pointer> targets = new Vector<Pointer>(pointers.length);
		for (Pointer pointer : pointers) {
			if (pointer.getType().equals(type)) {
				targets.add(pointer);
			}
		}
		return targets;
	}

	//End modified by Eric Bengtson.
	

	protected static PointerTarget[] collectTargets(Pointer[] pointers) {
		PointerTarget[] targets = new PointerTarget[pointers.length];
		for (int i = 0; i < pointers.length; ++i) {
			targets[i] = pointers[i].getTarget();
		}
		return targets;
	}
	
	public Pointer[] getPointers() {
		return pointers;
	}
	
	public Pointer[] getPointers(PointerType type) {
		//Modified by Eric: Made Generic:
		Vector<Pointer> vector = new Vector<Pointer>(pointers.length);
		for (int i = 0; i < pointers.length; ++i) {
			Pointer pointer = pointers[i];
			if (pointer.getType().equals(type)) {
				vector.addElement(pointer);
			}
		}
		Pointer[] targets = new Pointer[vector.size()];
		vector.copyInto(targets);
		return targets;
	}
	
	public PointerTarget[] getTargets() {
		return collectTargets(getPointers());
	}
	
	public PointerTarget[] getTargets(PointerType type) {
		return collectTargets(getPointers(type));
	}
}
