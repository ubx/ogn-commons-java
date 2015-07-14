/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon;

import java.util.HashMap;
import java.util.Map;

/**
 * Types of aircraft as provided in FLARM dataport manual (page 9)
 * 
 * @see <a
 *      href="http://www.flarm.com/support/manual/FLARM_DataportManual_v6.00E.pdf">FLARM
 *      data port specification</a>
 * @author wbuczak
 */
public enum AircraftType {

	UNKNOWN(0), GLIDER(1), TOW_PLANE(2), HELICOPTER_ROTORCRAFT(3), PARACHUTE(4), DROP_PLANE(5), HANG_GLIDER(6), PARA_GLIDER(
			7), POWERED_AIRCRAFT(8), JET_AIRCRAFT(9), UFO(10), BALLOON(11), AIRSHIP(12), UAV(13), STATIC_OBJECT(15);

	private int value;

	private AircraftType(int value) {
		this.value = value;
	}

	public int getCode() {
		return this.value;
	}

	private static final Map<Integer, AircraftType> typesByValue = new HashMap<>();

	static {
		for (AircraftType type : AircraftType.values()) {
			typesByValue.put(type.value, type);
		}
	}

	public static AircraftType forValue(int value) {
		return typesByValue.containsKey(value) ? typesByValue.get(value) : UNKNOWN;
	}
}