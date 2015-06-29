/**
 * Copyright (c) 2014-2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import static org.ogn.commons.utils.AprsUtils.dmsToDeg;
import static org.ogn.commons.utils.AprsUtils.feetsToMetres;
import static org.ogn.commons.utils.AprsUtils.kntToKmh;
import static org.ogn.commons.utils.AprsUtils.toUtcTimestamp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ogn.commons.beacon.AddressType;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftType;
import org.ogn.commons.beacon.impl.OgnBeaconImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AprsAircraftBeacon extends OgnBeaconImpl implements AircraftBeacon {

	private static final long serialVersionUID = -7640993719847348787L;

	private static final Logger LOG = LoggerFactory
			.getLogger(AprsAircraftBeacon.class);

	/**
	 * Name of the receiver which received this message
	 */
	protected String receiverName;

	/**
	 * ICAO/FLARM/OGN tracker ID
	 */
	protected String address;

	/**
	 * id can be either ICAO, FLARM or OGN
	 */
	protected AddressType addressType = AddressType.UNRECOGNIZED;

	/**
	 * type of an aircraft (Glider, tow plane, helicopter, etc..)
	 */
	protected AircraftType aircraftType;

	/**
	 * stealth mode active or not
	 */
	protected boolean stealth;

	/**
	 * climb rate in m/s
	 */
	protected float climbRate;

	/**
	 * turn rate in deg/s
	 */
	protected float turnRate;

	/**
	 * reception signal strength measured in dB
	 */
	protected float signalStrength;

	/**
	 * frequency offset measured in KHz
	 */
	protected float frequencyOffset; // in KHz

	/**
	 * GPS status (number of sat received ?)
	 */
	protected String gpsStatus;

	/**
	 * number of errors corrected by the receiver
	 */
	protected int errorCount;

	/**
	 * id of another aircraft received by this aircraft
	 */
	protected Set<String> heardAircraftIds = new HashSet<>();

	// F-GEKY>APRS,qAS,CHALLES:/145914h4533.12N/00559.93E'140/045/A=003316|H&,+Ll#U"RLz|
	// F-CLUI>APRS,qAS,TELECOM:/162648h4531.79N/00558.92E'036/056/A=003037
	// id06DDA310 +495fpm -0.4rot 10.7dB 0e hear9B73
	// D-4465>APRS,qAS,EDMA:/132350h4825.31N/01055.79E'112/002/A=001512
	// id06DF03B3 -019fpm +0.0rot 39.0dB 0e -6.7kHz gps1x2 hear0CC5 hearABA7

	private static final Pattern basicAprsPattern = Pattern
			.compile("(.+?)>APRS,.+,(.+?):/(\\d{6})+h(\\d{4}\\.\\d{2})(N|S).(\\d{5}\\.\\d{2})(E|W).((\\d{3})/(\\d{3}))?/A=(\\d{6}).*?");

	private static final Pattern addressPattern = Pattern.compile("id(\\S{8})");
	private static final Pattern climbRatePattern = Pattern
			.compile("(\\+|\\-)(\\d+)fpm");
	private static final Pattern turnRatePattern = Pattern
			.compile("(\\+|\\-)(\\d+\\.\\d+)rot");
	private static final Pattern signalStrengthPattern = Pattern
			.compile("(\\d+\\.\\d+)dB");
	private static final Pattern errorCountPattern = Pattern.compile("(\\d+)e");

	// TODO: manage multiple heard ids
	private static final Pattern hearIDPattern = Pattern
			.compile("hear(\\w{4})");
	private static final Pattern frequencyOffsetPattern = Pattern
			.compile("(\\+|\\-)(\\d+\\.\\d+)kHz");
	private static final Pattern gpsStatusPattern = Pattern
			.compile("gps(\\d+x\\d+)");

	@Override
	public String getReceiverName() {
		return receiverName;
	}

	@Override
	public int getTrack() {
		return track;
	}

	@Override
	public float getGroundSpeed() {
		return groundSpeed;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public AddressType getAddressType() {
		return addressType;
	}

	@Override
	public AircraftType getAircraftType() {
		return aircraftType;
	}

	@Override
	public boolean isStealth() {
		return stealth;
	}

	@Override
	public float getClimbRate() {
		return climbRate;
	}

	@Override
	public float getTurnRate() {
		return turnRate;
	}

	@Override
	public float getSignalStrength() {
		return signalStrength;
	}

	@Override
	public float getFrequencyOffset() {
		return frequencyOffset;
	}

	@Override
	public String getGpsStatus() {
		return gpsStatus;
	}

	@Override
	public int getErrorCount() {
		return errorCount;
	}

	@Override
	public String[] getHeardAircraftIds() {
		return heardAircraftIds.toArray(new String[0]);
	}

	// private default constructor
	// required by jackson (as it uses reflection)
	@SuppressWarnings("unused")
	private AprsAircraftBeacon() {
		// no default implementation
	}

	public AprsAircraftBeacon(final String aprsSentence) {

		Matcher matcher;

		List<String> unmachedParams = new ArrayList<>();

		// remember raw packet string
		rawPacket = aprsSentence;

		String[] aprsParams = aprsSentence.split("\\s+");
		for (String aprsParam : aprsParams) {
			if ((matcher = basicAprsPattern.matcher(aprsParam)).matches()) {
				id = matcher.group(1);
				receiverName = matcher.group(2);
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

			} else if ((matcher = addressPattern.matcher(aprsParam)).matches()) {
				address = matcher.group(1).substring(2, 8);
				// Flarm ID type byte in APRS msg: PTTT TTII
				// P => stealth mode
				// TTTTT => aircraftType
				// II => IdType: 0=Random, 1=ICAO, 2=FLARM, 3=OGN
				// (see
				// https://groups.google.com/forum/#!msg/openglidernetwork/lMzl5ZsaCVs/YirmlnkaJOYJ).
				//
				addressType = AddressType.forValue(Integer.parseInt(matcher
						.group(1).substring(0, 2), 16) & 3); // 2
				aircraftType = AircraftType.forValue((Integer.parseInt(matcher
						.group(1).substring(0, 2), 16) & 0b1111100) >>> 2);
				stealth = (Integer.parseInt(matcher.group(1).substring(0, 2),
						16) & 0b10000000) != 0;
			} else if ((matcher = climbRatePattern.matcher(aprsParam))
					.matches()) {
				climbRate = feetsToMetres(Float.parseFloat(matcher.group(2))) / 60; // feets/m
																					// to
																					// m/s
				if (matcher.group(1).equals("-"))
					climbRate *= -1;
				climbRate = (float) (Math.round(climbRate * 100) / 100.0);
			} else if ((matcher = turnRatePattern.matcher(aprsParam)).matches()) {
				turnRate = Float.parseFloat(matcher.group(2));
				if (matcher.group(1).equals("-"))
					turnRate *= -1;
			} else if ((matcher = signalStrengthPattern.matcher(aprsParam))
					.matches()) {
				signalStrength = Float.parseFloat(matcher.group(1));
			} else if ((matcher = errorCountPattern.matcher(aprsParam))
					.matches()) {
				errorCount = Integer.parseInt(matcher.group(1));
			} else if ((matcher = hearIDPattern.matcher(aprsParam)).matches()) {
				heardAircraftIds.add(matcher.group(1));
			} else if ((matcher = frequencyOffsetPattern.matcher(aprsParam))
					.matches()) {
				frequencyOffset = Float.parseFloat(matcher.group(2));
				if (matcher.group(1).equals("-"))
					frequencyOffset *= -1;
			} else if ((matcher = gpsStatusPattern.matcher(aprsParam))
					.matches()) {
				gpsStatus = matcher.group(1);
			} else {
				unmachedParams.add(aprsParam);
			}
		}

		if (!unmachedParams.isEmpty()) {
			System.err.println(String.format(
					"aprs-sentence:[%s] unmatched aprs parms: %s",
					aprsSentence, unmachedParams));
			LOG.warn("aprs-sentence:[{}] unmatched aprs parms: {}",
					aprsSentence, unmachedParams);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result
				+ ((addressType == null) ? 0 : addressType.hashCode());
		result = prime * result
				+ ((aircraftType == null) ? 0 : aircraftType.hashCode());
		result = prime * result + Float.floatToIntBits(climbRate);
		result = prime * result + errorCount;
		result = prime * result + Float.floatToIntBits(frequencyOffset);
		result = prime * result
				+ ((gpsStatus == null) ? 0 : gpsStatus.hashCode());
		result = prime
				* result
				+ ((heardAircraftIds == null) ? 0 : heardAircraftIds.hashCode());
		result = prime * result
				+ ((receiverName == null) ? 0 : receiverName.hashCode());
		result = prime * result + Float.floatToIntBits(signalStrength);
		result = prime * result + (stealth ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(turnRate);
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
		AprsAircraftBeacon other = (AprsAircraftBeacon) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (addressType != other.addressType)
			return false;
		if (aircraftType != other.aircraftType)
			return false;
		if (Float.floatToIntBits(climbRate) != Float
				.floatToIntBits(other.climbRate))
			return false;
		if (errorCount != other.errorCount)
			return false;
		if (Float.floatToIntBits(frequencyOffset) != Float
				.floatToIntBits(other.frequencyOffset))
			return false;
		if (gpsStatus == null) {
			if (other.gpsStatus != null)
				return false;
		} else if (!gpsStatus.equals(other.gpsStatus))
			return false;
		if (heardAircraftIds == null) {
			if (other.heardAircraftIds != null)
				return false;
		} else if (!heardAircraftIds.equals(other.heardAircraftIds))
			return false;
		if (receiverName == null) {
			if (other.receiverName != null)
				return false;
		} else if (!receiverName.equals(other.receiverName))
			return false;
		if (Float.floatToIntBits(signalStrength) != Float
				.floatToIntBits(other.signalStrength))
			return false;
		if (stealth != other.stealth)
			return false;
		if (Float.floatToIntBits(turnRate) != Float
				.floatToIntBits(other.turnRate))
			return false;
		return true;
	}

}