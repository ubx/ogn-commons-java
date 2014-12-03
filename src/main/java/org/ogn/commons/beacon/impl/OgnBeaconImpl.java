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
    protected String rawPacket;

    protected OgnBeaconImpl() {
    }

    public OgnBeaconImpl(String rawPacket, String id, long timestamp, double latitude, double longitude, float altitude) {
        this.rawPacket = rawPacket;
        this.id = id;
        this.timestamp = timestamp;
        this.lat = latitude;
        this.lon = longitude;
        this.alt = altitude;
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
    public String getRawPacket() {
        return rawPacket;
    }

    @Override
    public int hashCode() {
        // note: do not take timestamp for hashcode/equals as timestamp!
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(alt);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // note: do not take timestamp for hashcode/equals as timestamp!
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
        return true;
    }
}