/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.JsonUtils;

public class AircraftDescriptorImplTest {

    @Test
    public void testBasicInterface() {
        AircraftDescriptor d1 = new AircraftDescriptorImpl("G-EEBM", "EBM", "YGC", "SUTTON BANK", "Grob Astir CS",
                "129.975");
        assertNotNull(d1);

        assertEquals("G-EEBM", d1.getRegNumber());
        assertEquals("EBM", d1.getCN());
        assertEquals("YGC", d1.getOwner());
        assertEquals("SUTTON BANK", d1.getHomeBase());
        assertEquals("Grob Astir CS", d1.getModel());
        assertEquals("129.975", d1.getFreq());

    }

    @Test
    public void testEqualsAndHashCode() {
        AircraftDescriptor d1 = new AircraftDescriptorImpl("G-EEBM", "EBM", "YGC", "SUTTON BANK", "Grob Astir CS",
                "129.975");

        AircraftDescriptor d2 = new AircraftDescriptorImpl("G-EEBM", "EBM", "YGC", "SUTTON BANK", "Grob Astir CS",
                "129.975");

        assertEquals(d1.hashCode(), d2.hashCode());
        assertEquals(d1, d2);
        assertNotSame(d1, d2);
    }

    @Test
    public void testJson() {
        AircraftDescriptor d1 = new AircraftDescriptorImpl("G-EEBM", "EBM", "YGC", "SUTTON BANK", "Grob Astir CS",
                "129.975");

        AircraftDescriptor d2 = JsonUtils.fromJson(JsonUtils.toJson(d1), AircraftDescriptorImpl.class);

        assertNotNull(d2);
        assertEquals(d1, d2);
        assertNotSame(d1, d2);
    }
}
