package com.sky.mobile.skytvstream.controller;

import com.google.common.base.Optional;
import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.*;
import com.sky.mobile.skytvstream.service.device.DeviceService;
import com.sky.mobile.skytvstream.service.stream.StreamService;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.service.versions.VersionService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.ssmtv.oauth.annotations.OauthAuthenticated;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import com.sky.web.utils.HTTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.sky.mobile.skytvstream.utils.StreamingHeaders.CLIENT_ID;
import static com.sky.mobile.skytvstream.utils.StreamingHeaders.MODEL_ID;

@RestController
public class StreamController {


    private static final Logger LOG = LoggerFactory
            .getLogger(StreamController.class);


    @Resource
    private AuthenticatedPerson person;

    @Resource
    private SubscriptionsService subscriptionsService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private StreamService streamService;

    @Resource
    private VersionService versionService;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @OauthAuthenticated
    @CacheHeaders(PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID,
            StreamingHeaders.CLIENT_ID, StreamingHeaders.SUB_PROVIDER})
    @RequestMapping(value = "/createstream/channel/{channelId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object createStream(@PathVariable final String channelId,
                               final HttpServletResponse response) throws IOException {

        if (!currentRequest.containsKey(StreamingHeaders.COUNTRY_ID)){
            currentRequest.put(StreamingHeaders.COUNTRY_ID,"GB");
            LOG.info("default user country={} ",currentRequest.get(StreamingHeaders.COUNTRY_ID));
        }

       LOG.info("request uri={}, provider={}, profileId={}", "/createstream/channel/" + channelId , currentRequest
                .get(StreamingHeaders.SUB_PROVIDER) , person.getProfileId());

        final String profileId = person.getProfileId();
        final String model = currentRequest.get(StreamingHeaders.MODEL_ID);
        final DeviceVo deviceVo = deviceService.getDeviceVo(model);

        final SubscriptionProvider subscriptionProvider = SubscriptionProvider.fromName(currentRequest.get(StreamingHeaders.SUB_PROVIDER));
        final String country = currentRequest.get(StreamingHeaders.COUNTRY_ID);


        if (subscriptionProvider == null) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "9400",
                    "Invalid request missing subscription provider.");
        }

        if (country == null) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "9400",
                    "Missing country");
        }

        if (!deviceService.isSupportDevice(deviceVo)) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, "9004",
                    "Device is not supported.");
        }

        Collection<SubscriptionVo> subscriptions = subscriptionsService
                .getAllSubscriptionsForUserByProfileId(profileId, subscriptionProvider);

        SubscriptionStatus subscriptionStatus = subscriptionsService
                .subscriptionsStatus(subscriptions);

        if (subscriptionStatus != SubscriptionStatus.ACTIVE_SUBSCRIPTIONS) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED,
                    subscriptionStatus.equals(SubscriptionStatus.EXPIRED_SUBSCRIPTIONS) ?
                            "9002" : "9001",
                    subscriptionStatus.getMessage());
        }

        Collection<String> channels = subscriptionsService
                .getChannelsForProducts(subscriptionsService
                        .filterForActiveSubscriptions(subscriptions));

        if (!channels.contains(channelId)) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_PAYMENT_REQUIRED, "9003",
                    "Channel not found in the pack for customer.");
        }

        final String deviceId = currentRequest.get(CLIENT_ID);

        if (!deviceService.isRegistered(deviceId, subscriptionProvider, profileId, deviceVo.getModel())) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, "9005",
                    "Device is not allowed to use this service.");
        }

        try {
            return streamService.createStream(channelId, profileId, deviceId, model);
        } catch (Exception e) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "8538",
                    "Unknown service error occurred.");
        }
    }

    @OauthAuthenticated
    @CacheHeaders(PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID, StreamingHeaders.CLIENT_ID, StreamingHeaders.SUB_PROVIDER})
    @RequestMapping(value = "/startstream/channel/{channelId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    public Object startStream(@RequestBody StreamResponseObjectVo streamObject,
                              @PathVariable String channelId, HttpServletResponse response)
            throws IOException {

        LOG.info("request uri={}, provider={}, profileId={}", "/startstream/channel/" + channelId , currentRequest
                .get(StreamingHeaders.SUB_PROVIDER) , person.getProfileId());

        final String deviceId = currentRequest.get(CLIENT_ID);

        final String model = currentRequest.get(MODEL_ID);

        final DeviceVo deviceVo = deviceService.getDeviceVo(model);

        StreamVerificationVo streamVerificationVo =  new StreamVerificationVo(streamObject, person, channelId, deviceId, model);

        if (!streamService.isValidStreamObject(streamVerificationVo)) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, "9010", "Invalid Stream object.");
        }

        boolean isSlatedVersion = versionService.isSlatedVersion(currentRequest.get(StreamingHeaders.VERSION));

        Optional<ChannelStreamVo> channelStreamVoOptional = streamService
                .getChannelStreamingUrls(isSlatedVersion ? StreamConfig.SLATED_CHANNEL_ID : channelId);

        if (channelStreamVoOptional.isPresent()) {
            streamService.buildStreamAndCookies(streamObject, channelStreamVoOptional.get(), response, deviceId, deviceVo);
        } else {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, "9011", "Stream not available for channel.");
        }

        return uriResponse(streamObject);
    }

    private String uriResponse(StreamResponseObjectVo streamObjectVo) {
        return new StringBuilder().append("{\"uri\":\"").append(streamObjectVo.getUri()).append("\"}").toString();
    }

}
