package com.sky.mobile.skytvstream.dao;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.sky.mobile.skytvstream.config.TestDbConfig;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDbConfig.class, TestSubscriptionDaoImpl.TestConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = {"testdata.xml"})
public class TestSubscriptionDaoImpl {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testDatabaseIsSetUp() {
        assertEquals("Create data sql here", 7,
                jdbcTemplate.queryForInt("select count(*) from subscriptions"));
    }

    @Test
    public void testFetchSubscriptionsForProfileIdByProvider() {
        final String profileId = "123";
        final SubscriptionProvider expectedProvider = SubscriptionProvider.GOOGLE;
        List<SubscriptionVo> subscriptions = (List<SubscriptionVo>) subscriptionDao
                .getSubscriptionsByProfileIdAndProvider(profileId, expectedProvider);

        assertEquals(3, subscriptions.size());

        SubscriptionVo firstSubscription = subscriptions.get(0);
        assertEquals("c", firstSubscription.getProductId());
        assertEquals(expectedProvider.getProviderName(),
                firstSubscription.getProvider());

        assertTrue(subscriptions.get(0).isActivated());
        assertTrue(subscriptions.get(1).isActivated());
        assertFalse(subscriptions.get(2).isActivated());


        UnmodifiableIterator<SubscriptionVo> resultiter = Iterators.filter(
                subscriptions.iterator(), new Predicate<SubscriptionVo>() {
                    @Override
                    public boolean apply(SubscriptionVo arg0) {
                        boolean profile = arg0.getProfileId().equals(profileId);
                        boolean isGoogle = SubscriptionProvider.GOOGLE
                                .getProviderName().equalsIgnoreCase(
                                        arg0.getProvider());
                        return profile && isGoogle;
                    }
                });

        ArrayList<SubscriptionVo> result = Lists.newArrayList(resultiter);
        assertEquals(3, result.size());
    }


    @Test
    public void testFetchSubscriptionsForProfileId() {
        final String profileId = "123";
        List<SubscriptionVo> subscriptions = (List<SubscriptionVo>) subscriptionDao
                .getSubscriptionsByProfileId(profileId);

        assertEquals(5, subscriptions.size());
    }

    @Test
    public void testFetchSubscriptionsForProfileIdNotFound() {
        final String profileId = "123688787";
        List<SubscriptionVo> subscriptions = (List<SubscriptionVo>) subscriptionDao
                .getSubscriptionsByProfileId(profileId);

        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testFetchSubscriptionsForProfileIAndProviderNotFound() {
        final String profileId = "123456";

        List<SubscriptionVo> subscriptions = (List<SubscriptionVo>) subscriptionDao
                .getSubscriptionsByProfileIdAndProvider(profileId,
                        SubscriptionProvider.GOOGLE);

        assertEquals(0, subscriptions.size());
    }


    @Configurable
    public static class TestConfig {
        @Bean
        public SubscriptionDao getSubscriptionDao(DataSource dataSource) {
            return new SubscriptionDaoImpl(dataSource);
        }
    }

}
