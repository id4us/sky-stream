package com.sky.mobile.skytvstream.service.device;

import com.sky.mobile.skytvstream.domain.DeviceVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

public interface DeviceService {

    DeviceVo getDeviceVo(String deviceHeader);

    boolean isSupportDevice(DeviceVo deviceVo);

    boolean isRegistered(String clientID,
                         SubscriptionProvider subscriptionProvider, String profileId, String deviceName);
}
