package com.sky.mobile.skytvstream.service.device;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.dao.RegisteredDeviceDao;
import com.sky.mobile.skytvstream.domain.DeviceVo;
import com.sky.mobile.skytvstream.domain.RegisteredDevice;
import com.sky.mobile.skytvstream.domain.RegisteredDevice.DeviceEntry;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

@Service
public class DeviceServiceImpl implements DeviceService, ApplicationListener<RefreshDataEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final StreamConfig streamConfig;
    private Set<String> unsupportedDevicesList;
    private RegisteredDeviceDao registeredDeviceDao;

    private void loadData() throws IOException {
        String content = streamConfig.getConfiguration(
                StreamConfig.UNSUPPORTED_DEVICES);
        content = content.toUpperCase();
        unsupportedDevicesList = Sets.newHashSet(Splitter.on(',').trimResults()
                .omitEmptyStrings().split(content));
    }

    @Autowired
    public DeviceServiceImpl(final StreamConfig streamConfig,
                             RegisteredDeviceDao registeredDeviceDao)
            throws IOException {
        this.streamConfig = streamConfig;
        this.registeredDeviceDao = registeredDeviceDao;
        loadData();
    }

    @Override
    public DeviceVo getDeviceVo(String deviceHeader) {
        String[] headerParts = deviceHeader.split(";");
        return new DeviceVo(headerParts[0].trim(), headerParts.length > 1 ? headerParts[1].trim() : "");
    }

    /*
     * Any value for os other than IOS will force a black list check
     */
    @Override
    public boolean isSupportDevice(DeviceVo deviceVo) {
        Preconditions.checkNotNull(unsupportedDevicesList);
        return deviceVo.getOs().trim().equalsIgnoreCase("IOS") ? true
                : deviceVo.getOs().trim().equalsIgnoreCase("ANDROID")
                && StringUtils.isNotBlank(deviceVo.getModel())
                && !unsupportedDevicesList.contains(deviceVo.getModel()
                .toUpperCase().trim());
    }

    @Override
    public boolean isRegistered(String clientID,
                                SubscriptionProvider subscriptionProvider, String profileId, String deviceName) {

        RegisteredDevice registeredDevice = registeredDeviceDao
                .findByProfileIdForProvider(profileId, subscriptionProvider);

        if (registeredDevice != null) {
            if (registeredDevice.hasUdid(clientID)) {
                return true;
            } else if (registeredDevice.getDeviceCount() >= subscriptionProvider
                    .getMaxSupportedDevices()) {
                return false;
            } else {
                registerAdditionalDevice(registeredDevice, clientID, deviceName);
            }
        } else {
            registeredDevice = new RegisteredDevice();
            registeredDevice.setProfileId(profileId);
            registeredDevice.setProvider(subscriptionProvider);
            registerFirstDevice(registeredDevice, clientID, deviceName);
        }
        return true;
    }

    @Override
    public void onApplicationEvent(RefreshDataEvent event) {
        try {
            loadData();
        } catch (IOException e) {
            LOG.error("Ioexception trying to Load/refresh Device List", e);
        }
    }

    private static void createDeviceEntry(RegisteredDevice registeredDevice,
                                          String clientID, String deviceName) {
        DeviceEntry entry = new DeviceEntry();
        entry.setUdid(clientID);
        entry.setDateAdded(new Date());
        entry.setDeviceName(deviceName);
        registeredDevice.getDeviceIds().add(entry);
    }

    private void registerAdditionalDevice(RegisteredDevice registeredDevice, String clientID, String deviceName) {
        try {
            createDeviceEntry(registeredDevice, clientID, deviceName);
            registeredDeviceDao.updateRegisteredDevice(registeredDevice);
        } catch (Exception e) { // fail silently and let user continue, this will be tried again and will eventually result in db consistency
            LOG.warn("failed to register additional device, let the user continue as normal profileId={} clientId={}", registeredDevice.getProfileId(), clientID, e);
        }
    }


    private void registerFirstDevice(RegisteredDevice registeredDevice, String clientID, String deviceName) {
        try {
            createDeviceEntry(registeredDevice, clientID, deviceName);
            registeredDeviceDao.insertRegisteredDevice(registeredDevice);
        } catch (Exception e) { // fail silently and let user continue, this will be tried again and will eventually result in db consistency
            LOG.warn("failed to register first device, let the user continue as normal profileId={} clientId={}", registeredDevice.getProfileId(), clientID, e);
        }
    }

}
