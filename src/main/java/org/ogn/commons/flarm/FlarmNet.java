package org.ogn.commons.flarm;

import static org.ogn.commons.utils.StringUtils.hex2ascii;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT YET IMPLEMENTED! This class handles FlarmNet db file
 * 
 * @author wbuczak
 */
public class FlarmNet {
    private static final String flarmnetDbFile = "bin/data.fln";

    private List<FlarmnetLine> flarmnetDBcontent = new ArrayList<FlarmnetLine>();

    private static Logger LOG = LoggerFactory.getLogger(FlarmNet.class);

    // @Value
    private String flarmNetDbFile;

    static class FlarmnetLine {
        String flarmID;
        String registration;
        String CN;
    }

    // Constructor
    public FlarmNet() {
        reloadDB();
    }

    public void reloadDB() {
        flarmnetDBcontent.clear(); // Cleanup DB

        // Read file to populate db
        File dbFile = new File(flarmnetDbFile);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dbFile));
            String line;
            while ((line = br.readLine()) != null) {

                String decodedLine = new String(hex2ascii(line));
                LOG.trace(decodedLine);

                if (decodedLine.length() >= 86) { // Correct line
                    FlarmnetLine flarmnetLine = new FlarmnetLine();
                    flarmnetLine.flarmID = decodedLine.substring(0, 6).trim();
                    flarmnetLine.registration = decodedLine.substring(69, 76).trim();
                    flarmnetLine.CN = decodedLine.substring(76, 79).trim(); // Competition
                                                                            // number
                    flarmnetDBcontent.add(flarmnetLine);

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

    public String id2reg(String id) {
        for (FlarmnetLine flarmnetLine : flarmnetDBcontent) {
            if (id.equals(flarmnetLine.flarmID)) {
                return flarmnetLine.registration;
            }
        }
        return null;
    }

    public String reg2id(String reg) {
        for (FlarmnetLine flarmnetLine : flarmnetDBcontent) {
            if (reg.equals(flarmnetLine.registration)) {
                return flarmnetLine.flarmID;
            }
        }
        return null;
    }

    public String id2cn(String id) {
        for (FlarmnetLine flarmnetLine : flarmnetDBcontent) {
            if (id.equals(flarmnetLine.flarmID)) {
                return flarmnetLine.CN;
            }
        }
        return null;
    }

    // public void populateGliderfix(AircraftBeacon currentFix) {
    //
    // for (FlarmnetLine flarmnetLine : flarmnetDBcontent) {
    // if ((currentFix.Id != null)
    // && (currentFix.Id.equals(flarmnetLine.flarmID))) {
    // currentFix.regNum = flarmnetLine.registration;
    // currentFix.CN = flarmnetLine.CN;
    // break;
    // }
    // }
    // if (currentFix.regNum == null) { // Not in FlarmnetDB
    // if (currentFix.idType == 1)
    // // currentFix.regnum = "ICA"+currentFix.Id;
    // currentFix.regNum = currentFix.aprsId;
    // else
    // currentFix.regNum = "FLR" + currentFix.Id;
    // }
    // if ((currentFix.CN == null) || (currentFix.CN.trim().equals(""))) // If
    // // CN
    // // not
    // // found
    // // add
    // // last
    // // 2
    // // chars
    // // from
    // // reg
    // // num
    // currentFix.CN = currentFix.regNum.substring(Math.max(0,
    // currentFix.regNum.length() - 2));
    // }
}