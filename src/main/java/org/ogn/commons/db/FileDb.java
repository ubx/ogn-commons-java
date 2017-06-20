/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.db;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.utils.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for loading data from "file" databases, such as FlarmNet db or OGN db. Data can be loaded from
 * remote server (e.g. directly from flarmnet or ogn web server) or from a local db file and is kept in the internal
 * cache. When refresh() is called the cache is updated. This class is thread-safe!
 * 
 * @author Seb, wbuczak
 */
public abstract class FileDb {

	private static Logger LOG = LoggerFactory.getLogger(FileDb.class);

	public static final String PROTOCOL_FILE = "file";

	protected static class AircraftDescriptorWithId {
		private String id;
		private AircraftDescriptor desc;

		public AircraftDescriptorWithId(String id, AircraftDescriptor desc) {
			this.id = id;
			this.desc = desc;
		}
	}

	private ConcurrentMap<String, AircraftDescriptor> cache = new ConcurrentHashMap<>();

	private String dbFileUri;

	protected FileDb(String dbFileUri) {
		this.dbFileUri = dbFileUri == null ? getDefaultDbFileUri() : dbFileUri;
	}

	public String getUrl() {
		return this.dbFileUri;
	}

	protected abstract String getDefaultDbFileUri();

	protected abstract AircraftDescriptorWithId processLine(String line);

	public synchronized void reload() {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		BufferedReader br = null;
		try {
			URL url = null;

			try {
				url = new URL(dbFileUri);

				if (url.getProtocol().equals(PROTOCOL_FILE)) {
					String path = url.getPath().substring(1); // get rid of
																// leading slash
					br = new BufferedReader(new FileReader(path));
				} else {
					Streams.copy(url.openStream(), bos);
					br = new BufferedReader(new StringReader(bos.toString()));
				}
			}

			catch (MalformedURLException ex) {
				// for malformed urls - still try to open it as a regular file
				br = new BufferedReader(new FileReader(dbFileUri));
			} catch (Exception ex) {
				LOG.error("Exception caught", ex);
				return;
			}

			String line;
			while ((line = br.readLine()) != null) {
				try {
					AircraftDescriptorWithId record = processLine(line);

					if (record != null && record.id != null)
						if (cache.replace(record.id, record.desc) == null) {
							LOG.trace("putting into the cache record with key: {}", record.id);
							cache.put(record.id, record.desc);
						}
				} catch (Exception e) {
					LOG.error("Exception caught", e);
				}

			} // while

		} catch (Exception e) {
			LOG.error("Exception caught", e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
				// nothing can be done, apart from logging
				LOG.warn("Exception caught", e);
			}
		}
	}

	public Optional<AircraftDescriptor> getDescriptor(String id) {
		if (null == id || null == cache.get(id))
			return Optional.empty();
		return Optional.of(cache.get(id));
	}
}