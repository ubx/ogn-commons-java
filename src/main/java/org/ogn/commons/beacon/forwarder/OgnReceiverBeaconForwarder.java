package org.ogn.commons.beacon.forwarder;

import org.ogn.commons.beacon.ReceiverBeacon;

/**
 * this interface is to be implemented by these ogn-gateway plugins who want to
 * receive receiver beacon updates
 * 
 * @author Wojtek
 * 
 */
public interface OgnReceiverBeaconForwarder extends OgnBeaconForwarder {

	/**
	 * @param beacon
	 *            ReceiverBeacon
	 */
	void onBeacon(ReceiverBeacon beacon);
}
