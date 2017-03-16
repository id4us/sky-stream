package com.sky.mobile.skytvstream.controller;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.domain.UserPurchasableProductsVo;
import com.sky.mobile.skytvstream.service.products.ProductService;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.ssmtv.oauth.annotations.OauthAuthenticated;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

@RestController
public class UserProductController {
    private static final Logger LOG = LoggerFactory
            .getLogger(UserProductController.class);

    @Autowired
    private AuthenticatedPerson user;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @Autowired
    private SubscriptionsService subService;

    @Autowired
    private ProductService productService;

    @OauthAuthenticated
    @CacheHeaders(PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID,
            StreamingHeaders.CLIENT_ID, StreamingHeaders.SUB_PROVIDER})
    @RequestMapping(value = "/user/products", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public UserPurchasableProductsVo getUserProducts() {

        if (!currentRequest.containsKey(StreamingHeaders.COUNTRY_ID)){
            currentRequest.put(StreamingHeaders.COUNTRY_ID,"GB");
            LOG.info("default user country={} ",currentRequest.get(StreamingHeaders.COUNTRY_ID));
        }

        String countryCode = currentRequest.get(StreamingHeaders.COUNTRY_ID);

        LOG.info("request uri={}, provider={}, profileId={} country={}", "/user/products" , currentRequest
                .get(StreamingHeaders.SUB_PROVIDER) , user.getProfileId(), countryCode);

        SubscriptionProvider subProvider = SubscriptionProvider
                .fromName(currentRequest.get(StreamingHeaders.SUB_PROVIDER));

        Collection<SubscriptionVo> subscriptions = subService
                .getAllSubscriptionsForUserByProfileId(user.getProfileId(),
                        subProvider);

        return productService
                .getUserPurchasableProductsFor(subProvider, subscriptions);
    }

    public void setUser(AuthenticatedPerson user) {
        this.user = user;
    }

    public void setCurrentRequest(Map<StreamingHeaders, String> currentRequest) {
        this.currentRequest = currentRequest;
    }

    public void setSubService(SubscriptionsService subService) {
        this.subService = subService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

}
