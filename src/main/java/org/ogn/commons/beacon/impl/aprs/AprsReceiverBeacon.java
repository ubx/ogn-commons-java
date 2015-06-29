/**
 * Copyright (c) 2014-2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.ogn.commons.utils.AprsUtils.dmsToDeg;
import static org.ogn.commons.utils.AprsUtils.feetsToMetres;
import static org.ogn.commons.utils.AprsUtils.kntToKmh;
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

public class AprsReceiverBeacon extends OgnBeaconImpl implements
		ReceiverBeacon, Serializable {

	private static final long serialVersionUID = 2851952572220758613L;

	private static final Logger LOG = LoggerFactory
			.getLogger(AprsReceiverBeacon.class);

	/**
	 * name of the server receiving the packet
	 */
	protected String srvName;

	/**
	 * receiver's version
	 */
	protected String version;

	/**
	 * hardware platform on which the receiver runs
	 */
	protected String platform;

	/**
	 * CPU load (as indicated by the linux 'uptime' command)
	 */
	protected float cpuLoad;

	/**
	 * CPU temperature of the board (in deg C) or <code>Float.NaN</code> if not
	 * set
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

	// D-4465>APRS,qAS,EDMA:/132350h4825.31N/01055.79E'112/002/A=001512
	// id06DF03B3 -019fpm +0.0rot 39.0dB 0e -6.7kHz
	// gps1x2 hear0CC5 hearABA7

	// LFLE>APRS,TCPIP*,qAC,GLIDERN1:/203735h4533.44NI00558.59E&020/010/A=000977
	// v0.2.3.x86 CPU:0.4 RAM:80.9/517.6MB NTP:0.7ms/-25.6ppm RF:+3.80dB";

	private static final Pattern basicAprsPattern = Pattern
			.compile("(.+?)>APRS,.+,(.+?):/(\\d{6})+h(\\d{4}\\.\\d{2})(N|S).(\\d{5}\\.\\d{2})(E|W).((\\d{3})/(\\d{3}))?/A=(\\d{6}).*?");

	private static final Pattern versionPattern = Pattern
			.compile("v(\\d+\\.\\d+\\.\\d+)\\.?(.*?)");

	private static final Pattern cpuPattern = Pattern
			.compile("CPU:(\\d+\\.\\d+)");
	private static final Pattern cpuTempPattern = Pattern
			.compile("(\\+|\\-)(\\d+\\.\\d+)C");
	private static final Pattern ramPattern = Pattern
			.compile("RAM:(\\d+\\.\\d+)/(\\d+\\.\\d+)MB");
	private static final Pattern ntpPattern = Pattern
			.compile("NTP:(\\d+\\.\\d+)ms/(\\+|\\-)(\\d+\\.\\d+)ppm");

	private static final Pattern rfPatternFull = Pattern
			.compile("RF:(\\+|\\-)(\\d+)(\\+|\\-)(\\d+\\.\\d+)ppm/(\\+|\\-)(\\d+\\.\\d+)dB");

	private static final Pattern rfPatternLight1 = Pattern
			.compile("RF:(\\+|\\-)(\\d+\\.\\d+)dB");

	private static final Pattern rfPatternLight2 = Pattern
			.compile("RF:(\\+|\\-)(\\d+)(\\+|\\-)(\\d+\\.\\d+)ppm");

	// Delft>APRS,TCPIP*,qAC,GLIDERN1:/100152h5200.69NI00421.98E&/A=000033
	// v0.1.3 CPU:0.0 RAM:77.5/458.6MB
	// NTP:1.1ms/-52.0ppm +45.5C RF:+68+0.0ppm

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
	public String getPlatform() {
		return platform;
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
				if (matcher.group(8) != null) { // track+speed are optional
					track = Integer.parseInt(matcher.group(9));
					groundSpeed = kntToKmh(Float.parseFloat(matcher.group(10))); // kts
																					// to
																					// km/h
				}
				alt = feetsToMetres(Float.parseFloat(matcher.group(11)));

			} else if ((matcher = versionPattern.matcher(aprsParam)).matches()) {
				version = matcher.group(1);
				String tmp = platform = matcher.group(2);
			} else if ((matcher = cpuPattern.matcher(aprsParam)).matches()) {
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
			} else if ((matcher = rfPatternFull.matcher(aprsParam)).matches()) {
				recCrystalCorrection = Integer.parseInt(matcher.group(2));
				if (matcher.group(1).equals("-"))
					recCrystalCorrection *= -1;
				recCrystalCorrectionFine = Float.parseFloat(matcher.group(4));
				if (matcher.group(3).equals("-"))
					recCrystalCorrectionFine *= -1;
				recInputNoise = Float.parseFloat(matcher.group(6));
				if (matcher.group(5).equals("-"))
					recInputNoise *= -1;
			} else if ((matcher = rfPatternLight1.matcher(aprsParam)).matches()) {
				recInputNoise = Float.parseFloat(matcher.group(2));
				if (matcher.group(1).equals("-"))
					recInputNoise *= -1;
			} else if ((matcher = rfPatternLight2.matcher(aprsParam)).matches()) {
				recCrystalCorrection = Integer.parseInt(matcher.group(2));
				if (matcher.group(1).equals("-"))
					recCrystalCorrection *= -1;
				recCrystalCorrectionFine = Float.parseFloat(matcher.group(4));
				if (matcher.group(3).equals("-"))
					recCrystalCorrectionFine *= -1;
			} else {
				unmachedParams.add(aprsParam);
			}
		}

		if (!unmachedParams.isEmpty()) {
			LOG.warn("aprs-sentence:[{}] unmatched aprs parms: {}",
					aprsSentence, unmachedParams);
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
		result = prime * result
				+ Float.floatToIntBits(recCrystalCorrectionFine);
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
		if (Float.floatToIntBits(cpuLoad) != Float
				.floatToIntBits(other.cpuLoad))
			return false;
		if (Float.floatToIntBits(cpuTemp) != Float
				.floatToIntBits(other.cpuTemp))
			return false;
		if (Float.floatToIntBits(freeRam) != Float
				.floatToIntBits(other.freeRam))
			return false;
		if (Float.floatToIntBits(ntpError) != Float
				.floatToIntBits(other.ntpError))
			return false;
		if (recCrystalCorrection != other.recCrystalCorrection)
			return false;
		if (Float.floatToIntBits(recCrystalCorrectionFine) != Float
				.floatToIntBits(other.recCrystalCorrectionFine))
			return false;
		if (Float.floatToIntBits(recInputNoise) != Float
				.floatToIntBits(other.recInputNoise))
			return false;
		if (Float.floatToIntBits(rtCrystalCorrection) != Float
				.floatToIntBits(other.rtCrystalCorrection))
			return false;
		if (srvName == null) {
			if (other.srvName != null)
				return false;
		} else if (!srvName.equals(other.srvName))
			return false;
		if (Float.floatToIntBits(totalRam) != Float
				.floatToIntBits(other.totalRam))
			return false;
		return true;
	}
}