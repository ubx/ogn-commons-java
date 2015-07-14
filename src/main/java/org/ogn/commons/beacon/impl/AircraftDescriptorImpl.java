/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl;

import java.io.Serializable;

import org.ogn.commons.beacon.AircraftDescriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AircraftDescriptorImpl implements AircraftDescriptor, Serializable {

	public static AircraftDescriptor UNKNOWN_AIRCRAFT_DESCRIPTOR = new AircraftDescriptorImpl();

	private static final long serialVersionUID = -5907836745963656899L;

	/**
	 * @param regNumber
	 * @param cn
	 * @param owner
	 * @param home
	 * @param model
	 * @param freq
	 * @param tracked
	 * @param identified
	 */
	public AircraftDescriptorImpl(String regNumber, String cn, String owner, String home, String model, String freq,
			boolean tracked, boolean identified) {
		this.regNumber = regNumber;
		this.cn = cn;
		this.owner = owner;
		this.homeBase = home;
		this.model = model;
		this.freq = freq;
		this.tracked = tracked;
		this.identified = identified;
	}

	public AircraftDescriptorImpl(String regNumber, String cn, String model, boolean tracked, boolean identified) {
		this(regNumber, cn, null, null, model, null, tracked, identified);
	}

	private String regNumber;
	private String cn;
	private String owner;
	private String homeBase;
	private String model;
	private String freq;

	private boolean tracked;
	private boolean identified;

	protected AircraftDescriptorImpl() {
	}

	@Override
	public String getRegNumber() {
		return regNumber;
	}

	@Override
	public String getCN() {
		return cn;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public String getHomeBase() {
		return homeBase;
	}

	@Override
	public String getModel() {
		return model;
	}

	@Override
	public String getFreq() {
		return freq;
	}

	@Override
	@JsonIgnore
	public boolean isKnown() {
		// to be considered as "known" a descriptor must have at least
		// registration number
		return regNumber != null;
	}

	@Override
	public boolean isTracked() {
		return tracked;
	}

	@Override
	public boolean isIdentified() {
		return identified;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cn == null) ? 0 : cn.hashCode());
		result = prime * result + ((freq == null) ? 0 : freq.hashCode());
		result = prime * result + ((homeBase == null) ? 0 : homeBase.hashCode());
		result = prime * result + (identified ? 1231 : 1237);
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((regNumber == null) ? 0 : regNumber.hashCode());
		result = prime * result + (tracked ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AircraftDescriptorImpl other = (AircraftDescriptorImpl) obj;
		if (cn == null) {
			if (other.cn != null)
				return false;
		} else if (!cn.equals(other.cn))
			return false;
		if (freq == null) {
			if (other.freq != null)
				return false;
		} else if (!freq.equals(other.freq))
			return false;
		if (homeBase == null) {
			if (other.homeBase != null)
				return false;
		} else if (!homeBase.equals(other.homeBase))
			return false;
		if (identified != other.identified)
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (regNumber == null) {
			if (other.regNumber != null)
				return false;
		} else if (!regNumber.equals(other.regNumber))
			return false;
		if (tracked != other.tracked)
			return false;
		return true;
	}

}