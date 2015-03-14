/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.db.ogn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.JsonUtils;

public class OgnDbTest {

    @Test
    public void testWithRemoteDb() throws Exception {

        final OgnDb ogndb = new OgnDb();
        ogndb.reload();

        AircraftDescriptor desc = ogndb.getDescriptor(null);
        assertNull(desc);

        desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
        assertNotNull(desc);

        System.out.println(JsonUtils.toJson(desc));

        desc = ogndb.getDescriptor("DDDC04"); // FLARM address
        System.out.println(JsonUtils.toJson(desc));
        assertNotNull(desc);

        final Runnable reloader = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    // reload once more
                    ogndb.reload();
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

            desc = ogndb.getDescriptor("48497C"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);

            desc = ogndb.getDescriptor("48497C"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);
            desc = ogndb.getDescriptor("48497C"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);
            Thread.sleep(20 + r.nextInt(50));

            desc = ogndb.getDescriptor("some-not-existing");
            assertNull(desc);

            Thread.sleep(20 + r.nextInt(50));
            desc = ogndb.getDescriptor("48497C"); // ICAO address
            System.out.println(JsonUtils.toJson(desc));
            assertNotNull(desc);

            assertEquals("PH-GZC", desc.getRegNumber());
            assertEquals("PZC", desc.getCN());
            assertNull(desc.getOwner());
            assertEquals("Towplane", desc.getModel());
            assertNull(desc.getFreq());

            desc = ogndb.getDescriptor("some-not-existing");
            assertNull(desc);

            Thread.sleep(100 + r.nextInt(100));
        }

        t1.join();
        t2.join();
    }

    @Test
    public void testCaching() {
        OgnDb ogndb = new OgnDb();
        long t1 = System.currentTimeMillis();
        ogndb.reload();
        AircraftDescriptor desc = ogndb.getDescriptor("DD83CE"); // FLARM address
        assertNotNull(desc);
        System.out.println(JsonUtils.toJson(desc));
        assertTrue(desc.isKnown());
        assertEquals("F-CLMT", desc.getRegNumber());
        long t2 = System.currentTimeMillis();
        long dt = t2 - t1;

        desc = ogndb.getDescriptor("DD83CE"); // FLARM address
        desc = ogndb.getDescriptor("DD83CE");
        desc = ogndb.getDescriptor("DD83CE");
        desc = ogndb.getDescriptor("DD83CE");
        assertNotNull(desc);
        System.out.println(JsonUtils.toJson(desc));
        assertTrue(desc.isKnown());
        assertEquals("F-CLMT", desc.getRegNumber());
        long t3 = System.currentTimeMillis();
        long dt2 = t3 - t2;
        assertTrue(dt2 < dt);
    }
}