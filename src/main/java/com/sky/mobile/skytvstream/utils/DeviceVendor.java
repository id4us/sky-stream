package com.sky.mobile.skytvstream.utils;

import com.google.common.base.Preconditions;

public enum DeviceVendor {

    ANDROID("android"),
    IOS("ios"),
    UNKNOWN("unknown");

    private final String vendor;

    DeviceVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    public static DeviceVendor fromName(String vendor) {
        Preconditions.checkNotNull(vendor, "Name cannot be null");
        for (DeviceVendor v : DeviceVendor.values()) {
            if (v.getVendor().equalsIgnoreCase(vendor)) {
                return v;
            }
        }
        return DeviceVendor.UNKNOWN;
    }

}
