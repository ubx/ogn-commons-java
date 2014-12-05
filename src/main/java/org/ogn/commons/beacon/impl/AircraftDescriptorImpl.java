/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.commons.beacon.impl;

import java.io.Serializable;

import org.ogn.commons.beacon.AircraftDescriptor;

public class AircraftDescriptorImpl implements AircraftDescriptor, Serializable {

    private static final long serialVersionUID = -5907836745963656899L;

    /**
     * @param regNumber
     * @param cn
     * @param owner
     * @param home
     * @param model
     * @param freq
     */
    public AircraftDescriptorImpl(String regNumber, String cn, String owner, String home, String model, String freq) {
        this.regNumber = regNumber;
        this.cn = cn;
        this.owner = owner;
        this.homeBase = home;
        this.model = model;
        this.freq = freq;
    }

    private String regNumber;
    private String cn;
    private String owner;
    private String homeBase;
    private String model;
    private String freq;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cn == null) ? 0 : cn.hashCode());
        result = prime * result + ((freq == null) ? 0 : freq.hashCode());
        result = prime * result + ((homeBase == null) ? 0 : homeBase.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((regNumber == null) ? 0 : regNumber.hashCode());
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
        return true;
    }
}