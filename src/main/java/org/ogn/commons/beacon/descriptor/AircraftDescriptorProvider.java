/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.descriptor;

import org.ogn.commons.beacon.AircraftDescriptor;

/**
 * The descriptor provider service provides aircraft static descriptors based on the information provided in the beacon
 * (e.g. aircraft's id, address type: FLARM/ICAO/OGN)
 * 
 * @author wbuczak
 */
public interface AircraftDescriptorProvider {

    /**
     * @param address hardware address (FLARM.ICAO,OGN..)
     * @return static aircraft's descriptor or null if unavailable
     */
    AircraftDescriptor getDescritor(String address);
}