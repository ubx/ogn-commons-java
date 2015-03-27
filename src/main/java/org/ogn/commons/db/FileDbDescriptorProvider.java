/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.db;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.beacon.descriptor.AircraftDescriptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This AircraftDescriptorProvider resolves AircraftDescriptors from OGN db. It can be configured to refresh its
 * internal cache periodically.
 * 
 * @author wbuczak
 */
public class FileDbDescriptorProvider<T extends FileDb> implements AircraftDescriptorProvider {

    private T db;
    private ScheduledExecutorService scheduledExecutor;

    private static Logger LOG = LoggerFactory.getLogger(FileDbDescriptorProvider.class);

    // default refresh rate (in sec.)
    private static final int DEFAULT_DB_INTERVAL = 60 * 60;

    public FileDbDescriptorProvider(Class<T> clazz, String dbFileUri, int dbRefreshInterval) {

        try {
            db = clazz.getConstructor(String.class).newInstance(dbFileUri);
            LOG.info(
                    "creating and initializing desciptor privider with parameters: uri: {}, refresh-interval: {} class: {}",
                    db.getUrl(), dbRefreshInterval, clazz.getCanonicalName());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOG.error("instantiation of descriptor provider failed!", e);
            return;
        }

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {

                LOG.debug("reloading db {}", db.getClass().getName());
                db.reload();

            }
        }, 0, dbRefreshInterval, TimeUnit.SECONDS);

    }

    public FileDbDescriptorProvider(Class<T> clazz, int dbRefreshInterval) {
        this(clazz, null, dbRefreshInterval);
    }

    public FileDbDescriptorProvider(Class<T> clazz) {
        this(clazz, null, DEFAULT_DB_INTERVAL);
    }

    @Override
    public AircraftDescriptor findDescriptor(String address) {
        LOG.trace("entering findDescriptor()..");
        return db.getDescriptor(address);
    }

}