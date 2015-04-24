/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.utils;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;

@RunWith(EasyMockRunner.class)
public class IgcUtilsTest {

    @Mock
    AircraftDescriptor descriptor;

    @Mock
    AircraftBeacon beacon;

    @Test
    public void test1() {
        expect(descriptor.isKnown()).andReturn(true);
        expect(descriptor.getRegNumber()).andReturn("A-BCD");
        expect(descriptor.getCN()).andReturn("XY");
        expect(beacon.getId()).andReturn("123456");

        replay(beacon, descriptor);

        String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);
        verify(beacon, descriptor);

        assertEquals("123456_A-BCD_XY", igcId);
    }

    @Test
    public void test2() {
        expect(descriptor.isKnown()).andReturn(true);
        expect(descriptor.getRegNumber()).andReturn("A-BCD");
        expect(descriptor.getCN()).andReturn(null);
        expect(beacon.getId()).andReturn("123456");

        replay(beacon, descriptor);

        String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);
        verify(beacon, descriptor);

        assertEquals("123456_A-BCD", igcId);
    }

    @Test
    public void test3() {
        expect(descriptor.isKnown()).andReturn(true);
        expect(descriptor.getRegNumber()).andReturn(null);
        expect(descriptor.getCN()).andReturn("XY");
        expect(beacon.getId()).andReturn("123456");

        replay(beacon, descriptor);

        String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);
        verify(beacon, descriptor);

        assertEquals("123456_XY", igcId);
    }

    @Test
    public void test4() {
        expect(descriptor.isKnown()).andReturn(true);
        expect(descriptor.getRegNumber()).andReturn("");
        expect(descriptor.getCN()).andReturn("XY");
        expect(beacon.getId()).andReturn("123456");

        replay(beacon, descriptor);

        String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);
        verify(beacon, descriptor);

        assertEquals("123456_XY", igcId);
    }

    @Test
    public void test5() {
        expect(descriptor.isKnown()).andReturn(true);
        expect(descriptor.getRegNumber()).andReturn("A-BCD");
        expect(descriptor.getCN()).andReturn("");
        expect(beacon.getId()).andReturn("123456");

        replay(beacon, descriptor);

        String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);
        verify(beacon, descriptor);

        assertEquals("123456_A-BCD", igcId);
    }
}