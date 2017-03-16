package com.sky.mobile.skytvstream.domain;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.collect.Lists;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

public class ProductVo implements Comparable<ProductVo> {

	private String name;

	private String description;

	private String displayText;

	private String displayCost;

	private String purchaseId;

	private String subscriptionProvider;

	private boolean available;

	private List<ChannelVo> channels = Lists.newArrayList();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getDescription() {
		return description;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	public List<ChannelVo> getChannels() {
		return channels;
	}

	public void setChannels(List<ChannelVo> channels) {
		this.channels = channels;
	}

	public String getSubscriptionProvider() {
		return subscriptionProvider;
	}

	public void setSubscriptionProvider(String subscriptionProvider) {
		this.subscriptionProvider = subscriptionProvider;
	}

	@JsonIgnore
	public SubscriptionProvider getSubscriptionProviderEnum() {
		return SubscriptionProvider.fromName(subscriptionProvider);

	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayCost() {
		return displayCost;
	}

	public void setDisplayCost(String displayCost) {
		this.displayCost = displayCost;
	}

	public String getPurchaseId() {
		return purchaseId;
	}

	public void setPurchaseId(String purchaseId) {
		this.purchaseId = purchaseId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
				* result
				+ ((subscriptionProvider == null) ? 0 : subscriptionProvider
						.hashCode());
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
		ProductVo other = (ProductVo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (subscriptionProvider == null) {
			if (other.subscriptionProvider != null)
				return false;
		} else if (!subscriptionProvider.equals(other.subscriptionProvider))
			return false;
		return true;
	}

	@Override
	public int compareTo(ProductVo that) {
		return this.name.compareToIgnoreCase(that.name);
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

}
