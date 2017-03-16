package com.sky.mobile.skytvstream.service.secure;


import com.sky.mobile.skytvstream.domain.ExchangeTokenResponse;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthServiceException;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthenticationException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeServiceException;
import com.sky.mobile.ssmtv.oauth.oogway.OauthResponse;
import com.sky.mobile.ssmtv.oauth.oogway.OogwayConnector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class TestTokenExchangeService {

    private TokenExchangeService tokenExchangeService;
    private OogwayConnector oogwayConnector;
    private SubscriptionsService subscriptionsService;
    private OauthTokenTranslator oauthTokenTranslator;

    @Before
    public void setUp(){
        oogwayConnector = mock(OogwayConnector.class);
        subscriptionsService = mock(SubscriptionsService.class);
        oauthTokenTranslator = mock(OauthTokenTranslator.class);
        tokenExchangeService = new TokenExchangeServiceImpl(oogwayConnector, subscriptionsService, oauthTokenTranslator);
    }

    @Test
    public void successfulExchangeWithActiveSubs() throws TokenExchangeException, TokenExchangeServiceException, AuthenticationException, AuthServiceException, IOException, GeneralSecurityException {
        String code = "myCode";
        String clientId ="myClientId";
        String originIp ="myClientId";
        String oogwayOauthToken = "oauthToken";
        String someEncryptedValue = "anyThing";
        OauthResponse oauthResponse = new OauthResponse("{\"details\":{\"username\":\"user\",\t\"email\":\"emailA\"}, \"id\":{\"profileid\":\"profileIdA\"}}");
        List<SubscriptionProvider> activeSubscriptionProviders = Arrays.asList(SubscriptionProvider.APPLE);

        when(oogwayConnector.exchangeCodeForOauthToken(eq(code),eq(originIp))).thenReturn(oogwayOauthToken);
        when(oogwayConnector.validateOauthToken(eq(oogwayOauthToken), eq(originIp))).thenReturn(oauthResponse);
        when(subscriptionsService.activeSubscriptionByProvider(eq(oauthResponse.getProfileId()))).thenReturn(activeSubscriptionProviders);
        when(oauthTokenTranslator.oauthEncrypt(eq(oogwayOauthToken), eq(clientId))).thenReturn(someEncryptedValue);

        ExchangeTokenResponse exchangeTokenResponse = tokenExchangeService.exchangeCode(code, clientId, originIp);

        assertEquals(oauthResponse.getEmail(), exchangeTokenResponse.getEmail());
        assertEquals(someEncryptedValue, exchangeTokenResponse.getOauthToken());
        assertEquals("ACTIVE", exchangeTokenResponse.getAppleSubscriptionStatus());
        assertEquals("INACTIVE", exchangeTokenResponse.getVodafoneSubscriptionStatus());
        assertEquals("INACTIVE", exchangeTokenResponse.getGoogleSubscriptionStatus());
    }

    @Test
    public void successfulExchangeWithNoSubs() throws TokenExchangeException, TokenExchangeServiceException, AuthenticationException, AuthServiceException, IOException, GeneralSecurityException {
        String code = "myCode";
        String clientId ="myClientId";
        String originIp ="myClientId";
        String oogwayOauthToken = "oauthToken";
        String someEncryptedValue = "anyThing";
        OauthResponse oauthResponse = new OauthResponse("{\"details\":{\"username\":\"user\",\t\"email\":\"emailA\"}, \"id\":{\"profileid\":\"profileIdA\"}}");
        List<SubscriptionProvider> activeSubscriptionProviders = Arrays.asList();

        when(oogwayConnector.exchangeCodeForOauthToken(eq(code),eq(originIp))).thenReturn(oogwayOauthToken);
        when(oogwayConnector.validateOauthToken(eq(oogwayOauthToken), eq(originIp))).thenReturn(oauthResponse);
        when(subscriptionsService.activeSubscriptionByProvider(eq(oauthResponse.getProfileId()))).thenReturn(activeSubscriptionProviders);
        when(oauthTokenTranslator.oauthEncrypt(eq(oogwayOauthToken), eq(clientId))).thenReturn(someEncryptedValue);

        ExchangeTokenResponse exchangeTokenResponse = tokenExchangeService.exchangeCode(code, clientId, originIp);

        assertEquals(oauthResponse.getEmail(), exchangeTokenResponse.getEmail());
        assertEquals(someEncryptedValue, exchangeTokenResponse.getOauthToken());
        assertEquals("INACTIVE", exchangeTokenResponse.getAppleSubscriptionStatus());
        assertEquals("INACTIVE", exchangeTokenResponse.getVodafoneSubscriptionStatus());
        assertEquals("INACTIVE", exchangeTokenResponse.getGoogleSubscriptionStatus());
    }

    @Test(expected = TokenExchangeException.class)
    public void exchangeWithExceptionsPropagates() throws TokenExchangeException, TokenExchangeServiceException, AuthenticationException, AuthServiceException, IOException, GeneralSecurityException {
        when(oogwayConnector.exchangeCodeForOauthToken(anyString(),anyString())).thenThrow(new TokenExchangeException("something"));
        tokenExchangeService.exchangeCode("code", "clientId", "originIp");
    }

}
