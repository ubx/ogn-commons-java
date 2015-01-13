/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.flarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.JsonUtils;

public class FlarmNetTest {

    @Test
    public void testWithRemoteFlarmnetDb() throws Exception {

        final FlarmNet fnet = new FlarmNet();
        fnet.reload();

        AircraftDescriptor desc = fnet.getDescriptor(null);
        assertNull(desc);

        desc = fnet.getDescriptor("DF08E8"); // FLARM address
        assertNotNull(desc);

        System.out.println(JsonUtils.toJson(desc));

        desc = fnet.getDescriptor("DDD587"); // FLARM address
        System.out.println(JsonUtils.toJson(desc));
        assertNotNull(desc);

        final Runnable reloader = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    // reload once more
                    fnet.reload();
                }
            }
        };

        Thread t1 = new Thread(reloader);
        Thread t2 = new Thread(reloader);
        t1.start();
        Thread.sleep(80);
        t2.start();

        Thread.sleep(100);

        Random r = new Random(System.currentTimeMillis());

        for (int i = 0; i < 5; i++) {

            desc = fnet.getDescriptor("406042"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);

            desc = fnet.getDescriptor("406042"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);
            desc = fnet.getDescriptor("406042"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);
            Thread.sleep(20 + r.nextInt(50));

            desc = fnet.getDescriptor("some-not-existing");
            assertNull(desc);

            Thread.sleep(20 + r.nextInt(50));
            desc = fnet.getDescriptor("406042"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);

            assertEquals("G-DEED", desc.getRegNumber());
            assertEquals("EED", desc.getCN());
            assertEquals("Bicester Gliding", desc.getOwner());
            assertEquals("Ka-8", desc.getModel());
            assertEquals("129.975", desc.getFreq());

            desc = fnet.getDescriptor("some-not-existing");
            assertNull(desc);

            Thread.sleep(100 + r.nextInt(100));
        }

        t1.join();
        t2.join();
    }

    @Test
    public void testCaching() {
        FlarmNet fnet = new FlarmNet();
        long t1 = System.currentTimeMillis();
        fnet.reload();
        AircraftDescriptor desc = fnet.getDescriptor("3ECE59"); // FLARM address
        assertNotNull(desc);
        System.out.println(JsonUtils.toJson(desc));
        assertTrue(desc.isKnown());
        assertEquals("D-KTCJ", desc.getRegNumber());
        long t2 = System.currentTimeMillis();
        long dt = t2 - t1;

        desc = fnet.getDescriptor("3ECE59"); // FLARM address
        desc = fnet.getDescriptor("3ECE59");
        desc = fnet.getDescriptor("3ECE59");
        desc = fnet.getDescriptor("3ECE59");
        assertNotNull(desc);
        System.out.println(JsonUtils.toJson(desc));
        assertTrue(desc.isKnown());
        assertEquals("D-KTCJ", desc.getRegNumber());
        long t3 = System.currentTimeMillis();
        long dt2 = t3 - t2;
        assertTrue(dt2 < dt);
    }

    @Test
    public void testWithLocalFlarmnetDb() throws Exception {
        FlarmNet fnet = new FlarmNet("src/test/resources/data.fln");
        fnet.reload();
        AircraftDescriptor desc = fnet.getDescriptor("DF08E8"); // FLARM address
        assertNotNull(desc);

        System.out.println(JsonUtils.toJson(desc));

        desc = fnet.getDescriptor("DDD587"); // FLARM address
        System.out.println(JsonUtils.toJson(desc));
        assertNotNull(desc);

        fnet = new FlarmNet("file:///src/test/resources/data.fln");
        fnet.reload();
        desc = fnet.getDescriptor("DF08E8"); // FLARM address
        assertNotNull(desc);
    }
}