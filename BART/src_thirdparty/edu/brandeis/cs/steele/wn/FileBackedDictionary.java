/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long
 * as you preserve the copyright notice and this restriction, and label your
 * changes.
 */
package edu.brandeis.cs.steele.wn;

import edu.brandeis.cs.steele.util.*;
import java.io.*;
import java.util.*;

/**
  * A <code>DictionaryDatabase</code> that retrieves objects from the text
  * files in the WordNet distribution directory.
  * 
  * A <code>FileBackedDictionary</code> has an <i>entity cache</i>.  The
  * entity cache is used to resolve multiple temporally contiguous lookups of
  * the same entity to the same object -- for example, successive calls to
  * <code>lookupIndexWord</code> with the same parameters would return the
  * same value (<code>==</code> as well as <code>equals</code>), as would
  * traversal of two <code>Pointer</code>s that shared the same target.  The
  * current implementation uses an LRU cache, so it's possible for two
  * different objects to represent the same entity, if their retrieval is
  * separated by other database operations.  The LRU cache will be replaced by
  * a cache based on WeakHashMap, once JDK 1.2 becomes more widely available.
  *
  * @see edu.brandeis.cs.steele.util.Cache
  * @see edu.brandeis.cs.steele.util.LRUCache
  * @author Oliver Steele, steele@cs.brandeis.edu
  * @version 1.0
 **/
public class FileBackedDictionary implements DictionaryDatabase {
	protected final FileManagerInterface db;

	//
	// Constructors
	//
	
	/**
    * Construct a DictionaryDatabase that retrieves file data from
    * <code>fileManager</code>.  A client can use this to create a
    * DictionaryDatabase backed by a RemoteFileManager.
    * @see RemoteFileManager
   **/
	public FileBackedDictionary(FileManagerInterface fileManager) {
		this.db = fileManager;
	}

	/** Construct a dictionary backed by a set of files contained in the default WN search directory.
	  * See {@link FileManager} for a description of the location of the default search directory. */
	public FileBackedDictionary() {
		this(new FileManager());
	}
	
	/** Construct a dictionary backed by a set of files contained in <var>searchDirectory</var>. */
	public FileBackedDictionary(String searchDirectory) {
		this(new FileManager(searchDirectory));
	}
	
	
	//
	// Entity lookup caching
	//
	protected final int DEFAULT_CACHE_CAPACITY = 1000;
	protected Cache entityCache = new LRUCache(DEFAULT_CACHE_CAPACITY);
	
	protected class DatabaseKey {
		POS pos;
		Object key;
		
		DatabaseKey(POS pos, Object key) {
			this.pos = pos;
			this.key = key;
		}
		
		public boolean equals(Object object) {
			return object instanceof DatabaseKey
				&& ((DatabaseKey) object).pos.equals(pos)
				&& ((DatabaseKey) object).key.equals(key);
		}
		
		public int hashCode() {
			return pos.hashCode() ^ key.hashCode();
		}
	}
	
	/** Set the dictionary's entity cache.
	 */
	public void setEntityCache(Cache cache) {
		if (entityCache != cache) {
			entityCache.clear();
			entityCache = cache;
		}
	}
	
	
	//
	// File name computation
	//
	protected static final POS[] POS_KEYS = {POS.NOUN, POS.VERB, POS.ADJ, POS.ADV};
	protected static final String[] POS_FILENAME_ROOTS = {"noun", "VERB", "adj", "adv"};
	
	protected static String getDatabaseSuffixName(POS pos) {
		int index = ArrayUtilities.indexOf(POS_KEYS, pos);
		return POS_FILENAME_ROOTS[index];
	}
	
	protected static String getDataFilename(POS pos) {
		return "data." + getDatabaseSuffixName(pos);
	}
	
	protected static String getIndexFilename(POS pos) {
		return "index." + getDatabaseSuffixName(pos);
	}
	
	protected static String getExceptionsFilename(POS pos) {
		return getDatabaseSuffixName(pos) + ".exc";
	}
	
	
	//
	// Entity retrieval
	//
	protected IndexWord getIndexWordAt(POS pos, long offset) {
    	DatabaseKey key = new DatabaseKey(pos, new Long(offset));
		IndexWord word = (IndexWord) entityCache.get(key);
		if (word == null) {
			String filename = getIndexFilename(pos);
			String line;
			try {
				line = db.readLineAt(filename, offset);
			} catch (IOException e) {
				throw new RuntimeException(e.toString());
			}
			word = IndexWord.parseIndexWord(this, line);
			entityCache.put(key, word);
		}
		return word;
	}
	
	
	protected Synset getSynsetAt(POS pos, long offset, String line) {
    	DatabaseKey key = new DatabaseKey(pos, new Long(offset));
		Synset synset = (Synset) entityCache.get(key);
		if (synset == null) {
			if (line == null) {
				String filename = getDataFilename(pos);
				try {
					line = db.readLineAt(filename, offset);
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
			}
			synset = Synset.parseSynset(this, line);

			entityCache.put(key, synset);
		}
		return synset;
	}
	
	 public Synset getSynsetAt(POS pos, long offset) {
		return getSynsetAt(pos, offset, null);
	}
	

	//
	// Lookup functions
	//
    public IndexWord lookupIndexWord(POS pos, String string) {
    	DatabaseKey key = new DatabaseKey(pos, string);
		IndexWord word = (IndexWord) entityCache.get(key);
		if (word == null) {
			String filename = getIndexFilename(pos);
			long offset;
			try {
				offset = db.getIndexedLinePointer(filename, string.toLowerCase().replace(' ', '_'));
			} catch (IOException e) {
				throw new RuntimeException(e.toString());
			}
			if (offset >= 0) {
				word = getIndexWordAt(pos, offset);
			}              
			if (word != null) {
				entityCache.put(key, word);
			}
		}
		return word;
	}
	
	public String lookupBaseForm(POS pos, String derivation) {
		String filename = getExceptionsFilename(pos);
		try {
			long offset = db.getIndexedLinePointer(filename, derivation.toLowerCase());
			if (offset >= 0) {
				String line = db.readLineAt(filename, offset);
				return line.substring(line.indexOf(' ') + 1);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
		return null;
	}
	
	
	//
	// Iterators
	//
	public Enumeration searchIndexWords(final POS pos, final String substring) {
		return new LookaheadEnumeration(new Enumeration() {
			protected String filename = getIndexFilename(pos);
			protected long nextOffset = 0;
			
			public boolean hasMoreElements() {
				return true;
			}
			
			public Object nextElement() {
				try {
					long offset = db.getMatchingLinePointer(filename, nextOffset, substring);
					if (offset >= 0) {
						Object value = getIndexWordAt(pos, offset);
						nextOffset = db.getNextLinePointer(filename, offset);
						return value;
					} else {
						throw new NoSuchElementException();
					}
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
			}
		});
	}

	public Enumeration synsets(final POS pos) {
		return new LookaheadEnumeration(new Enumeration() {
			protected String filename = getDataFilename(pos);
			protected long nextOffset = 0;
			
			public boolean hasMoreElements() {
				return true;
			}
			
			public Object nextElement() {
				try {
					String line;
					do {
						if (nextOffset < 0) {
							throw new NoSuchElementException();
						}
						line = db.readLineAt(filename, nextOffset);
						nextOffset = db.getNextLinePointer(filename, nextOffset);
					} while (line.startsWith("  "));
					return getSynsetAt(pos, nextOffset, line);
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
			}
		});
	}
}

