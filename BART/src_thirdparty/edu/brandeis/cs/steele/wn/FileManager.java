/*

 * WordNet-Java

 *

 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve

 * the copyright notice and this restriction, and label your changes.

 */

package edu.brandeis.cs.steele.wn;

import java.io.*;

import java.util.*;



/** An implementation of FileManagerInterface that reads files from the local file system.

 * a file.  <code>FileManager</code> caches the file position before and after

 * <code>readLineAt</code> in order to eliminate the redundant IO activity that a naive implementation

 * of these methods would necessitate.

 *

 * Instances of this class are guarded.  Operations are synchronized by file.

 *

 * @author Oliver Steele, steele@cs.brandeis.edu

 * @version 1.0

 */

public class FileManager implements FileManagerInterface {

	//

	// Class variables

	//



	/** The API version, used by <CODE>RemoteFileManager</CODE> for constructing a binding name. */

	public static String VERSION = "1.0";



	/** Set this to true to enable debugging messages in <code>getIndexedLinePointer</code>. */

	public static final boolean TRACE_LOOKUP = false;

	

	// work around some bugs in the Metrowerks VM

	protected static final boolean IS_MW_VM = System.getProperties().getProperty("java.vendor").equalsIgnoreCase("Metrowerks Corp.");

	

	//

	// Instance variables

	//

	protected String searchDirectory;

	//Modified by Eric Bengtson: Made Generic:
	protected Hashtable<String,RandomAccessFile> filenameCache
	 = new Hashtable<String,RandomAccessFile>();

	

	protected class NextLineCache {

		protected String filename;

		protected long previous;

		protected long next;

		

		void setNextLineOffset(String filename, long previous, long next) {

			this.filename = filename;

			this.previous = previous;

			this.next = next;

		}

		

		boolean matchingOffset(String filename, long offset) {

			return this.filename != null && previous == offset && this.filename.equals(filename);

		}

		

		long getNextOffset() {

			return next;

		}

	};

	protected NextLineCache nextLineCache = new NextLineCache();

	

	//

	// Constructors

	//

	/** Construct a file manager backed by a set of files contained in the default WN search directory.

	 * The default search directory is the location named by the system property WNSEARCHDIR; or, if this

	 * is undefined, by the directory named WNHOME/Database (under MacOS) or WNHOME/dict (otherwise);

	 * or, if the WNHOME is undefined, the current directory (under MacOS), "C:\wn16" (WIndows),

	 * or "/usr/local/wordnet1.6" (otherwise).

	 */

	public FileManager() {

		this(getWNSearchDir());

	}

	

	/** Construct a file manager backed by a set of files contained in <var>searchDirectory</var>. */

	public FileManager(String searchDirectory) {

		this.searchDirectory = searchDirectory;

	}

	

	

	//

	// Filename caching

	//

	protected static final boolean IS_WINDOWS_OS = System.getProperties().getProperty("os.name").startsWith("Windows");

	protected static final boolean IS_MAC_OS = System.getProperties().getProperty("os.name").startsWith("Mac");



	// work around a bug in the MW installation

	protected static final String fileSeparator = IS_MW_VM ? ":" : File.separator;

	

	protected static String getWNHome() {

		String home = System.getProperty("WNHOME");

		if (home != null) {

			return home;

		} else if (IS_WINDOWS_OS) {

			return "C:\\wn16";

		} else if (IS_MAC_OS) {

			return ".";

		} else {

			return "/usr/local/wordnet1.6";

		}

	}

	

	protected static String getWNSearchDir() {

		String searchDir = System.getProperty("WNSEARCHDIR");

		if (searchDir != null) {

			return searchDir;

		} else if (IS_MAC_OS && getWNHome().equals("."))

			return "Database";

		else {

			return getWNHome() + fileSeparator + (IS_MAC_OS ? "Database" : "dict");

		}

	}

	

	static String mapToWindowsFilename(String filename) {

		if (filename.startsWith("data.")) {

			filename = filename.substring("data.".length()) + ".dat";

		} else if (filename.startsWith("index.")) {

			filename = filename.substring("index.".length()) + ".idx";

		}

		return filename;

	}

	

	protected synchronized RandomAccessFile getFileStream(String filename) throws IOException {

		if (IS_WINDOWS_OS) {

			filename = mapToWindowsFilename(filename);

		}

		RandomAccessFile stream = (RandomAccessFile) filenameCache.get(filename);

		if (stream == null) {

			String pathname = searchDirectory + fileSeparator + filename;

			stream = new RandomAccessFile(pathname, "r");

			filenameCache.put(filename, stream);

		}

		return stream;

	}

	

	//

	// IO primitives

	//

	

	// work around a bug in Metrowerks Java

	protected String readLine(RandomAccessFile stream) throws IOException {

		if (IS_MW_VM) {

			StringBuffer input = new StringBuffer();

			int c;



			while (((c = stream.read()) != -1) && (c != '\n') && c != '\r') {

				input.append((char) c);

			}

			if ((c == -1) && (input.length() == 0)) {

				return null;

			}

			return input.toString();

		} else {

			return stream.readLine();

		}

	}

	

	protected void skipLine(RandomAccessFile stream) throws IOException {

		int c;

		

		while (((c = stream.read()) != -1) && c != '\n' && c != '\r') { }

	}

	

	//

	// Line-based interface methods

	//

	public String readLineAt(String filename, long offset) throws IOException {

		RandomAccessFile stream = getFileStream(filename);

		synchronized (stream) {

			stream.seek(offset);

			String line = readLine(stream);


			long nextOffset = stream.getFilePointer();

			if (line == null) {

				nextOffset = -1;

			}
			nextLineCache.setNextLineOffset(filename, offset, nextOffset);

			return line;

		}

	}

	

	protected String readLineWord(RandomAccessFile stream) throws IOException {

		StringBuffer input = new StringBuffer();

		int c;



		while (((c = stream.read()) != -1) && c != '\n' && c != '\r' && c != ' ') {

			input.append((char) c);

		}

		if ((c == -1) && (input.length() == 0)) {

			return null;

		}

		if (c == ' ') {

			skipLine(stream);

		}

		return input.toString();

	}

	

	public long getNextLinePointer(String filename, long offset) throws IOException {

		RandomAccessFile stream = getFileStream(filename);

		synchronized (stream) {

			if (nextLineCache.matchingOffset(filename, offset)) {

				return nextLineCache.getNextOffset();

			}

			stream.seek(offset);

			skipLine(stream);

			return stream.getFilePointer();

		}

	}

	

	//

	// Searching

	//

	public long getMatchingLinePointer(String filename, long offset, String substring) throws IOException {

		RandomAccessFile stream = getFileStream(filename);

		synchronized (stream) {

			stream.seek(offset);

			do {

				String line = readLineWord(stream);

				long nextOffset = stream.getFilePointer();

				if (line == null) {

					return -1;

				}

				nextLineCache.setNextLineOffset(filename, offset, nextOffset);

				if (line.indexOf(substring) >= 0) {

					return offset;

				}

				offset = nextOffset;

			} while (true);

		}

	}

	

	public long getIndexedLinePointer(String filename, String target) throws IOException {

		RandomAccessFile stream = getFileStream(filename);

		synchronized (stream) {

			long start = 0;

			long stop = stream.length();

			while (true) {

				long midpoint = (start + stop) / 2;

				stream.seek(midpoint);

				skipLine(stream);

				long offset = stream.getFilePointer();

				if (TRACE_LOOKUP) {

					System.out.println("  "+start+", "+((start+stop)/2)+", "+stop+" -> "+offset);

				}

				if (offset == start) {

					return -1;

				} else if (offset == stop) {

					stream.seek(start + 1);

					skipLine(stream);

					if (TRACE_LOOKUP) {

						System.out.println(". "+stream.getFilePointer());

					}

					while (stream.getFilePointer() < stop) {

						long result = stream.getFilePointer();

						String line = readLineWord(stream);

						if (TRACE_LOOKUP) {

							System.out.println(". "+line+" -> "+line.equals(target));

						}

						if (line.equals(target)) {

							return result;

						}

					}

					return -1;

				}

				long result = stream.getFilePointer();

				String line = readLineWord(stream);

				if (line.equals(target)) return result;

				int compare = compare(target, line);

				if (TRACE_LOOKUP) {

					System.out.println(line + ": " + compare);

				}

				if (compare > 0) {

					start = offset;

				} else if (compare < 0) {

					stop = offset;

				} else {

					return result;

				}

			}

		}

	}

	

	/** Return a negative value if a precedes b, a positive value if a follows b,

	 * otherwise 0. */

	protected int compare(String a, String b) {

		int maxLength = Math.min(a.length(), b.length());

		for (int i = 0; i < maxLength; i++) {

			int d = a.charAt(i) - b.charAt(i);

			if (d != 0) {

				return d;

			}

		}

		if (a.length() < maxLength) {

			return 1;

		} else if (maxLength < b.length()) {

			return -1;

		} else {

			return 0;

		}

	}

}
