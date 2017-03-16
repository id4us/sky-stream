package com.sky.mobile.skytvstream.service.secure;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChallengeValidatorImpl implements ChallengeValidator {
	private static final Logger LOG = LoggerFactory
			.getLogger(ChallengeValidatorImpl.class);

	@Value("${Xstream.access.challenge.salt}")
	String salt;

	@Override
	public boolean validate(String streamId, String proxyAouth,
			String channelId, String nonce, String deviceId, String challenge, String model) {

		StringBuilder sb = new StringBuilder();
		sb.append(salt).append(',').append(streamId).append(',')
				.append(proxyAouth).append(',').append(channelId).append(',')
				.append(nonce).append(',').append(deviceId).append(',').append(model);

        LOG.debug("challenge string:{}",sb.toString());

		try {
			final byte[] expected = DigestUtils.sha(sb.toString().getBytes(
					"UTF-8"));
			final String challengeBase64 = new String(
					new Base64().encode(expected));
			return StringUtils.equals(challenge, challengeBase64);
		} catch (UnsupportedEncodingException e) {
			LOG.warn("unsupported encoding of expected challenge", e);
		}

		return false;
	}

}
