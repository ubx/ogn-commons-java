/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */
package org.ogn.commons.beacon;

public interface AircraftBeacon extends OgnBeacon {

	/**
	 * Name of the receiver which received this message
	 */
	String getReceiverName();

	/**
	 * id can be either ICAO, FLARM, OGN or RANDOM
	 */
	AddressType getAddressType();

	/**
	 * ICAO/FLARM/OGN tracker ID
	 */
	String getAddress();

	/**
	 * 
	 * Original (FLARM) address. If one sets ICAO address this one will still point to the original FLARM device id
	 */
	String getOriginalAddress();

	/**
	 * type of an aircraft (Glider, tow plane, helicopter, etc..)
	 */
	AircraftType getAircraftType();

	/**
	 * stealth mode active or not. This is for internal use only, because a standard client will NOT be receiving
	 * aircraft beacons with stealth flag on
	 */
	boolean isStealth();

	/**
	 * climb rate in m/s
	 */
	float getClimbRate();

	/**
	 * turn rate in deg/s
	 */
	float getTurnRate();

	/**
	 * reception signal strength measured in dB
	 */
	float getSignalStrength();

	/**
	 * frequency offset measured in KHz
	 */
	float getFrequencyOffset();

	/**
	 * GPS status (GPS accuracy in meters, horizontal and vertical)
	 */
	String getGpsStatus();

	/**
	 * number of errors corrected by the receiver
	 */
	int getErrorCount();

	/**
	 * ids of other aircraft received by this aircraft
	 */
	String[] getHeardAircraftIds();

	/**
	 * version of the transmitter's firmware
	 */
	float getFirmwareVersion();

	/**
	 * 
	 * @return 8-bit hardware version
	 */
	int getHardwareVersion();

	/**
	 * 
	 * @return estimated effective radiated power of the transmitter
	 */
	float getERP();

}