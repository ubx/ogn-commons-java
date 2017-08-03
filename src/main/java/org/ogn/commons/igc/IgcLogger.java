/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.igc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.AprsUtils;
import org.ogn.commons.utils.AprsUtils.Coordinate;
import org.ogn.commons.utils.IgcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IGC logger creates and writes to IGC files. The logger's log() operation is non-blocking (logs are written to a
 * file by a background thread)
 * 
 * @author wbuczak
 */
public class IgcLogger {

	public static enum Mode {
		ASYNC, SYNC
	}

	private static final Logger LOG = LoggerFactory.getLogger(IgcLogger.class);

	private static final String DEFAULT_IGC_BASE_DIR = "log";

	private String igcBaseDir;

	private static final String LINE_SEP = System.lineSeparator();

	private Mode workingMode;

	private BlockingQueue<LogRecord> logRecords;
	private volatile Future<?> pollerFuture;
	private ExecutorService executor;

	private Map<String, String> id2reg = null;

	private static class LogRecord {
		AircraftBeacon beacon;
		Optional<AircraftDescriptor> descriptor;
		Optional<LocalDate> date;

		public LogRecord(AircraftBeacon beacon, Optional<LocalDate> date, Optional<AircraftDescriptor> descriptor) {
			this.beacon = beacon;
			this.descriptor = descriptor;
			this.date = date;

		}
	}

	public int getQueueSize() {
		return this.logRecords.size();
	}

	private class PollerTask implements Runnable {
		private Logger PLOG = LoggerFactory.getLogger(PollerTask.class);

		@Override
		public void run() {
			PLOG.trace("starting...");
			LogRecord record = null;
			while (!Thread.interrupted()) {
				try {
					record = logRecords.take();
					logToIgcFile(record.beacon, record.date, record.descriptor);
				} catch (InterruptedException e) {
					PLOG.trace("interrupted exception caught. Was the poller task interrupted on purpose?");
					Thread.currentThread().interrupt();
					continue;
				} catch (Exception e) {
					PLOG.error("exception caught", e);
					continue;
				}
			} // while
			PLOG.trace("exiting..");
		}
	}

	public IgcLogger(Mode mode) {
		this(DEFAULT_IGC_BASE_DIR, mode);
	}

	public IgcLogger(final String logsFolder, Mode mode) {
		LOG.info("creating igc logger [log-folder: {}, mode: {}]", logsFolder, mode);
		igcBaseDir = logsFolder;
		workingMode = mode;

		if (workingMode == Mode.ASYNC) {
			logRecords = new LinkedBlockingQueue<>();
			executor = Executors.newSingleThreadExecutor();
			pollerFuture = executor.submit(new PollerTask());
		}
	}

	public IgcLogger() {
		this(DEFAULT_IGC_BASE_DIR, Mode.ASYNC);
	}

	public IgcLogger(final String logsFolder) {
		this(logsFolder, Mode.ASYNC);
	}

	private void writeIgcHeader(FileWriter igcFile, ZonedDateTime datetime, Optional<AircraftDescriptor> descriptor) {

		// Write IGC file header
		StringBuilder bld = new StringBuilder();
		try {
			bld.append("AGNE001 OGN gateway").append(LINE_SEP);
			bld.append("HFDTE").append(String.format("%02d", datetime.getDayOfMonth()))
					.append(String.format("%02d", datetime.getMonthValue()))
					.append(String.format("%04d", datetime.getYear()).substring(2)); // last
																						// 2
																						// chars
																						// of
																						// the
																						// year

			if (descriptor != null && descriptor.isPresent())
				bld.append(LINE_SEP).append("HFGIDGLIDERID:").append(descriptor.get().getRegNumber()).append(LINE_SEP)
						.append("HFGTYGLIDERTYPE:").append(descriptor.get().getModel()).append(LINE_SEP)
						.append("HFCIDCOMPETITIONID:").append(descriptor.get().getCN()).append(LINE_SEP);

			igcFile.write(bld.toString());

		} catch (IOException e) {
			LOG.error("exception caught", e);
		}
	}

	private void logToIgcFile(final AircraftBeacon beacon, final Optional<LocalDate> date,
			final Optional<AircraftDescriptor> descriptor) {

		String igcId = IgcUtils.toIgcLogFileId(beacon, descriptor);

		LocalDate d = date.isPresent() ? date.get() : LocalDate.now();

		ZonedDateTime beaconTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(beacon.getTimestamp()),
				ZoneOffset.UTC);

		// take the time part from the beacon
		ZonedDateTime timestamp = ZonedDateTime.of(d,
				LocalTime.of(beaconTimestamp.getHour(), beaconTimestamp.getMinute(), beaconTimestamp.getSecond()),
				ZoneOffset.UTC);

		StringBuilder dateString = new StringBuilder(String.format("%04d", timestamp.getYear())).append("-")
				.append(String.format("%02d", timestamp.getMonth().getValue())).append("-")
				.append(String.format("%02d", timestamp.getDayOfMonth()));

		// Generate filename from date and immat
		String regName = id2reg.get(igcId.substring(3));
		String igcFileName;
		if (regName == null) {
			igcFileName = new String(igcId + ".IGC");
		} else {
			igcFileName = new String(igcId + "_" + regName + ".IGC");
		}

		File theDir = new File(igcBaseDir);
		if (!theDir.exists()) {
			// if directory doesn't exist create it
			if (!theDir.mkdir()) {
				LOG.warn("the directory {} could not be created", theDir);
				return;
			}
		}

		File subDir = new File(igcBaseDir + File.separatorChar + dateString);
		if (!subDir.exists()) {
			// if directory doesn't exist create it
			if (!subDir.mkdir()) {
				LOG.warn("the directory {} could not be created", subDir);
				return;
			}
		}

		String filePath = igcBaseDir + File.separatorChar + dateString + File.separatorChar + igcFileName;

		// Check if the IGC file already exist
		File f = new File(filePath);

		boolean writeHeader = false;
		if (!f.exists())
			// if this is a brand new file - write the header
			writeHeader = true;

		FileWriter igcFile = null;
		// create (if not exists) and/or open the file
		try {
			igcFile = new FileWriter(filePath, true);
		} catch (IOException ex) {
			LOG.error("exception caught", ex);
			return; // no point to continue - file could not be created
		}

		// if this is a brand new file - write the header
		if (writeHeader) {
			// write the igc header
			writeIgcHeader(igcFile, timestamp, descriptor);
		}

		// Add fix
		try {

			StringBuilder bld = new StringBuilder();

			// log original APRS sentence to IGC file for debug, SAR & co
			bld.append("LGNE ").append(beacon.getRawPacket()).append(LINE_SEP);

			bld.append("B").append(String.format("%02d", timestamp.getHour()))
					.append(String.format("%02d", timestamp.getMinute()))
					.append(String.format("%02d", timestamp.getSecond()))
					.append(AprsUtils.degToIgc(beacon.getLat(), Coordinate.LAT))
					.append(AprsUtils.degToIgc(beacon.getLon(), Coordinate.LON)).append("A") // A
					// for
					// 3D
					// fix
					// (and
					// not
					// 2D)
					.append("00000") // baro. altitude (but it is false as we
					// have only GPS altitude
					.append(String.format("%05.0f", beacon.getAlt())) // GPS
					// altitude

					.append(LINE_SEP);

			igcFile.write(bld.toString());

		} catch (IOException e) {
			LOG.error("exception caught", e);
		} finally {
			try {
				igcFile.close();
			} catch (Exception ex) {
				LOG.warn("could not close igc file", ex);
			}
		}
	}

	/**
	 * @param immat
	 *            aircraft registration (if known) or unique tracker/flarm id
	 * @param lat
	 *            latitude
	 * @param lon
	 *            longitude
	 * @param alt
	 *            altitude
	 * @param comment
	 *            a string which will fall into the igc file as a comment (e.g. aprs sentence can be logged for
	 *            debugging purposes)
	 */
	public void log(final AircraftBeacon beacon, final Optional<LocalDate> date,
			final Optional<AircraftDescriptor> descriptor) {
		switch (workingMode) {

		case ASYNC:
			if (!logRecords.offer(new LogRecord(beacon, date, descriptor))) {
				LOG.warn("could not insert LogRecord to the igc logging queue");
			}
			break;

		default:
			logToIgcFile(beacon, date, descriptor);
		}

	}

	public void log(final AircraftBeacon beacon, final Optional<AircraftDescriptor> descriptor) {
		log(beacon, Optional.empty(), descriptor);
	}

	/**
	 * can be used to stop the poller thread. only affects IgcLogger in ASYNC mode
	 */
	public void stop() {
		if (pollerFuture != null) {
			pollerFuture.cancel(false);
		}
	}

	public void setId2reg(Map<String, String> id2reg) {
		this.id2reg = id2reg;
	}

}

/*
 * From IGC spec (http://www.fai.org/gnss-recording-devices/igc-approved-flight-recorders):
 * 
 * Altitude - Metres, separate records for GNSS and pressure altitudes. Date (of the first line in the B record) - UTC
 * DDMMYY (day, month, year). Latitude and Longitude - Degrees, minutes and decimal minutes to three decimal places,
 * with N,S,E,W designators Time - UTC, for source, see para 3.4 in the main body in this document. Note that UTC is not
 * the same as the internal system time in the U.S. GPS system, see under "GPS system time" in the Glossary.
 * 
 * ---
 * 
 * Altitude - AAAAAaaa AAAAA - fixed to 5 digits with leading 0 aaa - where used, the number of altitude decimals (the
 * number of fields recorded are those available for altitude in the Record concerned, less fields already used for
 * AAAAA) Altitude, GNSS. Where GNSS altitude is not available from GNSS position-lines such as in the case of a 2D fix
 * (altitude drop-out), it shall be recorded in the IGC format file as zero so that the lack of valid GNSS altitude can
 * be clearly seen during post-flight analysis.
 * 
 * Date - DDMMYY DD - number of the day in the month, fixed to 2 digits with leading 0 where necessary MM - number of
 * the month in year, fixed to 2 digits with leading 0 where necessary YY - number of the year, fixed to 2 digits with
 * leading 0 where necessary
 * 
 * Lat/Long - D D M M m m m N D D D M M m m m E DD - Latitude degrees with leading 0 where necessary DDD - Longitude
 * degrees with leading 0 or 00 where necessary MMmmmNSEW - Lat/Long minutes with leading 0 where necessary, 3 decimal
 * places of minutes (mandatory, not optional), followed by North, South, East or West letter as appropriate
 * 
 * Time - HHMMSS (UTC) - for optional decimal seconds see "s" below HH - Hours fixed to 2 digits with leading 0 where
 * necessary MM - Minutes fixed to 2 digits with leading 0 where necessary SS - Seconds fixed to 2 digits with leading 0
 * where necessary s - number of decimal seconds (if used), placed after seconds (SS above). If the recorder uses fix
 * intervals of less than one second, the extra number(s) are added in the B-record line, their position on the line
 * being identified in the I-record under the Three Letter Code TDS (Time Decimal Seconds, see the codes in para A7).
 * One number "s" indicates tenths of seconds and "ss" is tenths and hundredths, and so forth. If tenths are used at,
 * for instance, character number 49 in the B-record (after other codes such as FXA, SIU, ENL), this is indicated in the
 * I record as: "4949TDS".
 * 
 * B HHMMSS DDMMmmmN DDDMMmmmE V PPPPP GGGGG CR LF B 130353 4344108N 00547165E A 00275 00275 4533.12N 00559.93E
 * 
 * Condor example: HFDTE140713 HFGIDGLIDERID:F-SEB B1303534344108N00547165EA0027500275
 */
