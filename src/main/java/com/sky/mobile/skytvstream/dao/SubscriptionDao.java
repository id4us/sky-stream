package com.sky.mobile.skytvstream.dao;

import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;

public interface SubscriptionDao {
	Collection<SubscriptionVo> getSubscriptionsByProfileId (String profileId);

	Collection<SubscriptionVo> getSubscriptionsByProfileIdAndProvider(String profileId, SubscriptionProvider subscriptionProvider);

	Date  getLastUpdated(SubscriptionVo sub);
}
