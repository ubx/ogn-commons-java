/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.igc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.ogn.commons.utils.AprsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IGC logger creates and writes to IGC files. The logger's log() operation is non-blocking (logs are written to a
 * file by a background thread)
 * 
 * @author Seb, wbuczak
 */
public class IgcLogger {

    public static enum Mode {
        ASYNC,
        SYNC
    }

    private static final Logger LOG = LoggerFactory.getLogger(IgcLogger.class);

    private static final String DEFAULT_IGC_BASE_DIR = "log";

    private String igcBaseDir;

    private static final String LINE_SEP = System.lineSeparator();

    private Mode workingMode;

    private BlockingQueue<LogRecord> logRecords;
    private volatile Future<?> pollerFuture;
    private ExecutorService executor;

    private static class LogRecord {
        String immat;
        double lat;
        double lon;
        float alt;
        String comment;

        LogRecord(String immat, double lat, double lon, float alt, String comment) {
            this.immat = immat;
            this.lat = lat;
            this.lon = lon;
            this.alt = alt;
            this.comment = comment;
        }
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
                    logToIgcFile(record.immat, record.lat, record.lon, record.alt, record.comment);
                } catch (InterruptedException e) {
                    PLOG.trace("interrupted exception caught. Was the poller task interrupted on purpose?");
                    Thread.currentThread().interrupt();
                    continue;
                }
            }// while
            PLOG.trace("exiting..");
        }
    }

    public IgcLogger(Mode mode) {
        this(DEFAULT_IGC_BASE_DIR, mode);
    }

    public IgcLogger(final String logsFolder, Mode mode) {
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

    private void writeIgcHeader(FileWriter igcFile, Calendar calendar, String immat) {
        // Write IGC file header
        try {
            igcFile.write("AGNE001Glider network gateway" + LINE_SEP);
            igcFile.write("HFDTE" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
                    + String.format("%02d", calendar.get(Calendar.MONTH) + 1)
                    + String.format("%04d", calendar.get(Calendar.YEAR)).substring(2) + // last 2 chars of the year
                    LINE_SEP);
            igcFile.write("HFGIDGLIDERID:" + immat + LINE_SEP);
        } catch (IOException e) {
            LOG.error("exception caught", e);
        }
    }

    private void logToIgcFile(String immat, double lat, double lon, float alt, String comment) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(System.currentTimeMillis());

        String dateString = new String(String.format("%04d", calendar.get(Calendar.YEAR)) + "-"
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-"
                + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));

        // Generate filename from date and immat
        String igcFileName = new String(dateString + "_" + immat + ".IGC");

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

        FileWriter igcFile = null;
        // create (if not exists) and/or open the file
        try {
            igcFile = new FileWriter(filePath, true);
        } catch (IOException ex) {
            LOG.error("exception caught", ex);
            return; // no point in continuing - file could not be created
        }

        // if this is a brand new file - write the header
        if (!f.exists()) {
            // write the igc header
            writeIgcHeader(igcFile, calendar, immat);
        }

        // Add fix
        try {
            char latitudeWay = 'N';
            if (lat < 0.0f)
                latitudeWay = 'S';
            char longitudeWay = 'E';
            if (lon < 0.0f)
                longitudeWay = 'W';

            if (comment != null) {
                // log original APRS sentence to IGC file for debug, SAR & co
                igcFile.write("LGNE " + comment + LINE_SEP);
            }

            igcFile.write("B"
                    + // Fix
                    String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
                    + // Time of day
                    String.format("%02d", calendar.get(Calendar.MINUTE))
                    + String.format("%02d", calendar.get(Calendar.SECOND))
                    + String.format("%07.0f", Math.abs(AprsUtils.degToDms(lat) * 100 * 100 * 10)) + latitudeWay
                    + String.format("%08.0f", Math.abs(AprsUtils.degToDms(lon) * 100 * 100 * 10)) + longitudeWay + "A" + // A
                                                                                                                         // for
                                                                                                                         // 3D
                                                                                                                         // fix
                                                                                                                         // (and
                                                                                                                         // not
                                                                                                                         // 2D)
                    "00000" + // Baro altitude (but it is false as we have only GPS altitude
                    String.format("%05.0f", alt) + // GPS altitude
                    LINE_SEP);
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

    public void log(String immat, double lat, double lon, float alt) {
        log(immat, lat, lon, alt, null);
    }

    /**
     * @param immat aircraft registration (if known) or unique tracker/flarm id
     * @param lat latitude
     * @param lon longitude
     * @param alt altitude
     * @param comment a string which will fall into the igc file as a comment (e.g. aprs sentence can be logged for
     *            debugging purposes)
     */
    public void log(String immat, double lat, double lon, float alt, String comment) {
        switch (workingMode) {

        case ASYNC:
            if (!logRecords.offer(new LogRecord(immat, lat, lon, alt, comment))) {
                LOG.warn("could not insert LogRecord to the igc logging queue");
            }
            break;

        default:
            logToIgcFile(immat, lat, lon, alt, comment);
        }

    }

    /**
     * can be used to stop the poller thread. only affects IgcLogger in ASYNC mode
     */
    public void stop() {
        if (pollerFuture != null) {
            pollerFuture.cancel(false);
        }
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
