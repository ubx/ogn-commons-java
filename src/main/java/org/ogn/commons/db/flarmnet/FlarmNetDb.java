package org.ogn.commons.db.flarmnet;

import static org.ogn.commons.utils.StringUtils.hex2ascii;

import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.impl.AircraftDescriptorImpl;
import org.ogn.commons.db.FileDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles FlarmNet db.
 * 
 * @author Seb, wbuczak
 */
public class FlarmNetDb extends FileDb {

    private static final int FLARMNET_LINE_LENGTH = 86;

    private static Logger LOG = LoggerFactory.getLogger(FlarmNetDb.class);

    private static final String DEFAULT_FLARMNET_DB_URL = "http://flarmnet.org/files/data.fln";

    public FlarmNetDb() {
        this(DEFAULT_FLARMNET_DB_URL);
    }

    public FlarmNetDb(String flarmnetFileUri) {
        super(flarmnetFileUri);
    }

    @Override
    protected AircraftDescriptorWithId processLine(String line) {

        AircraftDescriptorWithId result = null;

        String decodedLine = new String(hex2ascii(line));
        LOG.trace(decodedLine);

        if (decodedLine.length() == FLARMNET_LINE_LENGTH) {

            String id = decodedLine.substring(0, 6).trim();
            String owner = decodedLine.substring(6, 26).trim();
            String home = decodedLine.substring(27, 48).trim();
            String model = decodedLine.substring(48, 69).trim();
            String regNumber = decodedLine.substring(69, 76).trim();
            String cn = decodedLine.substring(76, 79).trim();
            String freq = decodedLine.substring(79, 86).trim();

            AircraftDescriptor desc = new AircraftDescriptorImpl(regNumber, cn, owner, home, model, freq, false, false);

            result = new AircraftDescriptorWithId(id, desc);
        }

        return result;
    }

    @Override
    protected String getDefaultDbFileUri() {        
        return DEFAULT_FLARMNET_DB_URL;
    }
}