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

}
