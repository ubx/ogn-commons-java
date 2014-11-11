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
    String getName();

    String getVersion();

    <BeaconType extends OgnBeacon> void onBeacon(BeaconType beacon);
}