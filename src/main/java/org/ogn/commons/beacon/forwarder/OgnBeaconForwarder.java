/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.forwarder;

import org.ogn.commons.beacon.OgnBeacon;

/**
 * This interface is to be implemented by all OGN gateway packet forwarder plugins. Packet Forwarders are services
 * sending data to different systems (e.g. FR24,...)
 * 
 * @author wbuczak
 */
public interface OgnBeaconForwarder {

    /**
     * @return short plugin's name
     */
    String getName();

    /**
     * @return version in format X.Y.Z (e.g. 1.0.0)
     */
    String getVersion();

    /**
     * @return plgin's description (where does it send data to, etc..)
     */
    String getDescription();

    /**
     * plugin should implement this interface to deliver beacon to destination system
     * 
     * @param beacon OGN beacon
     */
    <BeaconType extends OgnBeacon> void onBeacon(BeaconType beacon);
}