package com.sky.mobile.skytvstream.domain;

import java.util.Date;

public class SubscriptionVo {
	
	private long id;
	
	private String profileId;
	
	private String productId;
	
	private String provider;
	
	private Date expiry;

	private boolean activated;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileid) {
		this.profileId = profileid;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String product) {
		this.productId = product;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

}
