/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.db.ogn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.Random;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.JsonUtils;

public class OgnDbTest {

	@Test
	public void test1() throws Exception {

		final OgnDb ogndb = new OgnDb("src/test/resources/ogn-ddb.txt");
		ogndb.reload();

		assertFalse(ogndb.getDescriptor(null).isPresent());

		Optional<AircraftDescriptor> desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc.get()));

		desc = ogndb.getDescriptor("DDDC04"); // FLARM address
		assertNotNull(desc);
		assertTrue(desc.isPresent());
		System.out.println(JsonUtils.toJson(desc.get()));

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

			desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc.get()));

			desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));

			desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));
			Thread.sleep(20 + r.nextInt(50));

			desc = ogndb.getDescriptor("some-not-existing");
			assertNotNull(desc);
			assertFalse(desc.isPresent());

			Thread.sleep(20 + r.nextInt(50));
			desc = ogndb.getDescriptor("DD4E9C"); // FLARM address
			assertNotNull(desc);
			assertTrue(desc.isPresent());
			System.out.println(JsonUtils.toJson(desc));

			assertEquals("G-DGIO", desc.get().getRegNumber());
			assertEquals("DG1", desc.get().getCN());
			assertNull(desc.get().getOwner());
			assertEquals("DG-100", desc.get().getModel());
			assertNull(desc.get().getFreq());

			desc = ogndb.getDescriptor("some-not-existing");
			assertNotNull(desc);
			assertFalse(desc.isPresent());

			Thread.sleep(100 + r.nextInt(100));
		}

		t1.join();
		t2.join();
	}

	@Test
	public void testMalformedUrl() throws Exception {
		final OgnDb ogndb = new OgnDb("http/live.glidernet.org/db/download");
		try {
			ogndb.reload();
		} catch (Exception ex) {
			fail("Exception not expected");
		}

		Optional<AircraftDescriptor> desc = ogndb.getDescriptor("DD83CE");
		assertNotNull(desc);
		assertFalse(desc.isPresent());
	}

	@Test
	public void testResourceNotFound() throws Exception {
		final OgnDb ogndb = new OgnDb("http://live.glidernet.org/wrong-location");
		try {
			ogndb.reload();
		} catch (Exception ex) {
			fail("Exception not expected");
		}

		Optional<AircraftDescriptor> desc = ogndb.getDescriptor("DD83CE");
		assertNotNull(desc);
		assertFalse(desc.isPresent());
	}
}