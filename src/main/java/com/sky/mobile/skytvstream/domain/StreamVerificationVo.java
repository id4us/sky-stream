package com.sky.mobile.skytvstream.domain;


import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;

public class StreamVerificationVo {

	private String clientChallengeResponse;
	private String channelId;
	private String streamId;
	private String profileId;
	private String deviceId;
	private String proxyOAuth;
	private String model;
	private String uri;
	private String nextToken;

	public StreamVerificationVo(){}

	public StreamVerificationVo(StreamResponseObjectVo streamObjectVo,
								 AuthenticatedPerson authenticatedPerson,
								 String channelId,
								 String deviceId,
								 String model){
		this.channelId = channelId;
		this.deviceId = deviceId;
		this.model = model;
		this.clientChallengeResponse = streamObjectVo.getClientChallengeResponse();
		this.streamId = streamObjectVo.getStreamId();
		this.uri = streamObjectVo.getUri();
		this.nextToken = streamObjectVo.getNextToken();
		this.profileId = authenticatedPerson.getProfileId();
		this.proxyOAuth = authenticatedPerson.getEncryptedOauthToken();
	}

	public String getUri() {
		return uri;
	}

	public String getNextToken() {
		return nextToken;
	}

	public String getClientChallengeResponse() {
		return clientChallengeResponse;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getStreamId() {
		return streamId;
	}

	public String getProfileId() {
		return profileId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getProxyOAuth() {
		return proxyOAuth;
	}

	public String getModel() {
		return model;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StreamVerificationVo that = (StreamVerificationVo) o;

		if (clientChallengeResponse != null ? !clientChallengeResponse.equals(that.clientChallengeResponse) : that.clientChallengeResponse != null)
			return false;
		if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) return false;
		if (streamId != null ? !streamId.equals(that.streamId) : that.streamId != null) return false;
		if (profileId != null ? !profileId.equals(that.profileId) : that.profileId != null) return false;
		if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;
		if (proxyOAuth != null ? !proxyOAuth.equals(that.proxyOAuth) : that.proxyOAuth != null) return false;
		if (model != null ? !model.equals(that.model) : that.model != null) return false;
		if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
		return !(nextToken != null ? !nextToken.equals(that.nextToken) : that.nextToken != null);

	}

	@Override
	public int hashCode() {
		int result = clientChallengeResponse != null ? clientChallengeResponse.hashCode() : 0;
		result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
		result = 31 * result + (streamId != null ? streamId.hashCode() : 0);
		result = 31 * result + (profileId != null ? profileId.hashCode() : 0);
		result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
		result = 31 * result + (proxyOAuth != null ? proxyOAuth.hashCode() : 0);
		result = 31 * result + (model != null ? model.hashCode() : 0);
		result = 31 * result + (uri != null ? uri.hashCode() : 0);
		result = 31 * result + (nextToken != null ? nextToken.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "StreamVerificationVo{" +
				"clientChallengeResponse='" + clientChallengeResponse + '\'' +
				", channelId='" + channelId + '\'' +
				", streamId='" + streamId + '\'' +
				", deviceId='" + deviceId + '\'' +
				", proxyOAuth='" + proxyOAuth + '\'' +
				", model='" + model + '\'' +
				", uri='" + uri + '\'' +
				", nextToken='" + nextToken + '\'' +
				'}';
	}
}
