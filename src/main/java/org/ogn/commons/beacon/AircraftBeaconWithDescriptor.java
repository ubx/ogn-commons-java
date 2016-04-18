package org.ogn.commons.beacon;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A utility wrapper class for encapsulating aircraft beacon together with the aircraft descriptor
 * 
 * @author Wojtek
 *
 */
public class AircraftBeaconWithDescriptor {

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	AircraftBeacon beacon;

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	AircraftDescriptor descriptor;

	// default empty constructor required to satisfy jackson
	@SuppressWarnings("unused")
	private AircraftBeaconWithDescriptor() {

	}

	public AircraftBeaconWithDescriptor(AircraftBeacon beacon, AircraftDescriptor descriptor) {
		super();
		this.beacon = beacon;
		this.descriptor = descriptor;
	}

	public AircraftBeacon getBeacon() {
		return beacon;
	}

	public AircraftDescriptor getDescriptor() {
		return descriptor;
	}
}