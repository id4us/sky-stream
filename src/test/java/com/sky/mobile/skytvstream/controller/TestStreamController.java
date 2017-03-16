package com.sky.mobile.skytvstream.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.sky.mobile.skytvstream.config.TestDbConfig;
import com.sky.mobile.skytvstream.domain.*;
import com.sky.mobile.skytvstream.interceptor.HeadersInterceptor;
import com.sky.mobile.skytvstream.service.device.DeviceService;
import com.sky.mobile.skytvstream.service.stream.StreamService;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.service.versions.VersionService;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestDbConfig.class,
        TestStreamController.TestConfig.class, MockTimeProviderContext.class})
public class TestStreamController {

    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_NAME_AUTH = "Authorization";
    private static final String HTTP_HEADER_NAME_SUBS = "x-subscription-provider";
    private static final String HTTP_HEADER_NAME_VERSION = "x-version";
    private static final String HTTP_HEADER_NAME_MODEL = "x-model-identifier";
    private static final String HTTP_HEADER_NAME_CLIENTID = "x-client-id";
    private static final String HTTP_HEADER_COUNTRY = "x-country";
    private static final String OAUTH = "OAUTH";

    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";

    @Autowired
    private WebApplicationContext wac;

    @Resource
    private DeviceService deviceService;

    @Resource
    private StreamService streamService;

    @Resource
    private SubscriptionsService subscriptionsService;


    @Resource
    private AuthenticatedPerson authenticatedPerson;

    private MockMvc mockMvc;

    @Resource
    private DataSource datasource;
    private JdbcTemplate jdbctemplate;

    @Before
    public void setUp() throws Exception {
        this.jdbctemplate = new JdbcTemplate(datasource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        jdbctemplate.execute("delete from registered_devices");
    }

    @Test
    @DirtiesContext
    public void testCreateStreamFailsWithNoHeaders() throws Exception {
        this.mockMvc.perform(
                get("/createstream/channel/1301").header(
                        HTTP_HEADER_NAME_ACCEPT,
                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE).header(
                        HTTP_HEADER_NAME_CONTENT_TYPE,
                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)).andExpect(
                status().isBadRequest());
    }


    @Test
    @DirtiesContext
    public void testCreateStreamNotBlacklisted() throws Exception {
        MvcResult resultSet = this.mockMvc
                .perform(
                        get("/createstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer Not Needed")
                                .header(HTTP_HEADER_NAME_SUBS, "goose")
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, "IOS;3546")
                                .header(HTTP_HEADER_NAME_CLIENTID, "clientId"))
//                                .header(HTTP_HEADER_COUNTRY, "GB"))
                .andExpect(status().isBadRequest()).andReturn();
        String content = resultSet.getResponse().getContentAsString();
        assertEquals("{\"code\":\"9400\",\"message\":\"Invalid request missing subscription provider.\"}", content);
    }

    // startStreamTest

    @Test
    @DirtiesContext
    public void createStream_happyPath() throws Exception {
        String profileId = "5001";
        String clientId = "clientId";
        String channelId = "1301";
        String type = "GOOGLE";
        String model = "HTC";
        String country = "ROI";

        SubscriptionProvider subscriptionProvider = SubscriptionProvider.GOOGLE;

        DeviceVo deviceVo = new DeviceVo(type, model);

        when(deviceService.getDeviceVo("GOOGLE;HTC")).thenReturn(deviceVo);
        when(deviceService.isSupportDevice(deviceVo)).thenReturn(true);
        when(
                deviceService.isRegistered(clientId,
                        SubscriptionProvider.GOOGLE, profileId, model))
                .thenReturn(true);

        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setExpiry(new DateTime().plusDays(1).toDate());
        subscription.setId(1);
        subscription.setProfileId(profileId);
        subscription.setProvider(subscriptionProvider.toString());

        Collection<SubscriptionVo> subscriptions = Lists
                .newArrayList(subscription);

        when(subscriptionsService.getAllSubscriptionsForUserByProfileId(
                profileId, SubscriptionProvider.GOOGLE)).thenReturn(
                subscriptions);
        when(subscriptionsService.getChannelsForProducts(subscriptions))
                .thenReturn(Lists.newArrayList("1301"));
        when(subscriptionsService.subscriptionsStatus(subscriptions))
                .thenReturn(SubscriptionStatus.ACTIVE_SUBSCRIPTIONS);
        when(subscriptionsService.filterForActiveSubscriptions(subscriptions))
                .thenReturn(subscriptions);

        StreamResponseObjectVo streamObjectVo = new StreamResponseObjectVo();
        streamObjectVo.setNextToken("SOME TOKEN");
        streamObjectVo.setNonce("SOME NONCE");
        streamObjectVo.setStreamId("12");

        when(streamService.createStream(channelId, profileId, clientId, "GOOGLE;HTC"))
                .thenReturn(streamObjectVo);

        // Do the create stream call

        MvcResult createResultSet = this.mockMvc
                .perform(
                        get("/createstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer Not Needed")
                                .header(HTTP_HEADER_NAME_SUBS,
                                        subscriptionProvider.toString())
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, "GOOGLE;HTC")
                                .header(HTTP_HEADER_NAME_CLIENTID, clientId))
      //                          .header(HTTP_HEADER_COUNTRY, country))
                .andExpect(status().isOk()).andReturn();
        String content = createResultSet.getResponse().getContentAsString();

        System.out.println("content:" + content);

        with(content).assertEquals("$.nonce", "SOME NONCE")
                .assertEquals("$.streamId", "12")
                .assertEquals("uri", "http://skydvn-ssmtv-mobile-prod.mobile-tv.sky.com/ssmtv-skysports8/1371/ss8_sstv_hd.m3u8")
                .assertEquals("$.nextToken", "SOME TOKEN")
                .assertNull("clientChallengeResponse");
    }

    // Bug Test
    @Test
    @DirtiesContext
    public void testCreateStreamFailsWhenUserHasNoActiveSubscriptions() throws Exception {
        String profileId = "5001";
        String clientId = "clientId";
        String channelId = "1301";
        String type = "IOS";
        String model = "iphone";

        SubscriptionProvider subscriptionProvider = SubscriptionProvider.APPLE;

        DeviceVo deviceVo = new DeviceVo(type, model);

        when(deviceService.getDeviceVo("IOS;iphone")).thenReturn(deviceVo);
        when(deviceService.isSupportDevice(deviceVo)).thenReturn(true);
        when(deviceService.isRegistered(clientId,
                        SubscriptionProvider.APPLE, profileId, model))
                .thenReturn(true);

        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setExpiry(new DateTime().plusDays(1).toDate());
        subscription.setId(1);
        subscription.setProfileId(profileId);
        subscription.setProvider(subscriptionProvider.toString());

        Collection<SubscriptionVo> subscriptions = Lists
                .newArrayList(subscription);

        when(subscriptionsService.getAllSubscriptionsForUserByProfileId(
                profileId, SubscriptionProvider.APPLE)).thenReturn(
                subscriptions);
        when(subscriptionsService.getChannelsForProducts(subscriptions))
                .thenReturn(Lists.newArrayList("1301"));
        when(subscriptionsService.subscriptionsStatus(subscriptions))
                .thenReturn(SubscriptionStatus.NO_SUBSCRIPTIONS);
        when(subscriptionsService.filterForActiveSubscriptions(subscriptions))
                .thenReturn(subscriptions);

        StreamResponseObjectVo streamObjectVo = new StreamResponseObjectVo();
        streamObjectVo.setNextToken("SOME TOKEN");
        streamObjectVo.setNonce("SOME NONCE");
        streamObjectVo.setStreamId("12");

        when(streamService.createStream(channelId, profileId, clientId, "IOS;iphone"))
                .thenReturn(streamObjectVo);

        MvcResult createResultSet = this.mockMvc
                .perform(
                        get("/createstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer Not Needed")
                                .header(HTTP_HEADER_NAME_SUBS,
                                        subscriptionProvider.toString())
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, "IOS;iphone")
                                .header(HTTP_HEADER_NAME_CLIENTID, clientId)
                                .header(HTTP_HEADER_COUNTRY, "GB"))
                .andExpect(status().isPaymentRequired()).andReturn();
        String content = createResultSet.getResponse().getContentAsString();
        assertEquals("{\"code\":\"9001\",\"message\":\"no subscriptions found\"}", content);
    }

    @Test
    @DirtiesContext
    public void startStream_noBody_badBody() throws Exception {
        this.mockMvc
                .perform(
                        post("/startstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer Not Needed")
                                .header(HTTP_HEADER_NAME_SUBS,
                                        SubscriptionProvider.APPLE.toString())
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, "IOS;iphone")
                                .header(HTTP_HEADER_NAME_CLIENTID, "clientId"))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    @DirtiesContext
    public void startStream_badBody() throws Exception {

        String clientId = "clientId";
        String channelId = "1301";

        SubscriptionProvider subscriptionProvider = SubscriptionProvider.APPLE;


        StreamResponseObjectVo streamObjectVo = new StreamResponseObjectVo();
        streamObjectVo.setNextToken("SOME TOKEN");
        streamObjectVo.setNonce("SOME NONCE");
        streamObjectVo.setStreamId("12");
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(streamObjectVo);

        // Do the create stream call

        this.mockMvc
                .perform(
                        post("/startstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer Not Needed")
                                .header(HTTP_HEADER_NAME_SUBS,
                                        subscriptionProvider.toString())
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, "IOS;iphone")
                                .header(HTTP_HEADER_NAME_CLIENTID, clientId)
                                .content(json))
                .andExpect(status().isBadRequest()).andReturn();

    }

    @Test
    @DirtiesContext
    public void startStream_happyPath() throws Exception {

        String clientId = "clientId";
        String channelId = "1301";
        String model = "IOS;iphone";

        SubscriptionProvider subscriptionProvider = SubscriptionProvider.APPLE;

        StreamResponseObjectVo streamObjectVo = new StreamResponseObjectVo();
        streamObjectVo.setNextToken("SOME TOKEN");
        streamObjectVo.setNonce("SOME NONCE");
        streamObjectVo.setStreamId("12");
        streamObjectVo.setClientChallengeResponse("IMACHALLEGEN");

        StreamVerificationVo streamVerificationVo = new StreamVerificationVo(streamObjectVo, authenticatedPerson, channelId, clientId, model);

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(streamObjectVo);

        String proxyOAuth = OAUTH;

        ArgumentCaptor<StreamVerificationVo> argument = ArgumentCaptor
                .forClass(StreamVerificationVo.class);


        when(streamService.isValidStreamObject(argument.capture()))
                .thenReturn(true);

        when(streamService.getChannelStreamingUrls(channelId))
                .thenReturn(createChannelStreamVo1(channelId));

        MvcResult createResultSet = this.mockMvc
                .perform(
                        post("/startstream/channel/1301")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTH,
                                        "Bearer " + proxyOAuth)
                                .header(HTTP_HEADER_NAME_SUBS,
                                        subscriptionProvider.toString())
                                .header(HTTP_HEADER_NAME_VERSION, "v1_0")
                                .header(HTTP_HEADER_NAME_MODEL, model)
                                .header(HTTP_HEADER_NAME_CLIENTID, clientId)
                                .content(json)).andExpect(status().isOk())
                .andReturn();



        assertEquals(streamVerificationVo, argument.getValue());

        String content = createResultSet.getResponse().getContentAsString();
        System.out.println("content:" + content);
        assertEquals("{\"uri\":\"http://skydvn-ssmtv-mobile-prod.mobile-tv.sky.com/ssmtv-skysports8/1371/ss8_sstv_hd.m3u8\"}", content);
    }

    private Optional<ChannelStreamVo> createChannelStreamVo1(String channelId) {

        ChannelStreamVo channelVo = new ChannelStreamVo();
        channelVo.setChannelId(channelId);
        channelVo.setKeyPath("/keypath/");
        channelVo.setPlaylist("/1234.m8pu");
        channelVo.setPlaylistPath("/cn/cn");
        return Optional.of(channelVo);
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfig extends WebMvcConfigurerAdapter {

        @Bean(name = "currentRequest")
        @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
        public Map<StreamingHeaders, String> getCurrentRequest() {
            return new HashMap<>();
        }

        @Bean
        public AuthenticatedPerson getAuthenticatedPerson() {
            AuthenticatedPerson person = new AuthenticatedPerson();
            person.setProfileId("5001");
            person.setEncryptedOauthToken(OAUTH);
            return person;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(getHeadersInterceptor()).addPathPatterns(
                    "/**");
        }

        @Bean
        public HeadersInterceptor getHeadersInterceptor() {
            return new HeadersInterceptor();
        }

        @Bean
        public VersionService getVersionService() {
            VersionService service = mock(VersionService.class);
            when(service.isAllowedVersion(anyString())).thenReturn(true);
            return service;
        }

        @Bean
        public DeviceService getDeviceServiceMock() {
            return mock(DeviceService.class);
        }

        @Bean
        public SubscriptionsService getSubsServiceMock() {
            return mock(SubscriptionsService.class);
        }

        @Bean
        public StreamService getStreamService() {
            return mock(StreamService.class);
        }


        @Bean
        public StreamController getController() {
            return new StreamController();
        }
    }

}
