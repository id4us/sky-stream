package com.sky.mobile.skytvstream.service.secure;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestChallengeValidatorImpl {

    private static final String TEST_SALT = "HxG]m;l2k%Tab:0.9afaiZSPUf$q}l/a:B(}k)C";
    private ChallengeValidatorImpl validator;

    @Before
    public void setup() {
        validator = new ChallengeValidatorImpl();
        validator.salt = TEST_SALT;
    }

    @Test
    public void test_fails_invalid_challenge() {
        String challenge = "bad challenge";
        String streamId = "1234";
        String proxyAouth = "kdfjlsdhflakjsdfglkfdas";
        String channelId = "3303";
        String nonce = "348023948023";
        String deviceId = "3242423423";
        String model = "aModel";
        assertFalse(validator.validate(streamId, proxyAouth, channelId, nonce, deviceId, challenge, model));
    }

    @Test
    public void test_happy_path() {
        String challenge = "LWwnVP8zsk3JI2n7GLyyCXma2Os=";
        String streamId = "1234";
        String proxyAouth = "kdfjlsdhflakjsdfglkfdas";
        String channelId = "3303";
        String nonce = "348023948023";
        String deviceId = "3242423423";
        String model = "aModel";
        assertFalse(validator.validate(streamId, proxyAouth, channelId, nonce, deviceId, challenge, model));
    }

}
