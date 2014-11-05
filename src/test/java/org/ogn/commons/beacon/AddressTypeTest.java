/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AddressTypeTest {

    @Test
    public void test() {

        AddressType atype = AddressType.forValue(AddressType.ICAO.getCode());
        assertEquals(AddressType.ICAO, atype);

        atype = AddressType.forValue(AddressType.FLARM.getCode());
        assertEquals(AddressType.FLARM, atype);

        atype = AddressType.forValue(AddressType.OGN.getCode());
        assertEquals(AddressType.OGN, atype);

        atype = AddressType.forValue(AddressType.RANDOM.getCode());
        assertEquals(AddressType.RANDOM, atype);

        atype = AddressType.forValue(-1);
        assertEquals(AddressType.UNRECOGNIZED, atype);
    }

}
