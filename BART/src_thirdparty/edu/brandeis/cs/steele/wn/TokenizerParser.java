/*
 * Utility classes
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.wn;
import java.util.*;

/** A <code>StringTokenizer</code> with extensions to retrieve the values of numeric tokens, as well as
 * strings.
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public class TokenizerParser extends StringTokenizer {
	public TokenizerParser(String string, String delimiters) {
		super(string, delimiters);
	}
	
	public int nextByte() {
		return Byte.parseByte(nextToken());
	}
	
	public int nextShort() {
		return Short.parseShort(nextToken());
	}
	
	public int nextInt() {
		return Integer.parseInt(nextToken());
	}
	
	public int nextInt(int radix) {
		return Integer.parseInt(nextToken(), radix);
	}
	
	public int nextHexInt() {
		return nextInt(16);
	}
	
	public long nextLong() {
		return Long.parseLong(nextToken());
	}
}