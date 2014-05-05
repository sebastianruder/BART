/*

 * WordNet-Java

 *

 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve

 * the copyright notice and this restriction, and label your changes.

 */

package edu.brandeis.cs.steele.wn;

import java.util.*;



/** An object in a class that implements this interface is a broker or factory for objects that model WordNet lexical

 * and semantic entities.

 *

 * @see FileBackedDictionary

 * @author Oliver Steele, steele@cs.brandeis.edu

 * @version 1.0

 */

public interface DictionaryDatabase {

	/** Look up a word in the database.  The search is case-independent,

	 * and phrases are separated by spaces ("look up", not "look_up").

	 * @param pos The part-of-speech.

	 * @param lemma The orthographic representation of the word.

	 * @return An IndexWord representing the word, or <code>null</code> if no such entry exists.

	 */

    public IndexWord lookupIndexWord(POS pos, String lemma);

    

    /** Return the base form of an exceptional derivation, if an entry for it exists

     * in the database.

	 * @param pos The part-of-speech.

	 * @param derivation The inflected form of the word.

	 * @return The uninflected word, or null if no exception entry exists.

	 */

    public String lookupBaseForm(POS pos, String derivation);

    

    /** Return an enumeration of all the IndexWords whose lemmas contain <var>substring</var>

     * as a substring.

	 * @param pos The part-of-speech.

	 * @return An enumeration of <code>IndexWord</code>s.

	 */

	public Enumeration searchIndexWords(POS pos, String substring);



	/** Return an enumeration over all the Synsets in the database.

	 * @param pos The part-of-speech.

	 * @return An enumeration of <code>Synset</code>s.

	 */

	public Enumeration synsets(POS pos);

}
