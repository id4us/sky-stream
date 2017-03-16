package com.sky.mobile.skytvstream.utils;

public enum SubscriptionStatus {

    ACTIVE_SUBSCRIPTIONS("active subscriptions found"),
    NO_SUBSCRIPTIONS("no subscriptions found"),
    EXPIRED_SUBSCRIPTIONS("All subscriptions have expired"),
    DEVICE_LIMIT_EXCEEDED_FOR_SUBSCRIPTIONS("device limit reached");

    private String message;

    SubscriptionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
