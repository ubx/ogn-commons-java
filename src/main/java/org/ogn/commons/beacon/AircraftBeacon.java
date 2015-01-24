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
     * deg
     */
    int getTrack();

    /**
     * km/h
     */
    float getGroundSpeed();

    /**
     * id can be either ICAO, FLARM, OGN or RANDOM
     */
    AddressType getAddressType();

    /**
     * ICAO/FLARM/OGN tracker ID
     */
    String getAddress();

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
     * GPS status (number of sat received ?)
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
}