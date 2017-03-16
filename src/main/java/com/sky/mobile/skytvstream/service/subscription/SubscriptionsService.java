package com.sky.mobile.skytvstream.service.subscription;

import java.util.Collection;
import java.util.Date;

import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;

public interface SubscriptionsService {

    /**
     * get the most current active subscriptions for 
     */
    Collection<SubscriptionVo> filterForActiveSubscriptions(Collection<SubscriptionVo> subscriptions);

    /**
     * returns all subscriptions for a user, emptry list if either user nor subscriptions exist.
     * Collection has is ordered to Expiry date descending (ie most recent first).
     */
    Collection<SubscriptionVo> getAllSubscriptionsForUserByProfileId(String profileId, SubscriptionProvider subscriptionProvider);


    Collection<SubscriptionProvider> activeSubscriptionByProvider(String profileId);
    
    /**
     * returns a collection of unique channels for 1 or more products.
     */
    Collection<String> getChannelsForProducts(Collection<SubscriptionVo> subscriptions);

    
    /**
     *	returns the state of a collection of subscriptions. 
     */
    SubscriptionStatus subscriptionsStatus(Collection<SubscriptionVo> subscriptions);

    Collection<SubscriptionVo> getExpiredSubscriptionsForGoogle(Collection<SubscriptionVo> subscriptions);

    SubscriptionStatus subscriptionsStatusForGoogle(Collection<SubscriptionVo> subscriptions);

    Collection<SubscriptionVo> getLastUpdatedDateforSubs(Collection<SubscriptionVo> activeSubscriptions);
}
 
