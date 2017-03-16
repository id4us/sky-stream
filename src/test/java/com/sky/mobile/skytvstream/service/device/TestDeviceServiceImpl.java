package com.sky.mobile.skytvstream.service.device;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.dao.RegisteredDeviceDao;
import com.sky.mobile.skytvstream.domain.DeviceVo;
import com.sky.mobile.skytvstream.domain.RegisteredDevice;
import com.sky.mobile.skytvstream.domain.RegisteredDevice.DeviceEntry;
import com.sky.mobile.skytvstream.event.Events;
import com.sky.mobile.skytvstream.utils.DeviceVendor;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDeviceServiceImpl {

    private DeviceServiceImpl deviceService;
    private static final String EXPECTED_UNSUPPORTED_DEVICES = "narinders_810, rakesh_22637";
    private static final String EXPECTED_UNSUPPORTED_DEVICES_ALT = "Samsung Galaxy s4";
    private StreamConfig mockStreamConfig;
    private RegisteredDeviceDao registeredDeviceDao;

    @Before
    public void setup() throws IOException {
        mockStreamConfig = mock(StreamConfig.class);
        when(mockStreamConfig.getConfiguration(StreamConfig.UNSUPPORTED_DEVICES))
                .thenReturn(EXPECTED_UNSUPPORTED_DEVICES);
        registeredDeviceDao = createMock(RegisteredDeviceDao.class);
        deviceService = new DeviceServiceImpl(mockStreamConfig,
                registeredDeviceDao);
    }

    @Test
    public void isSupportedDeviceTrue() {
        DeviceVo expected = new DeviceVo();
        expected.setModel("Z710e-17");
        expected.setOs("android");
        assertTrue(deviceService.isSupportDevice(expected));
    }

    @Test
    public void isSupportedDeviceCaseInsensitiveTrue() {
        DeviceVo expected = new DeviceVo();
        expected.setModel("z710E-17");
        expected.setOs("ANDROID");
        assertTrue(deviceService.isSupportDevice(expected));
    }

    @Test
    public void isSupportedDeviceBlankFalse() {
        DeviceVo expected = new DeviceVo();
        expected.setModel("     ");
        expected.setOs("ANY DEVICE");
        assertFalse(deviceService.isSupportDevice(expected));
    }

    @Test
    public void isSupportedDeviceCommaFalse() {
        DeviceVo expected = new DeviceVo();
        expected.setModel(",");
        expected.setOs("WINDOWS");
        assertFalse(deviceService.isSupportDevice(expected));
    }

    @Test
    public void isSupportedDeviceForIosISAlwaysTrue() {
        DeviceVo expected = new DeviceVo();
        expected.setOs("ios");
        assertTrue(deviceService.isSupportDevice(expected));
    }

    @Test
    public void isSupportedDeviceForIOSISAlwaysTrue() {
        DeviceVo expected = new DeviceVo();
        expected.setOs("IOS");
        assertTrue(deviceService.isSupportDevice(expected));
    }

    @Test
    public void getDeviceVoWorkWithCorrectHeader() {
        String deviceHeader = "android ; Z710e-17";
        DeviceVo deviceVo = deviceService.getDeviceVo(deviceHeader);
        assertEquals("Z710e-17", deviceVo.getModel());
        assertEquals("android", deviceVo.getOs());
        assertEquals(DeviceVendor.ANDROID, deviceVo.getDeviceVendorEnum());
    }

    @Test
    public void getDeviceVoWorkWithCorrectHeaderAndDeviceCaseInsentive() {
        String deviceHeader = "AndrOid;Z710e-17";
        DeviceVo deviceVo = deviceService.getDeviceVo(deviceHeader);
        assertEquals("Z710e-17", deviceVo.getModel());
        assertEquals("AndrOid", deviceVo.getOs());
        assertEquals(DeviceVendor.ANDROID, deviceVo.getDeviceVendorEnum());
    }

    @Test
    public void getDeviceVoIOSWorkWithCorrectHeaderAndDeviceCaseInsentive() {
        String deviceHeader = "IoS;Any version";
        DeviceVo deviceVo = deviceService.getDeviceVo(deviceHeader);
        assertEquals("IoS", deviceVo.getOs());
        assertEquals("Any version", deviceVo.getModel());
        assertEquals(DeviceVendor.IOS, deviceVo.getDeviceVendorEnum());
    }

    @Test
    public void getDeviceVoIOSWorkWithInCorrectHeaderAndDeviceCaseInsentive() {
        String deviceHeader = "WINDOWS;9.0";
        DeviceVo deviceVo = deviceService.getDeviceVo(deviceHeader);
        assertEquals("WINDOWS", deviceVo.getOs());
        assertEquals("9.0", deviceVo.getModel());
        assertEquals(DeviceVendor.UNKNOWN, deviceVo.getDeviceVendorEnum());
    }

    @Test
    public void isSupportedDeviceTrueOnReload() throws IOException {
        DeviceVo expected = new DeviceVo();
        expected.setModel("Samsung Galaxy s4");
        expected.setOs("android");
        assertTrue(deviceService.isSupportDevice(expected));

        when(mockStreamConfig.getConfiguration(StreamConfig.UNSUPPORTED_DEVICES))
                .thenReturn(EXPECTED_UNSUPPORTED_DEVICES_ALT);

        deviceService.onApplicationEvent(Events.newRefreshDataEvent(this));

        assertFalse(deviceService.isSupportDevice(expected));
    }

    @Test
    public void registeredFirstAndroidDeviceWhenSqlFailsShouldFailSilently() {

        String expectedClientID = "device1234";
        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.GOOGLE;
        String expectedProfileId = "123456";
        String expectedDevice = "HTC MAX";

        expect(
                registeredDeviceDao.findByProfileIdForProvider(
                        expectedProfileId, expectedSubscriptionProvider))
                .andReturn(null);

        Capture<RegisteredDevice> captured = new Capture<RegisteredDevice>();
        registeredDeviceDao.insertRegisteredDevice(EasyMock.capture(captured));
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(expectedClientID,
                expectedSubscriptionProvider, expectedProfileId, expectedDevice);

        assertTrue(result);
        assertEquals(expectedProfileId, captured.getValue().getProfileId());
        assertEquals(expectedSubscriptionProvider, captured.getValue()
                .getProvider());
        assertEquals(1, captured.getValue().getDeviceCount());
        assertTrue(captured.getValue().hasUdid(expectedClientID));

        EasyMock.verify(registeredDeviceDao);

    }

    @Test
    public void registeredFirstAndroidDevice() {

        String expectedClientID = "device1234";
        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.GOOGLE;
        String expectedProfileId = "123456";
        String expectedDevice = "HTC MAX";

        expect(
                registeredDeviceDao.findByProfileIdForProvider(
                        expectedProfileId, expectedSubscriptionProvider))
                .andReturn(null);

        Capture<RegisteredDevice> captured = new Capture<RegisteredDevice>();
        registeredDeviceDao.insertRegisteredDevice(EasyMock.capture(captured));
        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(expectedClientID,
                expectedSubscriptionProvider, expectedProfileId, expectedDevice);

        assertTrue(result);
        assertEquals(expectedProfileId, captured.getValue().getProfileId());
        assertEquals(expectedSubscriptionProvider, captured.getValue()
                .getProvider());
        assertEquals(1, captured.getValue().getDeviceCount());
        assertTrue(captured.getValue().hasUdid(expectedClientID));

        EasyMock.verify(registeredDeviceDao);

    }

    @Test
    public void registeredAdditionalAppleDeviceWhenSqlFailsShouldFailSilently() {

        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.APPLE;
        String expectedProfileId = "123456";

        String additionalDeviceClientID = "additionalClientId";
        String additionalDeviceName = "iPhone 6";

        RegisteredDevice registeredDevice = new RegisteredDevice();
        registeredDevice.setDeviceId("[{\"udid\":\"0c5d90511ffa2fa1281ff35320885c42\",\"deviceName\":\"iPhone 5\",\"dateAdded\":1445349296995}]");
        registeredDevice.setProfileId(expectedProfileId);
        registeredDevice.setProvider(expectedSubscriptionProvider);

        expect(registeredDeviceDao.findByProfileIdForProvider(
                expectedProfileId, expectedSubscriptionProvider))
                .andReturn(registeredDevice);

        Capture<RegisteredDevice> captured = new Capture<RegisteredDevice>();
        registeredDeviceDao.updateRegisteredDevice(EasyMock.capture(captured));
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(additionalDeviceClientID,
                expectedSubscriptionProvider, expectedProfileId, additionalDeviceName);

        assertTrue(result);
        assertEquals(expectedProfileId, captured.getValue().getProfileId());
        assertEquals(expectedSubscriptionProvider, captured.getValue().getProvider());
        assertEquals(2, captured.getValue().getDeviceCount());

        List<DeviceEntry> sortedList = sort(captured.getValue().getDeviceIds());

        DeviceEntry firstDevice= sortedList.get(0);
        assertEquals("0c5d90511ffa2fa1281ff35320885c42", firstDevice.getUdid());
        assertEquals("iPhone 5", firstDevice.getDeviceName());

        DeviceEntry secondDevice =sortedList.get(1);
        assertEquals(additionalDeviceClientID, secondDevice.getUdid());
        assertEquals(additionalDeviceName, secondDevice.getDeviceName());

        EasyMock.verify(registeredDeviceDao);
    }

    private List<DeviceEntry> sort(Set<DeviceEntry> set){
        List<DeviceEntry> sortedList = new ArrayList(set);
        Collections.sort(sortedList, new Comparator<DeviceEntry>(){
            public int compare(DeviceEntry o1, DeviceEntry o2) {
                return o1.getUdid().compareTo(o2.getUdid());
            }
        });
        return sortedList;
    }

    @Test
    public void registeredAdditionalAppleDevice() {

        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.APPLE;
        String expectedProfileId = "123456";

        String additionalDeviceClientID = "additionalClientId";
        String additionalDeviceName = "iPhone 6";

        RegisteredDevice registeredDevice = new RegisteredDevice();
        registeredDevice.setDeviceId("[{\"udid\":\"0c5d90511ffa2fa1281ff35320885c42\",\"deviceName\":\"iPhone 5\",\"dateAdded\":1445349296995}]");
        registeredDevice.setProfileId(expectedProfileId);
        registeredDevice.setProvider(expectedSubscriptionProvider);

        expect(registeredDeviceDao.findByProfileIdForProvider(
                expectedProfileId, expectedSubscriptionProvider))
                .andReturn(registeredDevice);

        Capture<RegisteredDevice> captured = new Capture<RegisteredDevice>();
        registeredDeviceDao.updateRegisteredDevice(EasyMock.capture(captured));
        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(additionalDeviceClientID,
                expectedSubscriptionProvider, expectedProfileId, additionalDeviceName);

        assertTrue(result);
        assertEquals(expectedProfileId, captured.getValue().getProfileId());
        assertEquals(expectedSubscriptionProvider, captured.getValue().getProvider());
        assertEquals(2, captured.getValue().getDeviceCount());

        List<DeviceEntry> sortedList = sort(captured.getValue().getDeviceIds());

        DeviceEntry firstDevice= sortedList.get(0);
        assertEquals("0c5d90511ffa2fa1281ff35320885c42", firstDevice.getUdid());
        assertEquals("iPhone 5", firstDevice.getDeviceName());

        DeviceEntry secondDevice =sortedList.get(1);
        assertEquals(additionalDeviceClientID, secondDevice.getUdid());
        assertEquals(additionalDeviceName, secondDevice.getDeviceName());

        EasyMock.verify(registeredDeviceDao);
    }

    @Test
    public void checkAlreadyRegisteredVodafoneDevice() {

        String expectedClientID = "device1234";
        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.VODAFONE;
        String expectedProfileId = "123456";
        String expectedDevice = "another model";

        RegisteredDevice expectedRegisteredDevice = createRegisteredDevice(expectedProfileId, expectedSubscriptionProvider,
                "device1234");

        expect(
                registeredDeviceDao.findByProfileIdForProvider(
                        expectedProfileId, expectedSubscriptionProvider))
                .andReturn(expectedRegisteredDevice);

        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(expectedClientID,
                expectedSubscriptionProvider, expectedProfileId, expectedDevice);

        assertTrue(result);

        EasyMock.verify(registeredDeviceDao);

    }

    @Test
    public void limitReachedWhenRegisteringAnAdditionalVodafoneDevice() {

        String expectedClientID = "device1234";
        SubscriptionProvider expectedSubscriptionProvider = SubscriptionProvider.VODAFONE;
        String expectedProfileId = "123456";
        String expectedDeviceType = "test Model";

        RegisteredDevice expectedRegisteredDevice = createRegisteredDevice(expectedProfileId, expectedSubscriptionProvider,
                "device1");

        expect(
                registeredDeviceDao.findByProfileIdForProvider(
                        expectedProfileId, expectedSubscriptionProvider))
                .andReturn(expectedRegisteredDevice);

        replay(registeredDeviceDao);

        boolean result = deviceService.isRegistered(expectedClientID,
                expectedSubscriptionProvider, expectedProfileId, expectedDeviceType);

        assertFalse(result);

        EasyMock.verify(registeredDeviceDao);

    }

    private RegisteredDevice createRegisteredDevice(String profileId,
                                                    SubscriptionProvider subscriptionProvider, String... udids) {
        RegisteredDevice registeredDevice = new RegisteredDevice();
        registeredDevice.setProfileId(profileId);
        registeredDevice.setProvider(subscriptionProvider);
        for (String s : udids) {
            DeviceEntry entry = new DeviceEntry();
            String[] split = s.split("-");
            entry.setUdid(split[0]);
            entry.setDeviceName(split.length > 1 ? split[1] : null);
            entry.setDateAdded(new Date());
            registeredDevice.getDeviceIds().add(entry);
        }
        return registeredDevice;
    }

}
