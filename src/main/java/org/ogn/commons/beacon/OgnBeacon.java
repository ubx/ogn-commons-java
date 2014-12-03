/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon;

/**
 * basic, common interface of all OGN beacons
 */
public interface OgnBeacon {

    /**
     * @return packet identifier (in case of receivers - name of the receiver, in case of aircraft beacons - it can be
     *         reg. number (resolved by the receiver) or tracker address
     */
    String getId();

    /**
     * 
     * @return UTC timestamp (unix format, ms since 1970)
     */
    long getTimestamp();
    
    /**
     * @return beacon's latitude
     */
    double getLat();

    /**
     * @return beacon's longitude
     */
    double getLon();

    /**
     * @return beacon's altitude (m)
     */
    float getAlt();
    
    /**
     * 
     * @return raw (un-decoded) string representation of a packet
     */
    String getRawPacket();
}