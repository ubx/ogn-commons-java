/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.db.ogn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;
import org.ogn.commons.db.FileDbDescriptorProvider;

public class OgnDbDescriptorProviderTest {

	@Test
	public void test() throws Exception {
		AircraftDescriptorProvider provider = new FileDbDescriptorProvider<OgnDb>(OgnDb.class,
				"src/test/resources/ogn-ddb.txt", 2000);

		assertNotNull(provider);

		Thread.sleep(1000);

		Optional<AircraftDescriptor> desc = provider.findDescriptor("DD83CE");
		assertTrue(desc.isPresent());		

		assertEquals("F-CLMT", desc.get().getRegNumber());

	}

}
