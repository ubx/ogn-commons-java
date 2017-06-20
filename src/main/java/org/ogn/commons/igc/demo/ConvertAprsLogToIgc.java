package org.ogn.commons.igc.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.OgnBeacon;
import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;
import org.ogn.commons.beacon.impl.aprs.AprsLineParser;
import org.ogn.commons.igc.IgcLogger;
import org.ogn.commons.igc.IgcLogger.Mode;

/**
 * This small demo class demonstrates the basic usage of <code>IgcLogger</code>
 *
 */
public class ConvertAprsLogToIgc {

	static final String DEFAULT_INPUT_FILE = "c://Temp/aprs-log.txt";

	public static String getAprsLogFileName(String[] args) {
		String aprsLogfileName = DEFAULT_INPUT_FILE;
		// you can pass the path to the input aprs-logs file, if not, default will be taken
		if (args.length > 0) {
			aprsLogfileName = args[0];
		}

		return aprsLogfileName;
	}

	public static IgcLogger createIgcLogger(String[] args) {
		IgcLogger igcLogger;
		// you can pass the path to the output folder, if not, default will be taken
		if (args.length > 1) {
			igcLogger = new IgcLogger(args[1], Mode.SYNC);
		} else {
			igcLogger = new IgcLogger(Mode.SYNC);
		}
		return igcLogger;

	}

	static void processAprsLogFile(String aprsLogFileName, IgcLogger igcLogger, AircraftDescriptorProvider provider) {
		try (Stream<String> stream = Files.lines(Paths.get(aprsLogFileName))) {

			stream.forEach(

					l -> {
						try {
							OgnBeacon ognBeacon = AprsLineParser.get().parse(l);

							// just take aircraft beacons (a must, if the log file contains mix)
							if (ognBeacon instanceof AircraftBeacon) {
								AircraftBeacon aircraftBeacon = (AircraftBeacon) ognBeacon;
								igcLogger.log(aircraftBeacon, provider == null ? Optional.empty()
										: provider.findDescriptor(aircraftBeacon.getAddress()));
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

			);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String aprsLogfileName = getAprsLogFileName(args);
		IgcLogger igcLogger = createIgcLogger(args);

		processAprsLogFile(aprsLogfileName, igcLogger, null);
		System.exit(0);
	}
}
