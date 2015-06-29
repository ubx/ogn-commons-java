/**
 * Copyright (c) 2014-2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ogn.commons.beacon.ReceiverBeacon;
import org.ogn.commons.utils.JsonUtils;

public class AprsReceiverBeaconTest {

	@Test
	public void testEqualsAndHashCode() {
		String recBeacon = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";

		ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);
		ReceiverBeacon b2 = new AprsReceiverBeacon(recBeacon);

		assertEquals(b1.hashCode(), b2.hashCode());
		assertEquals(b1, b2);
		assertNotSame(b1, b2);
	}

	@Test
	public void test1() {
		String recBeacon = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 v1.0.4.ARM CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";

		ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);

		assertNotNull(b1);

		System.out.println(JsonUtils.toJson(b1));

		assertEquals(recBeacon, b1.getRawPacket());

		assertEquals("EBZW", b1.getId());
		assertEquals("1.0.4", b1.getVersion());
		assertEquals(100004, b1.getNumericVersion());
		assertEquals("ARM", b1.getPlatform());

		assertEquals("GLIDERN1", b1.getServerName());

		assertEquals(51.0143333f, b1.getLat(), 1e-4);
		assertEquals(5.52383333f, b1.getLon(), 1e-4);
		assertEquals(90.8f, b1.getAlt(), 1e-2);

		assertEquals(968.2f, b1.getFreeRam(), 1e-2);
		assertEquals(1056.5, b1.getTotalRam(), 1e-2);

		assertEquals(0.9, b1.getCpuLoad(), 1e-2);
		assertEquals(Float.NaN, b1.getCpuTemp(), 0.001);

		assertEquals(1.5f, b1.getNtpError(), 1e-2);
		assertEquals(-20.0f, b1.getRtCrystalCorrection(), 1e-2);

		assertEquals(127, b1.getRecCrystalCorrection());
		assertEquals(-2.9, b1.getRecCrystalCorrectionFine(), 1e-2);
		assertEquals(127 + (-2.9), b1.getRecAbsCorrection(), 1e-2);

		assertEquals(4.3f, b1.getRecInputNoise(), 1e-2);

		recBeacon = "HHWaard>APRS,TCPIP*,qAC,GLIDERN2:/102540h5240.05NI00450.69E&/A=000020 CPU:1.1 RAM:223.5/458.7MB NTP:0.3ms/-14.8ppm +40.6C RF:+49+4.1ppm/+0.2dB";
		b1 = new AprsReceiverBeacon(recBeacon);
		assertEquals(40.6, b1.getCpuTemp(), 1e-2);
		assertNull(b1.getVersion());
		assertEquals(0, b1.getNumericVersion());
		assertNull(b1.getPlatform());

		// check a version with just noise in the RF
		recBeacon = "Solothurn>APRS,TCPIP*,qAC,GLIDERN2:/220227h4712.67NI00731.89E&/A=001509 v0.2.2 CPU:0.8 RAM:301.2/456.4MB NTP:0.8ms/-37.7ppm +34.2C RF:+0.99dB";
		b1 = new AprsReceiverBeacon(recBeacon);
		assertNotNull(b1);
		assertEquals(0.0f, b1.getRecAbsCorrection(), 1e-4);
		assertEquals(0, b1.getRecCrystalCorrection());
		assertEquals(0.0f, b1.getRecCrystalCorrectionFine(), 1e-4);
		assertEquals(0.99f, b1.getRecInputNoise(), 1e-4);

		recBeacon = "Gladbck>APRS,TCPIP*,qAC,GLIDERN2:/095759h5133.28NI00659.55E&/A=000154 v0.1.3 CPU:0.1 RAM:287.6/458.6MB NTP:0.3ms/-10.0ppm +40.1C RF:+53+1.0ppm";
		b1 = new AprsReceiverBeacon(recBeacon);
		assertNotNull(b1);
		assertEquals(53, b1.getRecCrystalCorrection());
		assertEquals(1.0f, b1.getRecCrystalCorrectionFine(), 1e-4);
		assertEquals(0.0f, b1.getRecInputNoise(), 1e-4);

		recBeacon = "LFLE>APRS,TCPIP*,qAC,GLIDERN1:/203735h4533.44NI00558.59E&020/010/A=000977 v0.2.3.x86 CPU:0.4 RAM:80.9/517.6MB NTP:0.7ms/-25.6ppm RF:+3.80dB";
		b1 = new AprsReceiverBeacon(recBeacon);

		assertEquals(20, b1.getTrack());
		assertEquals(18.52, b1.getGroundSpeed(), 1e-4);
		assertEquals("x86", b1.getPlatform());
	}

	@Test
	public void test2() {
		String recBeacon = "incorrect > ! Cdd blah blah blah xxx beacon $$ format";

		ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);

		assertEquals(recBeacon, b1.getRawPacket());

		// still, the object should be created (although its attributes will not
		// be initialized)
		assertNotNull(b1);
	}

}