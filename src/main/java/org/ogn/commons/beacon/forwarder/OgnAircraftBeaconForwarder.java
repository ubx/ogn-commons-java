/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.forwarder;

import java.util.Optional;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;

/**
 * this interface is to be implemented by these ogn gateway plugins who want to
 * receive aircraft beacon updates
 * 
 * @author wbuczak
 */
public interface OgnAircraftBeaconForwarder extends OgnBeaconForwarder {

	/**
	 * @param beacon
	 *            and AircraftBeacon
	 * @param descriptor
	 *            static aircraft description
	 */
	void onBeacon(AircraftBeacon beacon, Optional<AircraftDescriptor> descriptor);
}