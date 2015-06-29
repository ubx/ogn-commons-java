/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl;

import java.io.Serializable;

import org.ogn.commons.beacon.OgnBeacon;

/**
 * Generic class representing GPS position fix
 * 
 * @author wbuczak
 */
public class OgnBeaconImpl implements OgnBeacon, Serializable {

	private static final long serialVersionUID = 7387914213815737388L;

	protected String id;
	protected long timestamp;
	protected double lat;
	protected double lon;
	protected float alt;

	/**
	 * deg
	 */
	protected int track;

	/**
	 * km/h
	 */
	protected float groundSpeed;

	protected String rawPacket;

	protected OgnBeaconImpl() {
	}

	public OgnBeaconImpl(String rawPacket, String id, long timestamp,
			double latitude, double longitude, float altitude, int track,
			float groundSpeed) {
		this.rawPacket = rawPacket;
		this.id = id;
		this.timestamp = timestamp;
		this.lat = latitude;
		this.lon = longitude;
		this.alt = altitude;
		this.track = track;
		this.groundSpeed = groundSpeed;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public double getLat() {
		return lat;
	}

	@Override
	public double getLon() {
		return lon;
	}

	@Override
	public float getAlt() {
		return alt;
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
	public String getRawPacket() {
		return rawPacket;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(alt);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((rawPacket == null) ? 0 : rawPacket.hashCode());
		result = prime * result + Float.floatToIntBits(groundSpeed);
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + track;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OgnBeaconImpl other = (OgnBeaconImpl) obj;
		if (Float.floatToIntBits(alt) != Float.floatToIntBits(other.alt))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		if (rawPacket == null) {
			if (other.rawPacket != null)
				return false;
		} else if (!rawPacket.equals(other.rawPacket))
			return false;
		if (Float.floatToIntBits(groundSpeed) != Float
				.floatToIntBits(other.groundSpeed))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (track != other.track)
			return false;
		return true;
	}

}