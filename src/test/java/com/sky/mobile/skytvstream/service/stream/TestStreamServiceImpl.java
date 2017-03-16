package com.sky.mobile.skytvstream.service.stream;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;
import com.sky.mobile.skytvstream.domain.StreamResponseObjectVo;
import com.sky.mobile.skytvstream.domain.StreamVerificationVo;
import com.sky.mobile.skytvstream.service.secure.ChallengeValidator;
import com.sky.mobile.skytvstream.service.secure.CryptoFactory;
import com.sky.mobile.skytvstream.service.secure.StreamKeyService;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext.MockTimeProvider;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class TestStreamServiceImpl {

    private StreamServiceImpl streamService;
    private ChallengeValidator challengeValidator;
    private MockTimeProvider timeProvider;
    private StreamKeyService streamKeyService;

    @Before
    public void setUp() throws Exception {
        streamKeyService = createMock(StreamKeyService.class);
        challengeValidator = createMock(ChallengeValidator.class);
        timeProvider = new MockTimeProviderContext.MockTimeProvider();
        timeProvider.setMockTime(System.currentTimeMillis());
        streamService = new StreamServiceImpl(streamKeyService, challengeValidator, null, timeProvider, new CryptoFactory());
    }

    @Test
    public void testCreateStream() {
        String expectedChanelId = "1001";
        String expectedUserId = "auser";
        String expectedDeviceId = "adevice";
        final String expectedStreamId = "streamId";
        final String expectedNonce = "nonce1234";

        Capture<StreamKeyVo> expectedStreamKeyVo = new Capture<>();

        expect(streamKeyService.generateKey(capture(expectedStreamKeyVo))).andAnswer(new IAnswer<Optional<String>>() {
            public Optional<String> answer() {
                StreamKeyVo passedInStreamKeyVo = ((StreamKeyVo) EasyMock.getCurrentArguments()[0]);
                passedInStreamKeyVo.setNonce(expectedNonce);
                return Optional.fromNullable(expectedStreamId);
            }
        });


        replayMocks();

        StreamResponseObjectVo response = streamService.createStream(expectedChanelId,
                expectedUserId, expectedDeviceId, expectedChanelId);

        assertEquals(expectedChanelId, expectedStreamKeyVo.getValue().getChannelId());
        assertEquals(expectedUserId, expectedStreamKeyVo.getValue().getProfileId());
        assertEquals(expectedDeviceId, expectedStreamKeyVo.getValue().getDeviceId());

        assertNotNull(response.getNonce());
        assertEquals(expectedStreamId, response.getStreamId());
        assertEquals(response.getNonce(), expectedStreamKeyVo.getValue().getNonce());

        verifyMocks();
    }

    @Test
    public void testVerifyMissingFields() {
        assertFalse(streamService.isValidStreamObject(new StreamVerificationVo()));
    }

    @Test
    public void testVerifyMissingPersistedObject() {
        String streamId = "12345";
        String channelId = "3030";
        String deviceId = "client1";
        String profileId = "profileId";
        String proxyOAuth = "proxyOAuth";
        String model = "myModel";

        StreamResponseObjectVo streamVo = new StreamResponseObjectVo();
        streamVo.setStreamId(streamId);
        streamVo.setNextToken("sdafad");
        streamVo.setClientChallengeResponse("1232143");

        expect(streamKeyService.getStreamKeyVo(eq(streamId))).andReturn(Optional.<StreamKeyVo>absent());

        replayMocks();

        AuthenticatedPerson authenticatedPerson = new AuthenticatedPerson();
        authenticatedPerson.setProfileId(profileId);
        authenticatedPerson.setEncryptedOauthToken(proxyOAuth);

        assertFalse(streamService.isValidStreamObject(new StreamVerificationVo(streamVo, authenticatedPerson, channelId, deviceId, model)));

        verifyMocks();
    }

    @Test
    public void testVerifyPayloadsDoNotMatch() {
        String streamId = "12345";
        String channelId = "3030";
        String deviceId = "client1";
        String profileId = "profileId";
        String proxyOAuth = "proxyOAuth";
        String model = "myModel";

        StreamResponseObjectVo streamVo = new StreamResponseObjectVo();
        streamVo.setStreamId(streamId);
        streamVo.setNextToken("NEXT TOKEN");
        streamVo.setClientChallengeResponse("1232143");

        StreamKeyVo streamKeyVo = new StreamKeyVo(channelId, profileId, deviceId, model);

        expect(streamKeyService.getStreamKeyVo(eq(streamId))).andReturn(Optional.fromNullable(streamKeyVo));

        replayMocks();

        AuthenticatedPerson authenticatedPerson = new AuthenticatedPerson();
        authenticatedPerson.setProfileId(profileId);
        authenticatedPerson.setEncryptedOauthToken(proxyOAuth);

        assertFalse(streamService.isValidStreamObject(new StreamVerificationVo(streamVo, authenticatedPerson, channelId, deviceId, model)));

        verifyMocks();
    }

    @Test
    public void testVerifyExpiredCache() {

        String streamId = "12345";
        String channelId = "3030";
        String deviceId = "client1";
        String profileId = "profileId";
        String proxyOAuth = "proxyOAuth";
        String challengeResponse = "challengeResponse";
        String model = "myModel";

        StreamResponseObjectVo streamVo = new StreamResponseObjectVo();
        streamVo.setStreamId(streamId);
        streamVo.setNextToken("CHANGED");
        streamVo.setClientChallengeResponse(challengeResponse);

        StreamKeyVo streamKeyVo = new StreamKeyVo(channelId, profileId, deviceId, model);
        streamKeyVo.setExpiryTime(System.currentTimeMillis() - 2000l);

        expect(streamKeyService.getStreamKeyVo(eq(streamId))).andReturn(Optional.fromNullable(streamKeyVo));

        replayMocks();

        AuthenticatedPerson authenticatedPerson = new AuthenticatedPerson();
        authenticatedPerson.setProfileId(profileId);
        authenticatedPerson.setEncryptedOauthToken(proxyOAuth);

        assertFalse(streamService.isValidStreamObject(new StreamVerificationVo(streamVo, authenticatedPerson, channelId, deviceId, model)));

        verifyMocks();
    }

    @Test
    public void testVerifyIncorrectChallenge() {

        String streamId = "12345";
        String channelId = "3030";
        String deviceId = "client1";
        String profileId = "profileId";
        String proxyOAuth = "proxyOAuth";
        String nonce = "ORIGINAL";
        String challengeResponse = "challengeResponse";
        String model = "myModel";

        StreamResponseObjectVo streamVo = new StreamResponseObjectVo();
        streamVo.setStreamId(streamId);
        streamVo.setNextToken("CHANGED");
        streamVo.setClientChallengeResponse(challengeResponse);


        StreamKeyVo streamKeyVo = new StreamKeyVo(channelId, profileId, deviceId, model);
        streamKeyVo.setExpiryTime(System.currentTimeMillis() + 3000l);
        streamKeyVo.setNonce(nonce);

        expect(streamKeyService.getStreamKeyVo(eq(streamId))).andReturn(Optional.fromNullable(streamKeyVo));

        expect(challengeValidator.validate(streamId, proxyOAuth, channelId,
                nonce, deviceId, challengeResponse, model)).andReturn(false);

        replayMocks();

        AuthenticatedPerson authenticatedPerson = new AuthenticatedPerson();
        authenticatedPerson.setProfileId(profileId);
        authenticatedPerson.setEncryptedOauthToken(proxyOAuth);

        assertFalse(streamService.isValidStreamObject(new StreamVerificationVo(streamVo, authenticatedPerson, channelId, deviceId, model)));

        verifyMocks();
    }

    @Test
    public void testVerifyHappyPath() {

        String streamId = "12345";
        String channelId = "3030";
        String deviceId = "client1";
        String profileId = "profileId";
        String proxyOAuth = "proxyOAuth";
        String nonce = "ORIGINAL";
        String challengeResponse = "challengeResponse";
        String model = "myModel";

        StreamResponseObjectVo streamVo = new StreamResponseObjectVo();
        streamVo.setStreamId(streamId);
        streamVo.setNextToken("CHANGED");
        streamVo.setClientChallengeResponse(challengeResponse);


        StreamKeyVo streamKeyVo = new StreamKeyVo(channelId, profileId, deviceId, model);
        streamKeyVo.setExpiryTime(System.currentTimeMillis() + 3000l);
        streamKeyVo.setNonce(nonce);

        expect(streamKeyService.getStreamKeyVo(eq(streamId))).andReturn(Optional.fromNullable(streamKeyVo));

        expect(challengeValidator.validate(streamId, proxyOAuth, channelId,
                nonce, deviceId, challengeResponse, model)).andReturn(true);
        replayMocks();

        AuthenticatedPerson authenticatedPerson = new AuthenticatedPerson();
        authenticatedPerson.setProfileId(profileId);
        authenticatedPerson.setEncryptedOauthToken(proxyOAuth);

        assertTrue(streamService.isValidStreamObject(new StreamVerificationVo(streamVo, authenticatedPerson, channelId, deviceId, model)));
        verifyMocks();
    }

    private void verifyMocks() {
        verify(streamKeyService, challengeValidator);
    }

    private void replayMocks() {
        replay(streamKeyService, challengeValidator);
    }
}
