package com.sky.mobile.skytvstream.service.secure;

public interface ChallengeValidator {

	boolean validate(String streamId, String proxyAouth, String channelId,
			String nonce, String deviceId, String challenge, String model);

}

