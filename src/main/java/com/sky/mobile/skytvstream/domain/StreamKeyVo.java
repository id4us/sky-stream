package com.sky.mobile.skytvstream.domain;


public class StreamKeyVo {

	private String channelId;
	private String profileId;
	private String deviceId;
	private String model;
	private String nonce;
	private long expiryTime;

	public StreamKeyVo(String channelId, String profileId, String deviceId, String model){
		this.channelId = channelId;
		this.profileId = profileId;
		this.deviceId = deviceId;
		this.model = model;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getProfileId() {
		return profileId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getModel() {
		return model;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public long getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	@Override
	public String toString() {
		return "StreamKeyVo{" +
				"channelId='" + channelId + '\'' +
				", deviceId='" + deviceId + '\'' +
				", model='" + model + '\'' +
				", nonce='" + nonce + '\'' +
				", expiryTime=" + expiryTime +
				'}';
	}
}
