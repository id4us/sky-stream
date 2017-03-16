package com.sky.mobile.skytvstream.domain;

import com.sky.mobile.skytvstream.domain.RegisteredDevice.DeviceEntry;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRegisteredDevice {

    private static final DateTime TESTDATE = new DateTime();

    @Test
    public void registerDeviceCanMarshallFromJsonDeviceString() {

        RegisteredDevice registeredDevice = new RegisteredDevice();
        String expectedDeviceJson = "["
                + "{\"udid\":\"device1\",\"dateAdded\":\"2014-06-01\"}, "
                + "{\"udid\":\"device2\",\"dateAdded\":\"2014-05-30\"},"
                + "{\"udid\":\"device3\",\"dateAdded\":\"2012-04-23T18:25:43.511\"}"
                + "]";

        registeredDevice.setDeviceId(expectedDeviceJson);

        Set<DeviceEntry> deviceIds = registeredDevice.getDeviceIds();
        assertEquals(3, deviceIds.size());
        assertEquals(3, registeredDevice.getDeviceCount());
        DeviceEntry expectedDevice = new DeviceEntry();
        expectedDevice.setUdid("Device2");
        assertTrue(deviceIds.contains(expectedDevice));
        assertTrue(registeredDevice.hasUdid("DeViCe3"));
    }

    @Test
    public void registerDeviceCanMArchelToJson() {
        RegisteredDevice registeredDevice = new RegisteredDevice();
        DeviceEntry deviceEntry = new DeviceEntry();
        deviceEntry.setUdid("device1");
        deviceEntry.setDeviceName("HTC1");
        Date firstDate = TESTDATE.toDate();
        deviceEntry.setDateAdded(firstDate);
        registeredDevice.getDeviceIds().add(deviceEntry);

        deviceEntry = new DeviceEntry();
        deviceEntry.setUdid("device2");
        deviceEntry.setDeviceName("HTCMAX");
        Date secondDate = TESTDATE.minusDays(10).toDate();
        deviceEntry.setDateAdded(secondDate);
        registeredDevice.getDeviceIds().add(deviceEntry);

        assertEquals(2, registeredDevice.getDeviceCount());

        String json = StringUtils.deleteWhitespace(registeredDevice.getDeviceIdsAsJson());
        assertTrue(json.contains("\"udid\":\"device2\""));
        assertTrue(json.contains("\"udid\":\"device1\""));
    }
}
