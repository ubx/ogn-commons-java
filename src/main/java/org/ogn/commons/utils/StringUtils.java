/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.utils;

/**
 * This class defines a set of utility methods operating on strings and commonly
 * used in the project
 * 
 */
public class StringUtils {

	public static String hex2ascii(String hex) {
		String output = new String();
		for (int i = 0; i < hex.length(); i += 2) {
			String str = hex.substring(i, i + 2);
			output += (char) Integer.parseInt(str, 16);
		}
		return output;
	}

	public static String asciiToHex(String asciiValue) {
		char[] chars = asciiValue.toCharArray();
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString(chars[i]));
		}
		return hex.toString();
	}
}