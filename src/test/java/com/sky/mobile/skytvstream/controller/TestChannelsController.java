package com.sky.mobile.skytvstream.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.service.subscription.RefreshAndroidSubscription;
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext.MockTimeProvider;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import com.sky.web.utils.Country;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestChannelsController.TestConfig.class,
        MockTimeProviderContext.class})
public class TestChannelsController {

    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";
    private static final String HTTP_HEADER_COUNTRY = "x-country";
    private static final String EXPECTED = "{\"channelList\":[\"3\",\"2\",\"1\"]}";
    private static final String EXPECTED_ROI = "{\"channelList\":[\"3\",\"5\",\"1\"]}";
    private static final DateTime TEST_DATE = new DateTime();
    private static final String TEST_PROFILE_ID = "123";


    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Resource(name = "allChannelsService")
    private StaticFileService allChannelsService;

    @Resource(name = "allChannelsServiceRoi")
    private StaticFileService allChannelsServiceRoi;

    @Resource(name="countryCode")
    private Country countryCode;


    @Resource
    private SubscriptionsService subscriptionsService;

    @Autowired
    private MockTimeProvider timeProvider;

    @Autowired
    private RefreshAndroidSubscription refreshAndroidSubscription;


    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        timeProvider.setMockTime(TEST_DATE.getMillis());
    }

    @DirtiesContext
    @Test
    public void testGetChannelsForUser() throws Exception {

        Collection<SubscriptionVo> subscriptions = createSubscriptions();

        Set<String> channelIds = Sets.newHashSet("1", "2", "3");

        when(
                subscriptionsService.getAllSubscriptionsForUserByProfileId(
                        TEST_PROFILE_ID, SubscriptionProvider.APPLE))
                .thenReturn(subscriptions);
        when(
                subscriptionsService.subscriptionsStatus(subscriptions))
                .thenReturn(SubscriptionStatus.ACTIVE_SUBSCRIPTIONS);
        when(subscriptionsService.filterForActiveSubscriptions(subscriptions))
                .thenReturn(subscriptions);

        when(subscriptionsService.getChannelsForProducts(subscriptions))
                .thenReturn(channelIds);

        MvcResult result =  this.mockMvc
                .perform(
                        get("/user/channels")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE).header(HTTP_HEADER_COUNTRY,"GB"))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(HTTP_HEADER_NAME_CONTENT_TYPE,
                                is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))).andReturn();
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("\"1\"") && content.contains("\"3\"") && content.contains("\"2\""));

    }

    @Test
    @DirtiesContext
    public void testGetAllChannels() throws Exception {
        when(allChannelsService.getContent()).thenReturn(EXPECTED_ROI);
        when(countryCode.getCountry()).thenReturn("ROI");


        MvcResult result = this.mockMvc
                .perform(
                        get("/all/channels")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE).header(HTTP_HEADER_COUNTRY,"ROI"))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(HTTP_HEADER_NAME_CONTENT_TYPE,
                                is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))).andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("\"5\"") && content.contains("\"3\"") && content.contains("\"1\""));
    }

   private Collection<SubscriptionVo> createSubscriptions() {
        List<SubscriptionVo> list = Lists.newArrayList();
        list.add(getSubscription3());
        list.add(getSubscription4());
        Collections.sort(list, new Comparator<SubscriptionVo>() {

            @Override
            public int compare(SubscriptionVo o1, SubscriptionVo o2) {
                return o2.getExpiry().compareTo(o1.getExpiry());
            }
        });
        return list;
    }

    private SubscriptionVo getSubscription3() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setId(3);
        subscription.setProfileId(TEST_PROFILE_ID);
        subscription.setProvider("EE");
        subscription.setProductId("product3");
        subscription.setExpiry(TEST_DATE.plusMinutes(1).toDate());
        return subscription;
    }

    private SubscriptionVo getSubscription4() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setId(4);
        subscription.setProfileId(TEST_PROFILE_ID);
        subscription.setProvider("ON");
        subscription.setProductId("product4");
        subscription.setExpiry(TEST_DATE.plusDays(5).toDate());
        return subscription;
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfig extends WebMvcConfigurerAdapter {


        @Bean
        public AuthenticatedPerson getPerson() {
            AuthenticatedPerson person = new AuthenticatedPerson();
            person.setProfileId(TEST_PROFILE_ID);
            return person;
        }

        @Bean(name = "currentRequest")
        @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
        public Map<StreamingHeaders, String> getCurrentRequest() {
            HashMap<StreamingHeaders, String> request = new HashMap<>();
            request.put(StreamingHeaders.SUB_PROVIDER, "apple");
            request.put(StreamingHeaders.COUNTRY_ID, "GB" );
            return request;
        }

        @Bean
        public ChannelsForUserController getController() {
            return new ChannelsForUserController();
        }

        @Bean
        public SubscriptionsService getSubscriptionService() {
            return mock(SubscriptionsService.class);
        }

        @Bean
        public RefreshAndroidSubscription getRefreshAndroidSubscription() {
            return mock(RefreshAndroidSubscription.class);
        }

        @Bean(name = "allChannelsService")
        public StaticFileService getAllChannelsService() {
            return mock(StaticFileService.class);
        }

        @Bean(name = "allChannelsServiceRoi")
        public StaticFileService getAllChannelsServiceRoi() {
            return mock(StaticFileService.class);
        }

        @Bean(name = "countryCode")
        public Country getCountryCode() {
            return mock(Country.class);
        }

    }
}
