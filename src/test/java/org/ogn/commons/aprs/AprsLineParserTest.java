/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.aprs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.OgnBeacon;
import org.ogn.commons.beacon.ReceiverBeacon;
import org.ogn.commons.beacon.impl.aprs.AprsLineParser;

public class AprsLineParserTest {

    @Test
    public void test() {

        String acBeacon1 = "PH-844>APRS,qAS,Veendam:/102529h5244.42N/00632.07E'089/077/A=000876 id06DD82AC -474fpm +0.1rot 7.8dB 1e +0.7kHz gps2x3 hear8222";
        String brBeacon1 = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";

        AprsLineParser parser = AprsLineParser.get();

        AprsLineParser parser2 = AprsLineParser.get();

        // parser is expected to be a singleton
        assertSame(parser, parser2);

        OgnBeacon beacon = parser.parse(acBeacon1);

        assertNotNull(beacon);
        assertTrue(beacon instanceof AircraftBeacon);

        // ignore aircraft beacons
        beacon = parser.parse(acBeacon1, false, true);
        assertNull(beacon);

        beacon = parser.parse(brBeacon1);

        assertNotNull(beacon);
        assertTrue(beacon instanceof ReceiverBeacon);

        // ignore receiver beacons
        beacon = parser.parse(brBeacon1, true, false);
        assertNull(beacon);
    }

    @Test
    public void test2() {
        String brBeacon = "FLRDDDBBC>APRS,qAS,UKGRF:/140044h5227.15N/00108.34E'286/023/A=001200 id06DDDBBC +653fpm +0.7rot 9.0dB 0e +1.8kHz gps2x3 hearE61E";

        String recBeacon = "EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 v1.0.4 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm RF:+127-2.9ppm/+4.3dB";

        AprsLineParser parser = AprsLineParser.get();

        OgnBeacon beacon = parser.parse(brBeacon);

        assertNotNull(beacon);
        assertTrue(beacon instanceof AircraftBeacon);

        beacon = parser.parse(recBeacon);

        assertNotNull(beacon);
        assertTrue(beacon instanceof ReceiverBeacon);
        
        recBeacon = "ESSX>APRS,TCPIP*,qAC,GLIDERN1:/213502h5934.92NI01630.18E&/A=000026 NTP:16000.0ms/+0.0ppm +37.0C RF:+49+0.0ppm/+0.9dB";
        beacon = parser.parse(recBeacon);

        assertNotNull(beacon);
        assertTrue(beacon instanceof ReceiverBeacon);               
    }

}
