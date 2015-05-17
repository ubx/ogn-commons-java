/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.utils;

import java.util.regex.Pattern;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;

public class IgcUtils {

    /**
     * utility method preparing identifiers used for IGC log file naming
     * 
     * @param beacon
     * @param descriptor
     * @return
     */
    public static String toIgcLogFileId(final AircraftBeacon beacon, final AircraftDescriptor descriptor) {
        StringBuilder id = new StringBuilder(beacon.getId());
        if (descriptor.isKnown()) {

            // \/:*?"<>|
            final Pattern p = Pattern.compile("[^\\/:*?\"<>|]*$");

            String reg = descriptor.getRegNumber();
            String cn = descriptor.getCN();

            reg = reg == null || (!p.matcher(reg).matches()) ? null : reg.toUpperCase();
            cn = cn == null || (!p.matcher(cn).matches()) ? null : cn.toUpperCase();

            if (reg != null && !reg.isEmpty() && cn != null && !cn.isEmpty())
                id.append("_").append(reg).append("_").append(cn);
            else if (reg != null && !reg.isEmpty())
                id.append("_").append(reg);
            else if (cn != null && !cn.isEmpty())
                id.append("_").append(cn);
        }

        return id.toString();
    }
}