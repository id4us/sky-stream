package com.sky.mobile.skytvstream.service.secure;

import com.sky.mobile.skytvstream.domain.ExchangeTokenResponse;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthServiceException;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthenticationException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeServiceException;
import com.sky.mobile.ssmtv.oauth.oogway.OauthResponse;
import com.sky.mobile.ssmtv.oauth.oogway.OogwayConnector;
import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@Service
public class TokenExchangeServiceImpl implements TokenExchangeService {

    private OogwayConnector oogwayConnector;
    private SubscriptionsService subscriptionsService;
    private OauthTokenTranslator oauthTokenTranslator;


    @Autowired
    public TokenExchangeServiceImpl(OogwayConnector oogwayConnector, SubscriptionsService subscriptionsService, OauthTokenTranslator oauthTokenTranslator){
        this.oauthTokenTranslator = oauthTokenTranslator;
        this.oogwayConnector= oogwayConnector;
        this.subscriptionsService = subscriptionsService;
    }

    @Override
    public ExchangeTokenResponse exchangeCode(String code, String clientId, String originIp) throws TokenExchangeException, TokenExchangeServiceException, AuthenticationException, AuthServiceException, GeneralSecurityException, UnsupportedEncodingException {
        String oauthToken = oogwayConnector.exchangeCodeForOauthToken(code, originIp);
        OauthResponse oauthResponse = oogwayConnector.validateOauthToken(oauthToken, originIp);
        String oauthProxy = oauthTokenTranslator.oauthEncrypt(oauthToken, clientId);

        ExchangeTokenResponse response = new ExchangeTokenResponse();
        response.setEmail(oauthResponse.getEmail());
        response.setOauthToken(oauthProxy);
        response.setSubscriptionStatus(subscriptionsService.activeSubscriptionByProvider(oauthResponse.getProfileId()));
        return response;
    }
}

