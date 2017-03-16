package com.sky.mobile.skytvstream.service.products;

import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.domain.UserPurchasableProductsVo;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

import java.util.Collection;

public interface ProductService {

    UserPurchasableProductsVo getUserPurchasableProductsFor(
            SubscriptionProvider subProvider,
            Collection<SubscriptionVo> subscriptionVoList);

    void onApplicationEvent(RefreshDataEvent event);

}
