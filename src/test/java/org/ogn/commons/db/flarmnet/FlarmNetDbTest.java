/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.db.flarmnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.JsonUtils;

public class FlarmNetDbTest {

	@Test
	@Ignore
	// this test is disabled, because FlarmNet db is no longer remotely
	// available to OGN
	public void testWithRemoteFlarmnetDb() throws Exception {

		final FlarmNetDb fnet = new FlarmNetDb();
		fnet.reload();

		Optional<AircraftDescriptor> desc = fnet.getDescriptor(null);
		assertFalse(desc.isPresent());

		desc = fnet.getDescriptor("DF08E8"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc.get()));

		desc = fnet.getDescriptor("DDD587"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc.get()));

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
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));

			desc = fnet.getDescriptor("406042"); // ICAO address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));

			desc = fnet.getDescriptor("406042"); // ICAO address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));
			Thread.sleep(20 + r.nextInt(50));

			desc = fnet.getDescriptor("some-not-existing");
			assertNotNull(desc);
			assertFalse(desc.isPresent());

			Thread.sleep(20 + r.nextInt(50));
			desc = fnet.getDescriptor("406042"); // ICAO address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));

			assertEquals("G-DEED", desc.get().getRegNumber());
			assertEquals("EED", desc.get().getCN());
			assertEquals("Bicester Gliding", desc.get().getOwner());
			assertEquals("Ka-8", desc.get().getModel());
			assertEquals("129.975", desc.get().getFreq());

			desc = fnet.getDescriptor("some-not-existing");
			assertNull(desc);

			Thread.sleep(100 + r.nextInt(100));
		}

		t1.join();
		t2.join();
	}

	@Test
	public void testCaching() {
		FlarmNetDb fnet = new FlarmNetDb("src/test/resources/data.fln");
		long t1 = System.currentTimeMillis();
		fnet.reload();
		Optional<AircraftDescriptor> desc = fnet.getDescriptor("F72345"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc));

		assertEquals("PH-1341", desc.get().getRegNumber());
		long t2 = System.currentTimeMillis();
		long dt = t2 - t1;

		desc = fnet.getDescriptor("F72345"); // FLARM address
		desc = fnet.getDescriptor("F72345");
		desc = fnet.getDescriptor("F72345");
		desc = fnet.getDescriptor("F72345");
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc));

		assertEquals("PH-1341", desc.get().getRegNumber());
		long t3 = System.currentTimeMillis();
		long dt2 = t3 - t2;
		assertTrue(dt2 < dt);
	}

	@Test
	public void testWithLocalDb() throws Exception {
		FlarmNetDb fnet = new FlarmNetDb("src/test/resources/data.fln");
		fnet.reload();
		Optional<AircraftDescriptor> desc = fnet.getDescriptor("DF08E8"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc));

		desc = fnet.getDescriptor("DDD587"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc));

		fnet = new FlarmNetDb("file:///src/test/resources/data.fln");
		fnet.reload();
		desc = fnet.getDescriptor("DF08E8"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
	}
}