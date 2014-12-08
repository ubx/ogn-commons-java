/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.flarm;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;

public class FlarmNetDescriptorProvider implements AircraftDescriptorProvider {

    private FlarmNet fn;
    private ScheduledExecutorService scheduledExecutor;

    // in sek
    private static final int DEFAULT_DB_INTERVAL = 60 * 60;

    public FlarmNetDescriptorProvider(String flarmnetFileUrl, int dbRefreshInterval) {
        fn = new FlarmNet(flarmnetFileUrl);
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                fn.reload();

            }
        }, 0, dbRefreshInterval, TimeUnit.SECONDS);
    }

    public FlarmNetDescriptorProvider(int dbRefreshInterval) {
        this(null, dbRefreshInterval);
    }

    public FlarmNetDescriptorProvider() {
        this(null, DEFAULT_DB_INTERVAL);
    }

    @Override
    public AircraftDescriptor getDescritor(String address) {
        return fn.getDescriptor(address);
    }

}