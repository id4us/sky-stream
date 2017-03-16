package com.sky.mobile.skytvstream.dao;

import com.sky.mobile.skytvstream.domain.RegisteredDevice;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

public interface RegisteredDeviceDao {

    RegisteredDevice findByProfileIdForProvider(String profileId, SubscriptionProvider subscriptionProvider);

    void insertRegisteredDevice(RegisteredDevice registeredDevice);

    void updateRegisteredDevice(RegisteredDevice registeredDevice);

}
