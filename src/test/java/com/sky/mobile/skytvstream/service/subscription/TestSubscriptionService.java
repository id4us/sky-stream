package com.sky.mobile.skytvstream.service.subscription;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;

import com.sky.web.utils.Country;
import com.sky.web.utils.CountryImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext.MockTimeProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.mobile.skytvstream.utils.SubscriptionStatus;
import com.sky.mobile.skytvstream.dao.SubscriptionDao;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.web.utils.TimeProvider;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSubscriptionService.TestConfig.class, MockTimeProviderContext.class})

public class TestSubscriptionService {

	private static final String TEST_PROFILE_ID = "123";
	private static final DateTime TEST_DATE_TIME = new DateTime();
	private static final SubscriptionProvider TEST_SUBS_PROV_1 = SubscriptionProvider.GOOGLE;

	Country countryCode = new CountryImpl("GB");

	@Autowired
	SubscriptionsService service;
	
	
	@Autowired
	SubscriptionDao mockSubsDao;
	
	@Autowired
	MockTimeProvider timeProvider;

//	@Resource(name="countryCode")
//	private ArrayList countryCode;


	
	@Before
	public void setUp() throws Exception {
		timeProvider.setMockTime(TEST_DATE_TIME.getMillis());
	}


	@Test
	public void testActiveSubscriptionByProviderWithNoSubs(){
        when(mockSubsDao.getSubscriptionsByProfileId(eq(TEST_PROFILE_ID))).thenReturn(Arrays.<SubscriptionVo>asList());

		assertEquals(0,service.activeSubscriptionByProvider(TEST_PROFILE_ID).size());
	}

	@Test
	public void testActiveSubscriptionByProviderWithExpiredSubs(){
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetExpired();

		when(mockSubsDao.getSubscriptionsByProfileId(eq(TEST_PROFILE_ID))).thenReturn(subscriptionSet1);

		Collection<SubscriptionProvider> subscriptionProviders = service.activeSubscriptionByProvider(TEST_PROFILE_ID);

		assertEquals(0,subscriptionProviders.size());
	}

	@Test
	public void testActiveSubscriptionByProviderWithActiveSubs(){
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired1();

		when(mockSubsDao.getSubscriptionsByProfileId(eq(TEST_PROFILE_ID))).thenReturn(subscriptionSet1);

		Collection<SubscriptionProvider> subscriptionProviders = service.activeSubscriptionByProvider(TEST_PROFILE_ID);

		assertEquals(2,subscriptionProviders.size());
	}

	@Test
	public void testActiveSubscriptionByProviderWithInactiveSubs(){
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired2Inactive();

		when(mockSubsDao.getSubscriptionsByProfileId(eq(TEST_PROFILE_ID))).thenReturn(subscriptionSet1);

		Collection<SubscriptionProvider> subscriptionProviders = service.activeSubscriptionByProvider(TEST_PROFILE_ID);

		assertEquals(0,subscriptionProviders.size());
	}

	@Test
	public void testActiveSubscriptionByProviderWithUnexpiredAndExpiredSubs(){
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired2();

		when(mockSubsDao.getSubscriptionsByProfileId(eq(TEST_PROFILE_ID))).thenReturn(subscriptionSet1);

		Collection<SubscriptionProvider> subscriptionProviders = service.activeSubscriptionByProvider(TEST_PROFILE_ID);

		assertEquals(2,subscriptionProviders.size());
	}

	@Test
	public void testGetAllSubscriptionsForUser() {
		
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSet1();
		when(mockSubsDao.getSubscriptionsByProfileIdAndProvider(TEST_PROFILE_ID, TEST_SUBS_PROV_1)).thenReturn(subscriptionSet1);
		
		Collection<SubscriptionVo>  result= service.getAllSubscriptionsForUserByProfileId(TEST_PROFILE_ID, TEST_SUBS_PROV_1);
		
		assertEquals(4, result.size());
	}


	@Test
	public void testGetAllSubscriptionsForUser_notFound() {
		
		Collection<SubscriptionVo> subscriptionSet1 = Lists.newArrayList();
		when(mockSubsDao.getSubscriptionsByProfileIdAndProvider(TEST_PROFILE_ID, TEST_SUBS_PROV_1)).thenReturn(subscriptionSet1);
		
		Collection<SubscriptionVo>  result= service.getAllSubscriptionsForUserByProfileId(TEST_PROFILE_ID, TEST_SUBS_PROV_1);
		
		assertEquals(0, result.size());
	}

	@Test
	public void testGetActiveSubscriptionsForUser() {
		
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSet1();
		
		Collection<SubscriptionVo>  result= service.filterForActiveSubscriptions(subscriptionSet1);
		
		assertEquals(2, result.size());
		List<SubscriptionVo> listOfCollection = (List<SubscriptionVo>) subscriptionSet1;
		Iterator<SubscriptionVo> iter = result.iterator();
		assertEquals(listOfCollection.get(0), iter.next());
		assertEquals(listOfCollection.get(1), iter.next());
	}

	@Test
	public void testGetActiveSubscriptionsForUser_userNotFound() {
		Collection<SubscriptionVo> subscriptionSet1 = Lists.newArrayList();
		Collection<SubscriptionVo>  result= service.filterForActiveSubscriptions(subscriptionSet1);
		
		assertEquals(0, result.size());
	}
	
	@Test
	public void testGetActiveSubscriptionsForUser_allExpired() {
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetExpired();
		Collection<SubscriptionVo>  result= service.filterForActiveSubscriptions(subscriptionSet1);
		
		assertEquals(0, result.size());
	}

	@Test
	public void testSubscriptionsStatus_allExpired() {		
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetExpired();
		assertEquals(SubscriptionStatus.EXPIRED_SUBSCRIPTIONS, service.subscriptionsStatus(subscriptionSet1));				
	}
	
	@Test
	public void testSubscriptionsStatus_noSubs1() {		
		Collection<SubscriptionVo> subscriptionSet1 = Sets.newHashSet();
		assertEquals(SubscriptionStatus.NO_SUBSCRIPTIONS, service.subscriptionsStatus(subscriptionSet1));				
	}

	@Test
	public void testSubscriptionsStatus_unexpiredSubs1() {		
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired1();
		assertEquals(SubscriptionStatus.ACTIVE_SUBSCRIPTIONS, service.subscriptionsStatus(subscriptionSet1));				
	}

	@Test
	public void testSubscriptionsStatus_unexpiredSubs2() {		
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired2();
		assertEquals(SubscriptionStatus.ACTIVE_SUBSCRIPTIONS, service.subscriptionsStatus(subscriptionSet1));				
	}

	@Test
	public void testSubscriptionsStatus_unexpiredSubs2AndNotActivated() {
		Collection<SubscriptionVo> subscriptionSet1 = createSubscriptionSetUnExpired2Inactive();
		assertEquals(SubscriptionStatus.EXPIRED_SUBSCRIPTIONS, service.subscriptionsStatus(subscriptionSet1));
	}

	private Collection<SubscriptionVo> createSubscriptionSetUnExpired2Inactive() {
		List<SubscriptionVo> list = Lists.newArrayList();
		SubscriptionVo s = getSubscription1();
		s.setActivated(false);
		list.add(s);
		SubscriptionVo s1 = getSubscription1();
		s.setActivated(false);
		list.add(s1);
		SubscriptionVo s2 = getSubscription1();
		s.setActivated(false);
		list.add(s2);
		SubscriptionVo s3 = getSubscription1();
		s.setActivated(false);
		list.add(s3);
		Collections.sort(list, new Comparator<SubscriptionVo>() {

			@Override
			public int compare(SubscriptionVo o1, SubscriptionVo o2) {
				return o2.getExpiry().compareTo(o1.getExpiry());
			}
		});
		return list;
	}


	private Collection<SubscriptionVo> createSubscriptionSetUnExpired2() {
		List<SubscriptionVo> list = Lists.newArrayList();
		list.add(getSubscription1());
		list.add(getSubscription2());
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

	private Collection<SubscriptionVo> createSubscriptionSetUnExpired1() {
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

	private Collection<SubscriptionVo> createSubscriptionSetExpired() {
		List<SubscriptionVo> list = Lists.newArrayList();
		list.add(getSubscription1());
		list.add(getSubscription2());
		Collections.sort(list, new Comparator<SubscriptionVo>() {

			@Override
			public int compare(SubscriptionVo o1, SubscriptionVo o2) {
				return o2.getExpiry().compareTo(o1.getExpiry());
			}		
		});
		return list;
	}

	private Collection<SubscriptionVo> createSubscriptionSet1() {
		
		List<SubscriptionVo> list = Lists.newArrayList();
		list.add(getSubscription1());
		list.add(getSubscription4());
		list.add(getSubscription3());
		list.add(getSubscription2());
		Collections.sort(list, new Comparator<SubscriptionVo>() {

			@Override
			public int compare(SubscriptionVo o1, SubscriptionVo o2) {
				return o2.getExpiry().compareTo(o1.getExpiry());
			}		
		});
		return list;
	}



	private SubscriptionVo getSubscription1() {
		SubscriptionVo subscription = new SubscriptionVo();
		subscription.setId(1);
		subscription.setProfileId(TEST_PROFILE_ID);
		subscription.setProvider("VF");
		subscription.setProductId("product1");
		subscription.setActivated(true);
		subscription.setExpiry(TEST_DATE_TIME.minusDays(6).toDate());
		return subscription;
	}

	private SubscriptionVo getSubscription2() {
		SubscriptionVo subscription = new SubscriptionVo();
		subscription.setId(2);
		subscription.setProfileId(TEST_PROFILE_ID);
		subscription.setProvider("DF");
		subscription.setProductId("product2");
		subscription.setExpiry(TEST_DATE_TIME.minusMinutes(1).toDate());
		subscription.setActivated(true);
		return subscription;
	}

	private SubscriptionVo getSubscription3() {
		SubscriptionVo subscription = new SubscriptionVo();
		subscription.setId(3);
		subscription.setProfileId(TEST_PROFILE_ID);
		subscription.setProvider("EE");
		subscription.setProductId("product3");
		subscription.setExpiry(TEST_DATE_TIME.plusMinutes(1).toDate());
		subscription.setActivated(true);
		return subscription;
	}

	private SubscriptionVo getSubscription4() {
		SubscriptionVo subscription = new SubscriptionVo();
		subscription.setId(4);
		subscription.setProfileId(TEST_PROFILE_ID);
		subscription.setProvider("ON");
		subscription.setProductId("product4");
		subscription.setExpiry(TEST_DATE_TIME.plusDays(5).toDate());
		subscription.setActivated(true);
		return subscription;
	}
	
	
	@Configurable
	public static class TestConfig {
		
		@Bean
		public SubscriptionDao getSubscriptionsDao() {
			return mock(SubscriptionDao.class);
		}

		@Bean
		public SubscriptionsService getSubscriptionsService(SubscriptionDao subsDao, TimeProvider timeProvider) {
			return new SubscriptionsServiceImpl(subsDao, null, timeProvider);
		}

		@Bean(name = "countryCode")
		public Country getCountryCode() {
			return mock(Country.class);
		}
		
	}	
}
