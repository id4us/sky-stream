package com.sky.mobile.skytvstream.service.subscription;

import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

import java.util.Collection;

public interface RefreshAndroidSubscription {

    void updateAndroidSubscription(Collection<SubscriptionVo> productId,String clientId);


}
