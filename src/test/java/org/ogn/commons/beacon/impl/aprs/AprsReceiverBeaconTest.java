/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ogn.commons.beacon.ReceiverBeacon;

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
        String recBeacon = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 v1.0.4 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";

        ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);

        assertNotNull(b1);

        assertEquals(recBeacon, b1.getRawPacket());

        assertEquals("EBZW", b1.getId());
        assertEquals("1.0.4", b1.getVersion());
        assertEquals(100004, b1.getNumericVersion());

        assertEquals("GLIDERN1", b1.getServerName());

        assertEquals(51.0143333f, b1.getLat(), 0.0001);
        assertEquals(5.52383333f, b1.getLon(), 0.0001);
        assertEquals(90.8f, b1.getAlt(), 0.01);

        assertEquals(968.2f, b1.getFreeRam(), 0.01);
        assertEquals(1056.5, b1.getTotalRam(), 0.01);

        assertEquals(0.9, b1.getCpuLoad(), 0.01);
        assertEquals(Float.NaN, b1.getCpuTemp(), 0.001);

        assertEquals(1.5f, b1.getNtpError(), 0.01);
        assertEquals(-20.0f, b1.getRtCrystalCorrection(), 0.01);

        assertEquals(127, b1.getRecCrystalCorrection());
        assertEquals(-2.9, b1.getRecCrystalCorrectionFine(), 0.01);
        assertEquals(127 + (-2.9), b1.getRecAbsCorrection(), 0.01);

        assertEquals(4.3f, b1.getRecInputNoise(), 0.01);

        recBeacon = "HHWaard>APRS,TCPIP*,qAC,GLIDERN2:/102540h5240.05NI00450.69E&/A=000020 CPU:1.1 RAM:223.5/458.7MB NTP:0.3ms/-14.8ppm +40.6C RF:+49+4.1ppm/+0.2dB";
        b1 = new AprsReceiverBeacon(recBeacon);
        assertEquals(40.6, b1.getCpuTemp(), 0.01);
        assertNull(b1.getVersion());
        assertEquals(0, b1.getNumericVersion());

        // check a version with just noise in the RF
        recBeacon = "Solothurn>APRS,TCPIP*,qAC,GLIDERN2:/220227h4712.67NI00731.89E&/A=001509 v0.2.2 CPU:0.8 RAM:301.2/456.4MB NTP:0.8ms/-37.7ppm +34.2C RF:+0.99dB";
        b1 = new AprsReceiverBeacon(recBeacon);
        assertNotNull(b1);
        assertEquals(0.0f, b1.getRecAbsCorrection(), 0.0001);
        assertEquals(0, b1.getRecCrystalCorrection());
        assertEquals(0.0f, b1.getRecCrystalCorrectionFine(), 0.0001);
        assertEquals(0.99f, b1.getRecInputNoise(), 0.0001);

    }

    @Test
    public void test2() {
        String recBeacon = "incorrect > ! Cdd blah blah blah xxx beacon $$ format";

        ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);

        assertEquals(recBeacon, b1.getRawPacket());

        // still, the object should be created (although its attributes will not be initialized)
        assertNotNull(b1);
    }

}