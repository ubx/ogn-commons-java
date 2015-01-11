/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.ogn.commons.utils.AprsUtils.dmsToDeg;
import static org.ogn.commons.utils.AprsUtils.feetsToMetres;
import static org.ogn.commons.utils.AprsUtils.toUtcTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ogn.commons.beacon.ReceiverBeacon;
import org.ogn.commons.beacon.impl.OgnBeaconImpl;
import org.ogn.commons.utils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AprsReceiverBeacon extends OgnBeaconImpl implements ReceiverBeacon, Serializable {

    private static final long serialVersionUID = 2851952572220758613L;

    private static final Logger LOG = LoggerFactory.getLogger(AprsReceiverBeacon.class);

    /**
     * name of the server receiving the packet
     */
    protected String srvName;

    /**
     * receiver's version
     */
    protected String version;

    /**
     * CPU load (as indicated by the linux 'uptime' command)
     */
    protected float cpuLoad;

    /**
     * CPU temperature of the board (in deg C) or <code>Float.NaN</code> if not set
     */
    protected float cpuTemp = Float.NaN;

    /**
     * total size of RAM available in the system (in MB)
     */
    protected float totalRam;

    /**
     * size of free RAM (in MB)
     */
    protected float freeRam;

    /**
     * estimated NTP error (in ms)
     */
    protected float ntpError;

    /**
     * real time crystal correction(set in the configuration) (in ppm)
     */
    protected float rtCrystalCorrection;

    /**
     * receiver (DVB-T stick's) crystal correction (in ppm)
     */
    protected int recCrystalCorrection;

    /**
     * receiver correction measured taking GSM for a reference (in ppm)
     */
    protected float recCrystalCorrectionFine;

    /**
     * receiver's input noise (in dB)
     */
    protected float recInputNoise;

    // D-4465>APRS,qAS,EDMA:/132350h4825.31N/01055.79E'112/002/A=001512 id06DF03B3 -019fpm +0.0rot 39.0dB 0e -6.7kHz
    // gps1x2 hear0CC5 hearABA7

    // EBZW>APRS,TCPIP*,qAC,GLIDERN1:/102546h5100.86NI00531.43E&/A=000298 CPU:0.9 RAM:968.2/1056.5MB NTP:1.5ms/-20.0ppm
    // RF:+127-2.9ppm/+4.3dB

    private static final Pattern basicAprsPattern = Pattern
            .compile("(.+)>.+,(.+?):/(\\d{6})+h(\\d{4}\\.\\d{2})(N|S).(\\d{5}\\.\\d{2})(E|W)./A=(\\d{6}).*");

    private static final Pattern versionPattern = Pattern.compile("v(\\d+\\.\\d+\\.\\d+)");
    private static final Pattern cpuPattern = Pattern.compile("CPU:(\\d+\\.\\d+)");
    private static final Pattern cpuTempPattern = Pattern.compile("(\\+|\\-)(\\d+\\.\\d+)C");
    private static final Pattern ramPattern = Pattern.compile("RAM:(\\d+\\.\\d+)/(\\d+\\.\\d+)MB");
    private static final Pattern ntpPattern = Pattern.compile("NTP:(\\d+\\.\\d+)ms/(\\+|\\-)(\\d+\\.\\d+)ppm");
    private static final Pattern rfPattern = Pattern
            .compile("RF:(\\+|\\-)(\\d+)(\\+|\\-)(\\d+\\.\\d+)ppm/(\\+|\\-)(\\d+\\.\\d+)dB");

    @Override
    public float getCpuLoad() {
        return cpuLoad;
    }

    @Override
    public float getCpuTemp() {
        return cpuTemp;
    }

    @Override
    public float getFreeRam() {
        return freeRam;
    }

    @Override
    public float getTotalRam() {
        return totalRam;
    }

    @Override
    public float getNtpError() {
        return ntpError;
    }

    @Override
    public float getRtCrystalCorrection() {
        return rtCrystalCorrection;
    }

    @Override
    public int getRecCrystalCorrection() {
        return recCrystalCorrection;
    }

    @Override
    public float getRecCrystalCorrectionFine() {
        return recCrystalCorrectionFine;
    }

    @Override
    public float getRecAbsCorrection() {
        return recCrystalCorrection + recCrystalCorrectionFine;
    }

    @Override
    public float getRecInputNoise() {
        return recInputNoise;
    }

    @Override
    public String getServerName() {
        return srvName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int getNumericVersion() {
        return version == null ? 0 : Version.fromString(version);
    }

    // private default constructor
    // required by jackson (as it uses reflection)
    @SuppressWarnings("unused")
    private AprsReceiverBeacon() {
        // no default implementation
    }

    public AprsReceiverBeacon(final String aprsSentence) {

        List<String> unmachedParams = new ArrayList<>();
        Matcher matcher = null;

        // remember raw packet string
        rawPacket = aprsSentence;

        String[] aprsParams = aprsSentence.split("\\s+");
        for (String aprsParam : aprsParams) {

            if ((matcher = basicAprsPattern.matcher(aprsParam)).matches()) {
                id = matcher.group(1);
                srvName = matcher.group(2);
                timestamp = toUtcTimestamp(matcher.group(3));

                lat = dmsToDeg(Double.parseDouble(matcher.group(4)) / 100);
                if (matcher.group(5).equals("S"))
                    lat *= -1;
                lon = dmsToDeg(Double.parseDouble(matcher.group(6)) / 100);
                if (matcher.group(7).equals("W"))
                    lon *= -1;

                alt = feetsToMetres(Float.parseFloat(matcher.group(8)));
            }
            else if ((matcher = versionPattern.matcher(aprsParam)).matches()) {
                version = matcher.group(1);
            }else if ((matcher = cpuPattern.matcher(aprsParam)).matches()) {
                cpuLoad = Float.parseFloat(matcher.group(1));
            } else if ((matcher = cpuTempPattern.matcher(aprsParam)).matches()) {
                cpuTemp = Float.parseFloat(matcher.group(2));
                if (matcher.group(1).equals("-"))
                    cpuTemp *= -1;
            } else if ((matcher = ramPattern.matcher(aprsParam)).matches()) {
                freeRam = Float.parseFloat(matcher.group(1));
                totalRam = Float.parseFloat(matcher.group(2));
            } else if ((matcher = ntpPattern.matcher(aprsParam)).matches()) {
                ntpError = Float.parseFloat(matcher.group(1));
                rtCrystalCorrection = Float.parseFloat(matcher.group(3));
                if (matcher.group(2).equals("-"))
                    rtCrystalCorrection *= -1;
            } else if ((matcher = rfPattern.matcher(aprsParam)).matches()) {
                recCrystalCorrection = Integer.parseInt(matcher.group(2));
                if (matcher.group(1).equals("-"))
                    recCrystalCorrection *= -1;
                recCrystalCorrectionFine = Float.parseFloat(matcher.group(4));
                if (matcher.group(3).equals("-"))
                    recCrystalCorrectionFine *= -1;
                recInputNoise = Float.parseFloat(matcher.group(6));
                if (matcher.group(5).equals("-"))
                    recInputNoise *= -1;
            } else {
                unmachedParams.add(aprsParam);
            }
        }

        if (!unmachedParams.isEmpty()) {                        
            LOG.warn("aprs-sentence:[{}] unmatched aprs parms: {}", aprsSentence, unmachedParams);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(cpuLoad);
        result = prime * result + Float.floatToIntBits(cpuTemp);
        result = prime * result + Float.floatToIntBits(freeRam);
        result = prime * result + Float.floatToIntBits(ntpError);
        result = prime * result + recCrystalCorrection;
        result = prime * result + Float.floatToIntBits(recCrystalCorrectionFine);
        result = prime * result + Float.floatToIntBits(recInputNoise);
        result = prime * result + Float.floatToIntBits(rtCrystalCorrection);
        result = prime * result + ((srvName == null) ? 0 : srvName.hashCode());
        result = prime * result + Float.floatToIntBits(totalRam);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AprsReceiverBeacon other = (AprsReceiverBeacon) obj;
        if (Float.floatToIntBits(cpuLoad) != Float.floatToIntBits(other.cpuLoad))
            return false;
        if (Float.floatToIntBits(cpuTemp) != Float.floatToIntBits(other.cpuTemp))
            return false;
        if (Float.floatToIntBits(freeRam) != Float.floatToIntBits(other.freeRam))
            return false;
        if (Float.floatToIntBits(ntpError) != Float.floatToIntBits(other.ntpError))
            return false;
        if (recCrystalCorrection != other.recCrystalCorrection)
            return false;
        if (Float.floatToIntBits(recCrystalCorrectionFine) != Float.floatToIntBits(other.recCrystalCorrectionFine))
            return false;
        if (Float.floatToIntBits(recInputNoise) != Float.floatToIntBits(other.recInputNoise))
            return false;
        if (Float.floatToIntBits(rtCrystalCorrection) != Float.floatToIntBits(other.rtCrystalCorrection))
            return false;
        if (srvName == null) {
            if (other.srvName != null)
                return false;
        } else if (!srvName.equals(other.srvName))
            return false;
        if (Float.floatToIntBits(totalRam) != Float.floatToIntBits(other.totalRam))
            return false;
        return true;
    }

}