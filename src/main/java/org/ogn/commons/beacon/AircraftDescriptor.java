/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon;

/**
 * Static descriptive information about an aircraft
 * 
 * @author wbuczak
 */
public interface AircraftDescriptor {

    /**
     * @return true if an aircraft has been recognized by at least one descriptor provider, false otherwise
     */
    boolean isKnown();

    /**
     * @return aircraft's registration number
     */
    String getRegNumber();

    /**
     * @return competition number or null if unavailable
     */
    String getCN();

    /**
     * @return Owner's name
     */
    String getOwner();

    /**
     * @return name of the aircraft's home base
     */
    String getHomeBase();

    /**
     * @return model of the aircraft (e.g. Grob Astir, Std. Cirrus)
     */
    String getModel();

    /**
     * @return default frequency or null if unavailable
     */
    String getFreq();

}