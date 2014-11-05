package org.ogn.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ogn.commons.utils.StringUtils.asciiToHex;
import static org.ogn.commons.utils.StringUtils.hex2ascii;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testHexAscii() {

		String str1 = hex2ascii("54FF5543");
		assertNotNull(str1);
		String str2 = asciiToHex(str1);
		assertEquals("54FF5543", str2.toUpperCase());
	}

}
