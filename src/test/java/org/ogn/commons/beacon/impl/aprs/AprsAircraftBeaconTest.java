/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ogn.commons.beacon.AddressType.FLARM;
import static org.ogn.commons.beacon.AircraftType.GLIDER;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.utils.AprsUtils;
import org.ogn.commons.utils.JsonUtils;

public class AprsAircraftBeaconTest {

    @Test
    public void testEqualsAndHashCode() {
        String acBeacon =

        "PH-844>APRS,qAS,Veendam:/102529h5244.42N/00632.07E'089/077/A=000876 id06DD82AC -474fpm +0.1rot 7.8dB 1e +0.7kHz gps2x3 hear8222";

        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);
        AircraftBeacon b2 = new AprsAircraftBeacon(acBeacon);

        assertEquals(b1.hashCode(), b2.hashCode());
        assertEquals(b1, b2);
        assertNotSame(b1, b2);
    }

    @Test
    public void test1() {
        String acBeacon = "PH-844>APRS,qAS,Veendam:/102529h5244.42N/00632.07E'089/077/A=000876 id06DD82AC -474fpm +0.1rot 7.8dB 1e +0.7kHz gps2x3 hear8222 hear8223 hear8224";

        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);

        assertNotNull(b1);

        assertEquals(acBeacon, b1.getRawPacket());

        assertEquals("PH-844", b1.getId());

        assertEquals(0, AprsUtils.toUtcTimestamp(10, 25, 29) - b1.getTimestamp());

        assertEquals(GLIDER, b1.getAircraftType());
        assertEquals(-2.4f, b1.getClimbRate(), 0.1);

        assertEquals(1, b1.getErrorCount());
        assertEquals(0.7, b1.getFrequencyOffset(), 0.01);
        assertEquals("2x3", b1.getGpsStatus());
        assertEquals(142.6f, b1.getGroundSpeed(), 0.01);
        assertEquals(3, b1.getHeardAircraftIds().length);
        List<String> hearIds = Arrays.asList(b1.getHeardAircraftIds());
        assertTrue(hearIds.contains("8222"));
        assertTrue(hearIds.contains("8223"));
        assertTrue(hearIds.contains("8224"));

        assertEquals("DD82AC", b1.getAddress());
        assertEquals(FLARM, b1.getAddressType());

        assertEquals(52.74033f, b1.getLat(), 0.0001);
        assertEquals(6.5345, b1.getLon(), 0.0001);
        assertEquals(267.0, b1.getAlt(), 0.001);

        assertEquals("Veendam", b1.getReceiverName());

        assertEquals(7.8, b1.getSignalStrength(), 0.01);
        assertEquals(89, b1.getTrack());
        assertEquals(0.1, b1.getTurnRate(), 0.01);
    }

    @Test
    public void test2() {
        String acBeacon = "incorrect > ! Cdd blah blah blah xxx beacon $$ format";

        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);

        assertEquals(acBeacon, b1.getRawPacket());

        // still, the object should be created (although its attributes won't be initialized)
        assertNotNull(b1);
    }

    @Test
    public void test3() {
        String acBeacon = "PH-1345>APRS,qAS,LFGP:/133244h4758.48N/00346.17E'274/028/A=001138 id06DD8652 -019fpm -10.6rot 18.8dB 0e -2.5kHz gps5x11 hear8E05 hear8F0F hearAA4A";

        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);

        assertEquals(-10.6f, b1.getTurnRate(), 0.0f);
    }

    @Test
    public void test4() {

        // NO gpsHxV (OGN tracker uses NMEA GPS, so these numbers will not be available)
        String acBeacon = "PH-1345>APRS,qAS,LFGP:/133244h4758.48N/00346.17E'274/028/A=001138 id06DD8652 -019fpm -10.6rot 18.8dB 0e -2.5kHz hear8E05 hear8F0F hearAA4A";

        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);

        assertNull(b1.getGpsStatus());
    }
    
    @Test
    public void test5() {
        String acBeacon = "FLRDDDBBC>APRS,qAS,UKGRF:/144659h5227.48N/00110.18E'182/045/A=000508 id06DDDBBC -177fpm +0.5rot 7.0dB 1e +2.0kHz, gps2x3";
        
        AircraftBeacon b1 = new AprsAircraftBeacon(acBeacon);
        acBeacon = "FLRDDDBBC>APRS,qAS,UKGRF:/144659h5227.48N/00110.18E'182/045/A=000508 id06DDDBBC -177fpm +0.5rot 7.0dB 1e +2.0kHz gps2x3";
        b1 = new AprsAircraftBeacon(acBeacon);
        
        System.out.println(JsonUtils.toJson(b1));
      
    }

}
