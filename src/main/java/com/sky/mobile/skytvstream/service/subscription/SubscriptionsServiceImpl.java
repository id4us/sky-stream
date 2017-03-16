package com.sky.mobile.skytvstream.service.subscription;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.dao.ProductsDao;
import com.sky.mobile.skytvstream.dao.SubscriptionDao;
import com.sky.mobile.skytvstream.domain.ChannelVo;
import com.sky.mobile.skytvstream.domain.ProductVo;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.web.utils.TimeProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubscriptionsServiceImpl implements SubscriptionsService {

    private SubscriptionDao subsDao;

    private ProductsDao productsDao;

    private TimeProvider timeProvider;

    //This is a property to check when the Google subscription was last updated.
    @Value("${google.lastUpdated.check}")
    private String lastUpdatedCheckForGoogle;

    @Value("${google.process.for.last.months}")
    private String processGoogleForLastXMonths;

    @Autowired
    public SubscriptionsServiceImpl(SubscriptionDao subsDao, ProductsDao productsDao, TimeProvider timeProvider) {
        this.subsDao = subsDao;
        this.productsDao = productsDao;
        this.timeProvider = timeProvider;

    }

    @Override
    public Collection<SubscriptionProvider> activeSubscriptionByProvider(String profileId) {

        List<SubscriptionProvider> activeSubscriptionStatusList = new ArrayList<>();
        Collection<SubscriptionVo> subscriptions = subsDao.getSubscriptionsByProfileId(profileId);
        for (SubscriptionVo subscriptionVo : subscriptions) {
            if (subscriptionVo.getExpiry().after(timeProvider.getDate()) && subscriptionVo.isActivated()) {
                activeSubscriptionStatusList.add(SubscriptionProvider.fromName(subscriptionVo.getProvider()));
            }
        }
        return activeSubscriptionStatusList;
    }

    @Override
    public Collection<SubscriptionVo> getAllSubscriptionsForUserByProfileId(String profileId, SubscriptionProvider subscriptionProvider) {
        return subsDao.getSubscriptionsByProfileIdAndProvider(profileId, subscriptionProvider);
    }

    @Override
    public Collection<SubscriptionVo> filterForActiveSubscriptions(Collection<SubscriptionVo> subscriptions) {
        final DateTime now = timeProvider.getDateTime();
        List<SubscriptionVo> filtered = Lists.newArrayList
                (Iterators.filter(
                        subscriptions.iterator(), (input) -> new DateTime(input.getExpiry()).isAfter(now)));
        return filtered;
    }

    @Override
    public Collection<String> getChannelsForProducts(Collection<SubscriptionVo> subscriptions) {
        Set<String> channels = Sets.newHashSet();

        for (SubscriptionVo vo : subscriptions) {
            Optional<ProductVo> result = productsDao.getProductsByName(vo.getProductId());
            if (result.isPresent()) {
                for (ChannelVo channel : result.get().getChannels()) {
                    channels.add(channel.getChannelid());
                }
            }
        }
        return channels;
    }

    @Override
    public SubscriptionStatus subscriptionsStatus(Collection<SubscriptionVo> subscriptions) {
        Preconditions.checkNotNull(subscriptions, "Subscriptions Parameter should not be null");

        DateTime now = timeProvider.getDateTime();

        if (subscriptions.isEmpty()) {
            return SubscriptionStatus.NO_SUBSCRIPTIONS;
        } else {
            for (SubscriptionVo sub : subscriptions) {
                DateTime expiryDate = new DateTime(sub.getExpiry());
                if (now.isBefore(expiryDate) && sub.isActivated()) {
                    return SubscriptionStatus.ACTIVE_SUBSCRIPTIONS;
                }
            }
        }
        return SubscriptionStatus.EXPIRED_SUBSCRIPTIONS;
    }


    @Override
    public SubscriptionStatus subscriptionsStatusForGoogle(Collection<SubscriptionVo> subscriptions) {
        Preconditions.checkNotNull(subscriptions, "Subscriptions Parameter should not be null");

        DateTime now = timeProvider.getDateTime();

        if (subscriptions.isEmpty()) {
            return SubscriptionStatus.NO_SUBSCRIPTIONS;
        } else {
            for (SubscriptionVo sub : subscriptions) {
                DateTime expiryDate = new DateTime(sub.getExpiry());
                if (now.isAfter(expiryDate)) {
                    return SubscriptionStatus.EXPIRED_SUBSCRIPTIONS;
                }
            }
        }
        return SubscriptionStatus.ACTIVE_SUBSCRIPTIONS;
    }


    @Override
    public Collection<SubscriptionVo> getExpiredSubscriptionsForGoogle(Collection<SubscriptionVo> subscriptions) {
        Preconditions.checkNotNull(subscriptions, "Subscriptions Parameter should not be null");

        DateTime now = timeProvider.getDateTime();
        Collection<SubscriptionVo> subscriptionsToBeUpdated = new ArrayList<SubscriptionVo>();
        for (SubscriptionVo sub : subscriptions) {
            DateTime expiryDate = new DateTime(sub.getExpiry());
            if (!expiryDate.isBefore(now.minusMonths(Integer.parseInt(processGoogleForLastXMonths)))) {
                subscriptionsToBeUpdated.add(sub);
            }
        }
        return subscriptionsToBeUpdated;
    }


    @Override
    public Collection<SubscriptionVo> getLastUpdatedDateforSubs(Collection<SubscriptionVo> subscriptions) {
        Preconditions.checkNotNull(subscriptions, "Subscriptions Parameter should not be null");

        final DateTime now = timeProvider.getDateTime();
        Collection<SubscriptionVo> subscriptionsToBeUpdated = new ArrayList<SubscriptionVo>();

        Date lastUpdated;
        for (SubscriptionVo sub : subscriptions) {
            lastUpdated = subsDao.getLastUpdated(sub);
            DateTime dateTime = new DateTime(lastUpdated);

            if (!dateTime.isAfter(now.minusMinutes(Integer.parseInt(lastUpdatedCheckForGoogle)))) {
                subscriptionsToBeUpdated.add(sub);
            }
        }
        return subscriptionsToBeUpdated;
    }
}