/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.utils;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.String.format;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.ogn.commons.beacon.OgnBeacon;

public class AprsUtils {

	/**
	 * Generates APRS login sentence, required by APRS server. Refer to <a
	 * href="http://www.aprs-is.net/Connecting.aspx">Connecting to APRS-IS</a>
	 * for details.
	 * 
	 * @param userName
	 * @param passCode
	 * @param appName
	 * @param version
	 * @param filter
	 * @return
	 */
	public static String formatAprsLoginLine(final String userName, final String passCode, final String appName,
			final String version, final String filter) {
		return filter == null ? format("user %s pass %s vers %s %s", userName, passCode, appName, version) : format(
				"user %s pass %s vers %s %s filter %s", userName, passCode, appName, version, filter);
	}

	public static String formatAprsLoginLine(final String userName, final String passCode, final String appName,
			final String version) {
		return formatAprsLoginLine(userName, passCode, appName, version, null);
	}

	/**
	 * @return a unique client id(based on the host name + sequence id) which
	 *         can be used as APRS user name. The max length is 9 characters and
	 *         complies with APRS <a
	 *         href="http://www.aprs-is.net/Connecting.aspx#loginrules"> Login
	 *         rules </a>
	 */
	public static String generateClientId() {
		try {
			String suffix = UUID.randomUUID().toString().split("-")[3].toUpperCase();
			String res = InetAddress.getLocalHost().getHostName().substring(0, 3).toUpperCase();
			StringBuilder bld = new StringBuilder(res.replace("-", ""));
			bld.append("-");
			bld.append(suffix);

			return bld.toString();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public static double dmsToDeg(double dms) {
		double absDms = Math.abs(dms);
		double d = Math.floor(absDms);
		double m = (absDms - d) * 100 / 60;
		return (d + m);
	}

	public static double degToDms(double deg) {
		double absDeg = Math.abs(deg);
		double d = Math.floor(absDeg);
		double m = (absDeg - d) * 60 / 100;
		return (d + m);
	}

	public static enum Coordinate {
		LAT, LON;
	}

	public static String degToIgc(double deg, Coordinate what) {
		StringBuilder result = new StringBuilder();

		char sign = 'S';
		switch (what) {
		case LAT:
			if (deg < 0.0f)
				sign = 'S';
			else
				sign = 'N';

			result.append(String.format("%07.0f", Math.abs(degToDms(deg) * 100 * 100 * 10)));
			break;
		case LON:
			if (deg < 0.0f)
				sign = 'W';
			else
				sign = 'E';

			result.append(String.format("%08.0f", Math.abs(degToDms(deg) * 100 * 100 * 10)));
			break;
		}

		result.append(sign);
		return result.toString();
	}

	public static double degToMeters(double deg) {
		// Converts an angle (lon or lat) to meters.
		// We assume a spherical Earth and being at the sea level.
		double earthRadius = 6371 * 1000; // in meters
		return (deg * Math.PI * earthRadius / 180);
	}

	/**
	 * Creates a unix timestamp, based on given h:m:s. Local system's date is
	 * taken as a reference
	 * 
	 * @param h
	 *            hour
	 * @param m
	 *            minutes
	 * @param s
	 *            seconds
	 * @return
	 */
	public static long toUtcTimestamp(int h, int m, int s) {
		// Get today's date and time.
		Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c1.setTime(new Date());

		// TODO: take care of the rare situations when a packet arrives just
		// after midnight
		// yet it is delayed and its timestamp is set to 2359xx. We cannot take
		// the current day
		// number as a reference in such case, but we should move one day back.

		c1.set(Calendar.HOUR_OF_DAY, h);
		c1.set(Calendar.MINUTE, m);
		c1.set(Calendar.SECOND, s);
		// return the timestamp with sec. precision
		return (c1.getTimeInMillis() / 1000) * 1000;
	}

	/**
	 * @param time
	 *            time in 6 digit format provided in a APRS packet (e.g. 162334,
	 *            051202)
	 * @return
	 */
	public static long toUtcTimestamp(String time) {
		int h = Integer.parseInt(time.substring(0, 2));
		int m = Integer.parseInt(time.substring(2, 4));
		int s = Integer.parseInt(time.substring(4, 6));

		return toUtcTimestamp(h, m, s);
	}

	public static float feetsToMetres(float feets) {
		return (float) (Math.round((feets / (float) 3.2808) * 10) / 10.0);
	}
	
	public static int flToFeets(float fl) {				
		return Math.round(fl * 100);
	}

	public static float kntToKmh(float knts) {
		return knts * (float) 1.852; // kts to km/h
	}

	/**
	 * computes distance(in m) between two coordinates (in deg. format)
	 * 
	 * @param degLat1
	 * @param degLon1
	 * @param dgLat2
	 * @param degLon2
	 * @return a distance in m
	 */
	public static double calcDistance(double degLat1, double degLon1, double degLat2, double degLon2) {
		final int RADIUS = 6371000;

		double radLat1 = degLat1 * Math.PI / 180;
		double radLon1 = degLon1 * Math.PI / 180;

		double radLat2 = degLat2 * Math.PI / 180;
		double radLon2 = degLon2 * Math.PI / 180;

		return acos(sin(radLat1) * sin(radLat2) + cos(radLat1) * cos(radLat2) * cos(radLon2 - radLon1)) * RADIUS;
	}

	public static double calcDistance(OgnBeacon beacon1, OgnBeacon beacon2) {
		return calcDistance(beacon1.getLat(), beacon1.getLon(), beacon2.getLat(), beacon2.getLon());
	}

	public static double calcDistanceInKm(double degLat1, double degLon1, double degLat2, double degLon2) {
		return round(AprsUtils.calcDistance(degLat1, degLon1, degLat2, degLon2) / 1000 * 100.0) / 100.0;
	}

	public static double calcDistanceInKm(OgnBeacon beacon1, OgnBeacon beacon2) {
		return calcDistanceInKm(beacon1.getLat(), beacon1.getLon(), beacon2.getLat(), beacon2.getLon());
	}

}