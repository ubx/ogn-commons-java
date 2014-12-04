/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon;

public interface ReceiverBeacon extends OgnBeacon {

    /**
     * @return CPU load (as indicated by the linux 'uptime' command) or <code>Float.NaN</code> if not set
     */
    float getCpuLoad();

    /**
     * @return CPU temperature of the board (in deg C) or <code>Float.NaN</code> if not set
     */
    float getCpuTemp();

    /**
     * @return size of free RAM (in MB)
     */
    float getFreeRam();

    /**
     * @return total amount of RAM available in the system (in MB)
     */
    float getTotalRam();

    /**
     * @return estimated NTP error (in ms)
     */
    float getNtpError();

    /**
     * @return real time crystal correction(set in the configuration) (in ppm)
     */
    float getRtCrystalCorrection();

    /**
     * @return receiver (DVB-T stick's) crystal correction (in ppm)
     */
    int getRecCrystalCorrection();

    /**
     * @return receiver correction measured taking GSM for a reference (in ppm)
     */
    float getRecCrystalCorrectionFine();

    /**
     * @return receiver's absolute correction (as a result of: 'rec. crystal correction' + 'rec. correction fine') (in
     *         ppm)
     */
    float getRecAbsCorrection();

    /**
     * @return receiver's input noise (in dB)
     */
    float getRecInputNoise();

    /**
     * @return name of the server receiving the packet
     */
    String getServerName();

    /**
     * @return receiver version (if available) or null
     */
    String getVersion();

    /**
     * @return numeric representation of the receiver's version or 0 (if version is not available)
     */
    int getNumericVersion();

}
