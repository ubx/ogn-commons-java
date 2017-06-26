package org.ogn.commons.igc.demo;

import java.time.LocalDate;

import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;
import org.ogn.commons.db.FileDbDescriptorProvider;
import org.ogn.commons.db.ogn.OgnDb;
import org.ogn.commons.igc.IgcLogger;

/**
 * This small demo class demonstrates the basic usage of <code>IgcLogger</code> In this example <code>OgnDb</code>
 * aircraft descriptor provider is used. If an aircraft is registered, its reg number & cn will be appended in the igc
 * file name.
 */
public class ConvertAprsLogToIgc2 {

	static AircraftDescriptorProvider provider = new FileDbDescriptorProvider<OgnDb>(OgnDb.class);

	public static void main(String[] args) throws Exception {

		String aprsLogfileName = ConvertAprsLogToIgc.getAprsLogFileName(args);
		IgcLogger igcLogger = ConvertAprsLogToIgc.createIgcLogger(args);
		LocalDate dateOfFlight = ConvertAprsLogToIgc.getDateOfFlight(args);
		
		ConvertAprsLogToIgc.processAprsLogFile(aprsLogfileName, dateOfFlight, igcLogger, provider);

		System.exit(0);
	}

}
