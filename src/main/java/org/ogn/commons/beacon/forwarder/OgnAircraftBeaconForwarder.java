/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.forwarder;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;

public interface OgnAircraftBeaconForwarder extends OgnBeaconForwarder {
    /**
     * plug-in should implement this interface to deliver beacon to destination system
     * 
     * @param beacon OGN beacon
     */
    void onBeacon(AircraftBeacon beacon, AircraftDescriptor descriptor);
}