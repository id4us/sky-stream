package com.sky.mobile.skytvstream.controller;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.domain.ChannelListVo;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.service.subscription.RefreshAndroidSubscription;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.ssmtv.oauth.annotations.OauthAuthenticated;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import com.sky.web.utils.HTTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@RestController
public class ChannelsForUserController {

    private static final Logger LOG = LoggerFactory
            .getLogger(ChannelsForUserController.class);

    @Autowired
    private AuthenticatedPerson user;

    @Autowired
    private RefreshAndroidSubscription refreshAndroidSubscription;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @Autowired
    private SubscriptionsService subService;

    @Resource(name = "allChannelsService")
    private StaticFileService allChannelsService;

    @Resource(name = "allChannelsServiceRoi")
    private StaticFileService allChannelsServiceRoi;


    @CacheHeaders(value = PageCacheStrategy.CACHED, cacheMinuites = 6000)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID,
            StreamingHeaders.CLIENT_ID})
    @RequestMapping(value = "/all/channels", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getAllChannels() {

        if (!currentRequest.containsKey(StreamingHeaders.COUNTRY_ID)) {
            currentRequest.put(StreamingHeaders.COUNTRY_ID, "GB");
            LOG.info("default user country={} ", currentRequest.get(StreamingHeaders.COUNTRY_ID));
        }
        String countryCode = currentRequest.get(StreamingHeaders.COUNTRY_ID);


        if (countryCode.equals("GB")) {
            LOG.debug("user country={} ", countryCode);
            return allChannelsService.getContent();

        } else if (countryCode.equals("IE")) {
            LOG.debug("user country={} ", countryCode);
            return allChannelsServiceRoi.getContent();
        }
        return allChannelsService.getContent();

    }

    @OauthAuthenticated
    @CacheHeaders(PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID,
            StreamingHeaders.CLIENT_ID, StreamingHeaders.SUB_PROVIDER})
    @RequestMapping(value = "/user/channels", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object getChannelsForUser(HttpServletResponse response)
            throws IOException {
        // TODO: We can cache this for 2 hours, as long as it can be purged on the event of a purchase.

        if (!currentRequest.containsKey(StreamingHeaders.COUNTRY_ID)) {
            currentRequest.put(StreamingHeaders.COUNTRY_ID, "GB");
        }

        String clientId = currentRequest.get(StreamingHeaders.CLIENT_ID);

        String countryCode = currentRequest.get(StreamingHeaders.COUNTRY_ID);
        LOG.info("request uri={}, provider={}, profileId={}, country={}", "/user/channels", currentRequest.get(StreamingHeaders.SUB_PROVIDER), user.getProfileId(), countryCode);

        Collection<SubscriptionVo> subscriptions = subService
                .getAllSubscriptionsForUserByProfileId(user.getProfileId(),
                        SubscriptionProvider.fromName(currentRequest
                                .get(StreamingHeaders.SUB_PROVIDER)));

        SubscriptionStatus subscriptionStatus = subService
                .subscriptionsStatus(subscriptions);

        Collection<SubscriptionVo> activeSubscriptions = subService
                .filterForActiveSubscriptions(subscriptions);

        if (currentRequest.get(StreamingHeaders.SUB_PROVIDER).equals("GOOGLE")) {
            subscriptionStatus = subService.subscriptionsStatusForGoogle(subscriptions);
        }


        if (subscriptionStatus.equals(SubscriptionStatus.EXPIRED_SUBSCRIPTIONS)) {
            if (currentRequest.get(StreamingHeaders.SUB_PROVIDER).equalsIgnoreCase("GOOGLE")) {
                Collection<SubscriptionVo> expiredSubs = subService.getExpiredSubscriptionsForGoogle(subscriptions);
                Collection<SubscriptionVo> lastUpdatedSubs = subService.getLastUpdatedDateforSubs(expiredSubs);
                if (!lastUpdatedSubs.isEmpty()) {
                    refreshAndroidSubscription.updateAndroidSubscription(lastUpdatedSubs, clientId);

                    Collection<SubscriptionVo> subscriptionsUpdated = subService
                            .getAllSubscriptionsForUserByProfileId(user.getProfileId(),
                                    SubscriptionProvider.fromName(currentRequest
                                            .get(StreamingHeaders.SUB_PROVIDER)));

                    activeSubscriptions = subService
                            .filterForActiveSubscriptions(subscriptionsUpdated);

                    if(activeSubscriptions.size()<=0){
                        return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED, "9002",
                                SubscriptionStatus.EXPIRED_SUBSCRIPTIONS.getMessage());
                    }
                }
                else{
                    return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED, "9002",
                            SubscriptionStatus.EXPIRED_SUBSCRIPTIONS.getMessage());

                }

            } else {
                return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED, "9002",
                        SubscriptionStatus.EXPIRED_SUBSCRIPTIONS.getMessage());
            }
        } else if (subscriptionStatus
                .equals(SubscriptionStatus.NO_SUBSCRIPTIONS)) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED, "9001",
                    SubscriptionStatus.NO_SUBSCRIPTIONS.getMessage());
        }

        Collection<String> channels = subService
                .getChannelsForProducts(activeSubscriptions);

        if (channels.isEmpty()) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, "9003",
                    "No channels available for subscription.");
        }

        return ChannelListVo.from(channels);

    }
}
