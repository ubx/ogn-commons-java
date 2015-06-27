/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl.aprs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ogn.commons.beacon.OgnBeacon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AprsLineParser {

	public static final String APRS_SENTENCE_PATTERN = "(.+)>.+:/\\d+h(\\d{4}\\.\\d{2})(N|S)(.)(\\d{5}\\.\\d{2})(E|W)(.)((\\d{3})/(\\d{3}))?/A=(\\d{6}).*";

	// OGN APRS servers reply to the client or send periodic heart-bit where
	// first character is #
	// e.g:
	// # aprsc 2.0.14-g28c5a6a
	// # logresp PCBE13-1 unverified, server GLIDERN2
	private static final String APRS_SRV_MSG_FIRST_CHARACTER = "#";

	private static Pattern p = Pattern.compile(APRS_SENTENCE_PATTERN);

	private static Pattern rfPattern = Pattern
			.compile("(.)+ RF:(\\+|\\-)(\\d+)(\\+|\\-)(\\d+\\.\\d+)ppm/(\\+|\\-)(\\d+\\.\\d+)dB.*");

	private static Pattern cpuPattern = Pattern
			.compile("(.)+ CPU:(\\d+\\.\\d+).*");

	private static Logger LOG = LoggerFactory.getLogger(AprsLineParser.class);

	private static class AprsLineParserHolder {
		private static AprsLineParser theInstance = new AprsLineParser();
	}

	public static AprsLineParser get() {
		return AprsLineParserHolder.theInstance;
	}

	public OgnBeacon parse(String aprsLine) {
		return parse(aprsLine, true, true);
	}

	public OgnBeacon parse(String aprsLine, boolean processAircraftBeacons,
			boolean processReceiverBeacons) {
		LOG.trace(aprsLine);
		OgnBeacon result = null;

		Matcher m1 = p.matcher(aprsLine); // Try to match

		if (m1.matches()) {
			if (!aprsLine.startsWith(APRS_SRV_MSG_FIRST_CHARACTER)) {

				// receiver beacons are supposed to have RF and/or CPU
				// information
				if (rfPattern.matcher(aprsLine).matches()
						|| cpuPattern.matcher(aprsLine).matches()) {

					if (processReceiverBeacons) {
						// match receiver beacons
						LOG.debug("Receiver beacon: {}", aprsLine);
						result = new AprsReceiverBeacon(aprsLine);
					}

				} else {

					if (processAircraftBeacons) {
						// match aircraft beacons
						LOG.debug("Aircraft beacon: {}", aprsLine);
						result = new AprsAircraftBeacon(aprsLine);
					}

				}
			}

		}

		return result;
	}
}
