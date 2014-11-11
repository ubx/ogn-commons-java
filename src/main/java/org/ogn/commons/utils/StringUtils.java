/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class defines a set of utility methods operating on strings and commonly used in the project
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

    public static String md5(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            BigInteger i = new BigInteger(1, m.digest());
            return String.format("%1$032x", i);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}