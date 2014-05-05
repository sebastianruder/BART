/*
 * ArrayUtilities
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.util;

/**
 * Miscellaneous array functions.
 *
 * @author Oliver Steele, steele@cs.brandeis.edu
 * @version 1.0
 */
public abstract class ArrayUtilities {
	/** Return the index of the first element of <var>array</var> that is <code>equal</code>
	 * to <var>value</var>, or <code>-1</code>, if no such element exists.
	 */
	public static int indexOf(Object[] array, Object value) {
		for (int i = 0; i < array.length; ++i) {
			if (array[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}
}
