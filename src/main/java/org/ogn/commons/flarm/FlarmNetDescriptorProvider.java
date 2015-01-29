/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.flarm;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This AircraftDescriptorProvider resolves AircraftDescriptors from FlarmNet It can be configured to refresh its
 * internal cache periodically.
 * 
 * @author wbuczak
 */
public class FlarmNetDescriptorProvider implements AircraftDescriptorProvider {

    private FlarmNet fn;
    private ScheduledExecutorService scheduledExecutor;

    private static Logger LOG = LoggerFactory.getLogger(FlarmNetDescriptorProvider.class);

    // default refresh rate (in sec.)
    private static final int DEFAULT_DB_INTERVAL = 60 * 60;

    public FlarmNetDescriptorProvider(String flarmnetFileUrl, int dbRefreshInterval) {
        LOG.debug(
                "creating and initializing FlarmNetDescriptor provider with parameters: flarmnetFileUrl: {}, dbRefreshInterval: {}",
                flarmnetFileUrl, dbRefreshInterval);

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
    public AircraftDescriptor findDescriptor(String address) {
        return fn.getDescriptor(address);
    }

}