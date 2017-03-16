package com.sky.mobile.skytvstream.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

import java.util.Collection;

@JsonPropertyOrder(alphabetic = true)
public class ExchangeTokenResponse {
	private String oauthToken;
	private String email;

	private String vodafoneSubscriptionStatus="INACTIVE";
	private String appleSubscriptionStatus="INACTIVE";
	private String googleSubscriptionStatus="INACTIVE";

	@JsonProperty("VODAFONE")
	public String getVodafoneSubscriptionStatus() {
		return vodafoneSubscriptionStatus;
	}

	@JsonProperty("APPLE")
	public String getAppleSubscriptionStatus() {
		return appleSubscriptionStatus;
	}

	@JsonProperty("GOOGLE")
	public String getGoogleSubscriptionStatus() {
		return googleSubscriptionStatus;
	}

	public void setSubscriptionStatus(Collection<SubscriptionProvider> providers){

		for(SubscriptionProvider subscriptionProvider: providers) {
			if (SubscriptionProvider.APPLE == subscriptionProvider) {
				appleSubscriptionStatus="ACTIVE";
			} else if (SubscriptionProvider.GOOGLE == subscriptionProvider) {
				googleSubscriptionStatus="ACTIVE";
			} else if (SubscriptionProvider.VODAFONE == subscriptionProvider) {
				vodafoneSubscriptionStatus="ACTIVE";
			}
		}
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
