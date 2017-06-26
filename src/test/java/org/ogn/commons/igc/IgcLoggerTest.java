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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.impl.AircraftDescriptorImpl;
import org.ogn.commons.beacon.impl.aprs.AprsAircraftBeacon;

public class IgcLoggerTest {

	static String[] aprsPhrases = {
			"FLRDDA325>APRS,qAS,EHHO:/102536h5244.42N/00632.32E'090/075/A=000813 id06DD82AC +198fpm -0.1rot 19.0dB 0e +0.1kHz gps2x3 hear8222 hear9350 hearA4EC",
			"ICA4B0CA7>APRS,qAS,Salland:/102537h5228.09N/00620.44E'/A=000000 id06DDD7EA -019fpm +0.0rot 25.5dB 0e -7.8kHz gps2x2 hearD7F8 hearDA95",
			"FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.80N/00631.57E'/A=000030 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2 hear8222 hear82AC hear9350",
			"FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.80N/00630.57E'/A=000040 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2 hear8222 hear9350",
			"FLRDDA4EC>APRS,qAS,EHHO:/102538h5243.82N/00632.57E'/A=000050 id06DDA4EC +020fpm +0.0rot 20.8dB 0e -7.6kHz gps2x2",
			"FLRDDF984>APRS,qAS,Salland:/102539h5228.39N/00621.07E'124/057/A=000512 id06DDD7F8 -157fpm +1.2rot 23.2dB 0e -1.2kHz gps2x2 hearD7EA hearDA95" };

	static AircraftDescriptor ad1 = new AircraftDescriptorImpl("A-BCD", "aa", "Jantar std 2", true, true);
	static AircraftDescriptor ad2 = new AircraftDescriptorImpl("X-ABC", "w2", "ASW 20", true, true);
	static AircraftDescriptor ad3 = new AircraftDescriptorImpl("F-ABC", "", "Pegase 90", true, true);
	static AircraftDescriptor ad4 = new AircraftDescriptorImpl("PH-8020", "3c", "Discus", true, true);

	static AircraftDescriptor[] descriptors = { ad1, ad2, ad3, ad3, ad3, ad4 };

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
		// Path dir = Paths.get("log/" + date);

		Path dir = Paths.get("log/");
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
	public void testSync() throws Exception {
		IgcLogger logger = new IgcLogger(IgcLogger.Mode.SYNC);

		int i = 0;
		for (String aprsLine : aprsPhrases) {
			AircraftBeacon beacon = new AprsAircraftBeacon(aprsLine);
			logger.log(beacon, Optional.of(descriptors[i++]));
		}

		commonVerification(date);
	}

	@Test
	public void testSyncWithPredefinedDate() throws Exception {
		IgcLogger logger = new IgcLogger(IgcLogger.Mode.SYNC);

		LocalDate date = LocalDate.of(2016, 06, 14);

		int i = 0;
		for (String aprsLine : aprsPhrases) {
			AircraftBeacon beacon = new AprsAircraftBeacon(aprsLine);
			logger.log(beacon, Optional.of(date), Optional.of(descriptors[i++]));
		}

		commonVerification(date.toString());
	}

	@Test
	public void testAsync() throws Exception {
		IgcLogger logger = new IgcLogger();

		int i = 0;
		for (String aprsLine : aprsPhrases) {
			AircraftBeacon beacon = new AprsAircraftBeacon(aprsLine);
			logger.log(beacon, Optional.of(descriptors[i++]));
		}

		// wait a bit..
		Thread.sleep(1000);

		commonVerification(date);
	}

	private void commonVerification(String date) throws Exception {
		// make sure files were created
		// delete log folder if it exists
		Path dir = Paths.get("log/" + date);
		if (!Files.exists(dir))
			fail("log directory was expected!");

		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
			if (p.contains("FLRDDA325")) {
				// 1 fixes + 1 comment (aprs lines) + 5 header lines
				expectRows(1 + 1 + 5, f);
			} else if (p.contains("FLRDDA4EC")) {
				// 3 fixes + 3 comments (aprs lines) + 5 header lines
				expectRows(3 + 3 + 5, f);
				verifyHeader(f, ad3);
			}
		}
	}

	private static void expectRows(int rows, Path file) throws Exception {
		List<String> lines = Files.readAllLines(file, Charset.defaultCharset());
		assertEquals(rows, lines.size());
	}

	private static void verifyHeader(Path file, AircraftDescriptor descriptor) throws Exception {
		List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

		int expectedFieldsCounter = 0;
		for (String line : lines) {
			if (line.startsWith("HFGIDGLIDERID")) {
				String[] tokens = line.split(":");
				expectedFieldsCounter++;

				if (descriptor.getRegNumber() != null & descriptor.getRegNumber().length() > 0)
					if (tokens.length > 1)
						assertEquals(descriptor.getRegNumber(), tokens[1].trim());
					else
						fail("Aircraft model not found in the header");

			} else if (line.startsWith("HFGTYGLIDERTYPE")) {
				String[] tokens = line.split(":");
				expectedFieldsCounter++;

				if (descriptor.getModel() != null & descriptor.getModel().length() > 0)
					if (tokens.length > 1)
						assertEquals(descriptor.getModel(), tokens[1].trim());
					else
						fail("Aircraft model not found in the header");
			} else if (line.startsWith("HFCIDCOMPETITIONID")) {
				String[] tokens = line.split(":");
				expectedFieldsCounter++;

				if (descriptor.getCN() != null & descriptor.getCN().length() > 0)
					if (tokens.length > 1)
						assertEquals(descriptor.getCN(), tokens[1].trim());
					else
						fail("CN not found in the header");
			}

		}

		assertEquals(3, expectedFieldsCounter);
	}

}
