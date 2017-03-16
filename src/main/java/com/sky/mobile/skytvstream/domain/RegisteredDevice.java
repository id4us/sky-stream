package com.sky.mobile.skytvstream.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

public class RegisteredDevice {

	private static final Logger LOG = LoggerFactory
			.getLogger(RegisteredDevice.class);

	private String profileId;
	private SubscriptionProvider provider;
	private final Set<DeviceEntry> deviceIds = Sets.newHashSet();

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public SubscriptionProvider getProvider() {
		return provider;
	}

	public String getProviderString() {
		return provider.getProviderName();
	}

	public void setProvider(
			SubscriptionProvider provider) {
		this.provider = provider;
	}

	public void setProvider(String provider) {
		this.provider = SubscriptionProvider
				.fromName(provider);
	}

	public void setDeviceId(String devices) {
		deviceIds.clear();
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<DeviceEntry> list = mapper.readValue(devices,
					new TypeReference<List<DeviceEntry>>() {
					});

			deviceIds.addAll(list);
		} catch (Exception e) {
			LOG.warn("Unexpected Error parsing device String" + devices, e);
		}
	}

	public Set<DeviceEntry> getDeviceIds() {
		return deviceIds;
	}

	public String getDeviceIdsAsJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(deviceIds);
		} catch (Exception e) {
			LOG.warn(
					"Unable to marshall set" + deviceIds + " into Json String",
					e);
			return null;
		}
	}

	public long getDeviceCount() {
		return deviceIds.size();
	}
	
	
	public boolean hasUdid(String udid) {
		DeviceEntry expected = new DeviceEntry();
		expected.setUdid(udid);
		return deviceIds.contains(expected);
	}

	public static class DeviceEntry {
		
		private String udid;
		private String deviceName;
		private Date dateAdded = new Date();
		
		public DeviceEntry() {}

		public String getUdid() {
			return udid;
		}

		public void setUdid(String udid) {
			this.udid = udid;
		}

		public Date getDateAdded() {
			return dateAdded;
		}

		public void setDateAdded(Date dateAdded) {
			this.dateAdded = dateAdded;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((udid == null) ? 0 : udid.toLowerCase().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DeviceEntry other = (DeviceEntry) obj;
			if (udid == null) {
				if (other.udid != null)
					return false;
			} else if (!udid.equalsIgnoreCase(other.udid))
				return false;
			return true;
		}

		public String getDeviceName() {
			return deviceName;
		}

		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		
	}
}
