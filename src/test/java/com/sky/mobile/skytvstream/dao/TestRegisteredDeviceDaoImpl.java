package com.sky.mobile.skytvstream.dao;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.sky.mobile.skytvstream.config.TestDbConfig;
import com.sky.mobile.skytvstream.domain.RegisteredDevice;
import com.sky.mobile.skytvstream.domain.RegisteredDevice.DeviceEntry;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDbConfig.class, TestRegisteredDeviceDaoImpl.TestConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = {"testdata-device.xml"})
public class TestRegisteredDeviceDaoImpl {
    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Autowired
    private RegisteredDeviceDao registeredDeviceDao;

    @Test
    public void findByProfileIdForProviderWithMatches() {
        String profileId = "UNKNOWN PROFILE ID";
        SubscriptionProvider expectProvider = SubscriptionProvider.GOOGLE;
        RegisteredDevice registeredDevice = registeredDeviceDao
                .findByProfileIdForProvider(profileId, expectProvider);
        assertNull(registeredDevice);
    }

    @Test
    public void findByProfileIdForProvider() {
        String profileId = "1234567";
        SubscriptionProvider expectProvider = SubscriptionProvider.GOOGLE;
        RegisteredDevice registeredDevice = registeredDeviceDao
                .findByProfileIdForProvider(profileId, expectProvider);
        assertEquals(1, registeredDevice.getDeviceIds().size());
        assertEquals(1, registeredDevice.getDeviceCount());
        assertEquals(SubscriptionProvider.GOOGLE,
                registeredDevice.getProvider());
        assertTrue(registeredDevice.hasUdid("devicee"));

        profileId = "123456";
        expectProvider = SubscriptionProvider.APPLE;
        registeredDevice = registeredDeviceDao.findByProfileIdForProvider(
                profileId, expectProvider);
        assertEquals(3, registeredDevice.getDeviceIds().size());
        assertEquals(3, registeredDevice.getDeviceCount());
        assertEquals(SubscriptionProvider.APPLE, registeredDevice.getProvider());
        assertTrue(registeredDevice.hasUdid("deviced")
                && registeredDevice.hasUdid("deviceh")
                && registeredDevice.hasUdid("devicei"));
    }

    @Test
    public void insertRegisteredDeviceNewDevice() {
        String expectedDeviceJson = "["
                + "{\"udid\":\"device1\",\"dateAdded\":\"14017203686501\"}"
                + "]";

        String profileid = "44444";
        SubscriptionProvider subscriptionProvider = SubscriptionProvider.VODAFONE;
        RegisteredDevice registeredDevice = new RegisteredDevice();
        registeredDevice.setDeviceId(expectedDeviceJson);
        registeredDevice.setProfileId(profileid);
        registeredDevice.setProvider(subscriptionProvider);
        registeredDeviceDao.insertRegisteredDevice(registeredDevice);

        registeredDevice = registeredDeviceDao.findByProfileIdForProvider(
                profileid, subscriptionProvider);

        assertNotNull(registeredDevice);
        assertEquals(1, registeredDevice.getDeviceIds().size());
        assertEquals(1, registeredDevice.getDeviceCount());
        assertEquals(subscriptionProvider, registeredDevice.getProvider());
        assertEquals(profileid, registeredDevice.getProfileId());
    }

    @Test
    public void updateRegisteredDeviceAddAdditionalDevice() {
        String profileid = "12345";
        SubscriptionProvider subscriptionProvider = SubscriptionProvider.GOOGLE;
        String newDevice = "newDevice";

        RegisteredDevice registeredDevice = registeredDeviceDao
                .findByProfileIdForProvider(profileid, subscriptionProvider);

        assertEquals(1, registeredDevice.getDeviceIds().size());
        assertTrue(registeredDevice.getDeviceIdsAsJson().contains("deviceC")
                && !registeredDevice.getDeviceIdsAsJson().contains(newDevice));
        assertTrue(registeredDevice.hasUdid("devicec")
                && !registeredDevice.hasUdid(newDevice));

        DeviceEntry deviceEntry = new DeviceEntry();
        deviceEntry.setDateAdded(new Date());
        deviceEntry.setUdid(newDevice);
        registeredDevice.getDeviceIds().add(deviceEntry);

        registeredDeviceDao.updateRegisteredDevice(registeredDevice);

        RegisteredDevice updatedRegisteredDevice = registeredDeviceDao
                .findByProfileIdForProvider(profileid, subscriptionProvider);

        assertEquals(2, updatedRegisteredDevice.getDeviceIds().size());
        assertEquals(2, updatedRegisteredDevice.getDeviceCount());
        assertEquals(subscriptionProvider,
                updatedRegisteredDevice.getProvider());
        assertEquals(profileid, updatedRegisteredDevice.getProfileId());
        assertEquals(2, updatedRegisteredDevice.getDeviceIds().size());
        assertTrue(updatedRegisteredDevice.getDeviceIdsAsJson().contains(
                "deviceC")
                && updatedRegisteredDevice.getDeviceIdsAsJson().contains(
                newDevice));
        assertTrue(updatedRegisteredDevice.hasUdid("devicec")
                && updatedRegisteredDevice.hasUdid(newDevice));
    }


    @Configurable
    public static class TestConfig {

        @Bean
        public RegisteredDeviceDao getRegisteredDeviceDao(DataSource dataSource) {
            return new RegisteredDeviceDaoImpl(dataSource);
        }

    }
}
