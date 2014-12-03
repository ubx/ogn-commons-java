/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.igc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;
import org.ogn.commons.beacon.OgnBeacon;
import org.ogn.commons.beacon.impl.aprs.AprsAircraftBeacon;

public class IgcLoggerTest {

    static String[] aprsPhrases = {
            "PH-844>APRS,qAS,EHHO:/102536h5244.42N/00632.32E'090/075/A=000813 id06DD82AC +198fpm -0.1rot 19.0dB 0e +0.1kHz gps2x3 hear8222 hear9350 hearA4EC",
            "PH-881>APRS,qAS,Salland:/102537h5228.09N/00620.44E'/A=000000 id06DDD7EA -019fpm +0.0rot 25.5dB 0e -7.8kHz gps2x2 hearD7F8 hearDA95",
            "FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.80N/00631.57E'/A=000030 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2 hear8222 hear82AC hear9350",
            "FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.80N/00630.57E'/A=000040 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2 hear8222 hear9350",
            "FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.82N/00632.57E'/A=000050 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2",
            "PH-1293>APRS,qAS,Salland:/102539h5228.39N/00621.07E'124/057/A=000512 id06DDD7F8 -157fpm +1.2rot 23.2dB 0e -1.2kHz gps2x2 hearD7EA hearDA95" };

    static String getDate() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new String(String.format("%04d", calendar.get(Calendar.YEAR)) + "-"
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-"
                + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
    }

    static String date = getDate();

    List<Path> files = new ArrayList<>();

    @After
    public void cleanUp() throws Exception {

        files.clear();

        // delete log folder if it exists
        Path dir = Paths.get("log/" + date);
        if (Files.exists(dir)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }

    @Test
    public void test() throws Exception {
        IgcLogger logger = new IgcLogger();

        for (String aprsLine : aprsPhrases) {
            OgnBeacon beacon = new AprsAircraftBeacon(aprsLine);
            logger.log(beacon.getId(), beacon.getLat(), beacon.getLon(), beacon.getAlt(), aprsLine);
        }

        // make sure files were created
        // delete log folder if it exists
        Path dir = Paths.get("log/" + date);
        if (!Files.exists(dir))
            fail("log directory was expected!");

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // System.out.println(file.getFileName());
                files.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

        });

        assertEquals(4, files.size());

        for (Path f : files) {
            String p = f.toString();
            if (p.contains("PH-844")) {
                // 1 fixes + 1 comment (aprs lines)
                expectRows(1 + 1, f);
            } else if (p.contains("FLRDDA4EC")) {
                // 3 fixes + 3 comments (aprs lines)
                expectRows(3 + 3, f);
            }
        }
    }

    private static void expectRows(int rows, Path file) throws Exception {
        List<String> lines = Files.readAllLines(file, Charset.defaultCharset());
        assertEquals(rows, lines.size());
    }

}
