/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

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

    // "PH-844>APRS,qAS,Veendam:/102529h5244.42N/00632.07E'089/077/A=000876 id06DD82AC -474fpm +0.1rot 7.8dB 1e +0.7kHz gps2x3 hear8222";
   
    
    @Test
    public void test1() {
        String recBeacon = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";
        ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);

        assertNotNull(b1);

        assertEquals(recBeacon,b1.getRawPacket());
        
        assertEquals("EBZW", b1.getId());

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

    }

    @Test
    public void test2() {
        String recBeacon = "incorrect > ! Cdd blah blah blah xxx beacon $$ format";

        ReceiverBeacon b1 = new AprsReceiverBeacon(recBeacon);
        
        assertEquals(recBeacon,b1.getRawPacket());

        // still, the object should be created (although its attributes will not be initialized)
        assertNotNull(b1);
    }

}
