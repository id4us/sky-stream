package com.sky.mobile.skytvstream.domain;

import com.sky.mobile.skytvstream.utils.DeviceVendor;

public class DeviceVo {

    private String model;
    private String os;

    public DeviceVo() {
    }

    public DeviceVo(String os, String model) {
        this.os = os;
        this.model = model;
    }

    public DeviceVendor getDeviceVendorEnum() {
        return DeviceVendor.fromName(os);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

	@Override
	public String toString() {
		return "DeviceVo [model=" + model + ", os=" + os + "]";
	}
    
}
