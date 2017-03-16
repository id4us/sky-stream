package com.sky.mobile.skytvstream.utils;

public enum SubscriptionProvider {

    VODAFONE("VODAFONE", 1),
    GOOGLE("GOOGLE", 1),
    APPLE("APPLE", 10);

    private final String provName;
    private int maxSupportedDevices;

    SubscriptionProvider(String name, int maxSupportedDevices) {
        this.provName = name;
        this.maxSupportedDevices = maxSupportedDevices;
    }

    public String getProviderName() {
        return provName;
    }

    public int getMaxSupportedDevices() {
        return maxSupportedDevices;
    }

    public static SubscriptionProvider fromName(String name) {
        for (SubscriptionProvider s : SubscriptionProvider.values()) {
            if (s.getProviderName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

}
