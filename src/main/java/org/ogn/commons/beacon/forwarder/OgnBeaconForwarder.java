/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.forwarder;

/**
 * This interface is to be implemented by all OGN gateway packet forwarder plugins. Packet Forwarders are services
 * sending data to different systems (e.g. FR24,...)
 * 
 * @author wbuczak
 */
public interface OgnBeaconForwarder {

    /**
     * a plugin may require initialization (this interface is called by the ogn gateway)
     */
    void init();
        
    /**
     * before removing a plugin will be called to stop/release its resources (e.g. thread pools, file handles, sockets..)
     */
    void stop();

    /**
     * @return short plugin's name
     */
    String getName();

    /**
     * @return version in format X.Y.Z (e.g. 1.0.0)
     */
    String getVersion();

    /**
     * @return plugin's description (where does it send data to, etc..)
     */
    String getDescription();

}