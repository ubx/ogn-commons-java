package org.ogn.commons.flarm;

import static org.ogn.commons.utils.StringUtils.hex2ascii;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.impl.AircraftDescriptorImpl;
import org.ogn.commons.utils.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles FlarmNet db. Flarmnet data can be loaded from remote server (e.g. directly from flarmnet) or from
 * a local flarmnet db. file and it is kept in the internal cache. Every time refresh() is called the cache will be
 * updated. This class is thread-safe!
 * 
 * @author Seb, wbuczak
 */
public class FlarmNet {

    private static final int FLARMNET_LINE_LENGTH = 86;

    private static Logger LOG = LoggerFactory.getLogger(FlarmNet.class);

    private static final String DEFAULT_FLARMNET_FILE_URL = "http://flarmnet.org/files/data.fln";

    private static final String PROTOCOL_FILE = "file";

    private ConcurrentMap<String, AircraftDescriptor> flarmNetCache = new ConcurrentHashMap<>();

    private String flarmnetFileUri;

    public FlarmNet() {
        this(DEFAULT_FLARMNET_FILE_URL);
    }

    public FlarmNet(String flarmnetFileUri) {
        if (flarmnetFileUri == null) {
            this.flarmnetFileUri = DEFAULT_FLARMNET_FILE_URL;
        } else {
            this.flarmnetFileUri = flarmnetFileUri;
        }
    }

    public synchronized void reload() {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        BufferedReader br = null;
        try {
            URL url = null;

            try {
                url = new URL(flarmnetFileUri);

                if (url.getProtocol().equals(PROTOCOL_FILE)) {
                    String path = url.getPath().substring(1); // get rid of leading slash
                    br = new BufferedReader(new FileReader(path));
                } else {
                    Streams.copy(url.openStream(), bos);
                    br = new BufferedReader(new StringReader(bos.toString()));
                }
            }

            catch (MalformedURLException ex) {
                // for malformed urls - still try to open it as a regular file
                br = new BufferedReader(new FileReader(flarmnetFileUri));
            }

            String line;
            while ((line = br.readLine()) != null) {
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

                    AircraftDescriptor desc = new AircraftDescriptorImpl(regNumber, cn, owner, home, model, freq);

                    if (flarmNetCache.replace(id, desc) == null) {
                        flarmNetCache.put(id, desc);
                    }
                }
            }// while
        } catch (Exception e) {
            LOG.error("Exception exception caught", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
                // nothing can be done, apart from logging
                LOG.warn("Exception exception caught", e);
            }
        }
    }

    public AircraftDescriptor getDescriptor(String id) {
        if (id == null)
            return null;
        return flarmNetCache.get(id);
    }
}