package com.sky.mobile.skytvstream.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamResponseObjectVo {

	private String nonce;
	private String streamId;
	private String uri = "http://skydvn-ssmtv-mobile-prod.mobile-tv.sky.com/ssmtv-skysports8/1371/ss8_sstv_hd.m3u8"; //always initialize with a fake default
	private String nextToken;
	private String clientChallengeResponse;

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getNextToken() {
		return nextToken;
	}

	public void setNextToken(String nextToken) {
		this.nextToken = nextToken;
	}

	public String getClientChallengeResponse() {
		return clientChallengeResponse;
	}

	public void setClientChallengeResponse(String clientChallengeResponse) {
		this.clientChallengeResponse = clientChallengeResponse;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((clientChallengeResponse == null) ? 0
						: clientChallengeResponse.hashCode());
		result = prime * result
				+ ((nextToken == null) ? 0 : nextToken.hashCode());
		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
		result = prime * result
				+ ((streamId == null) ? 0 : streamId.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		StreamResponseObjectVo other = (StreamResponseObjectVo) obj;
		if (clientChallengeResponse == null) {
			if (other.clientChallengeResponse != null)
				return false;
		} else if (!clientChallengeResponse
				.equals(other.clientChallengeResponse))
			return false;
		if (nextToken == null) {
			if (other.nextToken != null)
				return false;
		} else if (!nextToken.equals(other.nextToken))
			return false;
		if (nonce == null) {
			if (other.nonce != null)
				return false;
		} else if (!nonce.equals(other.nonce))
			return false;
		if (streamId == null) {
			if (other.streamId != null)
				return false;
		} else if (!streamId.equals(other.streamId))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
