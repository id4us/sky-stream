package com.sky.mobile.skytvstream.service.secure;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;
import com.sky.mobile.skytvstream.service.stream.*;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.*;


public class TestCacheStreamKeyServiceImpl {

    private StreamKeyService streamKeyService;
    private PersistStream persistStream;

    @Before
    public void setUp() {
        persistStream = createMock(PersistStream.class);
        streamKeyService = new CacheStreamKeyServiceImpl(persistStream, new CryptoFactory());
    }

    @Test
    public void successfulGenerateKey() throws PersistQueueException {
        String expectedChanelId = "1001";
        String expectedUserId = "auser";
        String expectedDeviceId = "adevice";
        String model = "aModel";

        Capture<PersistPayload> expectedPayload = new Capture<>();
        Capture<String> expectedStreamKey = new Capture<>();

        persistStream.persistStreamInfo(capture(expectedStreamKey),
                capture(expectedPayload));

        replay(persistStream);

        StreamKeyVo streamKeyVo = new StreamKeyVo(expectedChanelId, expectedUserId, expectedDeviceId, model);
        Optional<String> key = streamKeyService.generateKey(streamKeyVo);

        assertEquals(expectedChanelId, expectedPayload.getValue().getChannelId());
        assertEquals(expectedUserId, expectedPayload.getValue().getUniqueUserId());
        assertEquals(expectedDeviceId, expectedPayload.getValue().getUniqueDeviceId());
        assertEquals(model, expectedPayload.getValue().getModel());
        assertNotNull(expectedPayload.getValue().getNonce());
        assertTrue(key.isPresent());
        assertEquals(key.get(), expectedStreamKey.getValue());

        verify(persistStream);
    }

    @Test
    public void persistQueueExceptionWhenGenerateKey() throws PersistQueueException {

        persistStream.persistStreamInfo(anyString(), anyObject(PersistPayload.class));
        expectLastCall().andThrow(new PersistQueueException("null", new RuntimeException())).anyTimes();

        replay(persistStream);

        StreamKeyVo streamKeyVo = new StreamKeyVo("xx", "sss", "dfasd", "www");
        Optional<String> keyOption = streamKeyService.generateKey(streamKeyVo);
        assertFalse(keyOption.isPresent());

        verify(persistStream);
    }

    @Test
    public void persistNotFoundExceptionWhenGetStreamKeyVo() throws PersistNotFoundException, PersistGetTimeoutException {
        String key = "key";

        persistStream.removeStreamInfo(eq(key));

        expect(persistStream.getStreamInfo(eq(key))).andThrow(new PersistNotFoundException("null")).anyTimes();

        replay(persistStream);

        Optional<StreamKeyVo> streamKeyVoOption = streamKeyService.getStreamKeyVo(key);
        assertFalse(streamKeyVoOption.isPresent());

        verify(persistStream);
    }

    @Test
    public void successfulGetStreamKeyVo() throws PersistNotFoundException, PersistGetTimeoutException {
        String key = "key";

        PersistPayload persistPayload = new PersistPayload();
        persistPayload.setChannelId("channelId");
        persistPayload.setUniqueUserId("profileId");
        persistPayload.setUniqueDeviceId("sadsadas");
        persistPayload.setModel("modelx");
        persistPayload.setNonce("nonce");

        persistStream.removeStreamInfo(eq(key));

        expect(persistStream.getStreamInfo(eq(key))).andReturn(persistPayload);

        replay(persistStream);

        Optional<StreamKeyVo> streamKeyVoOption = streamKeyService.getStreamKeyVo(key);

        assertTrue(streamKeyVoOption.isPresent());
        assertEquals(persistPayload.getChannelId(), streamKeyVoOption.get().getChannelId());
        assertEquals(persistPayload.getUniqueDeviceId(), streamKeyVoOption.get().getDeviceId());
        assertEquals(persistPayload.getUniqueUserId(), streamKeyVoOption.get().getProfileId());
        assertEquals(persistPayload.getModel(), streamKeyVoOption.get().getModel());
        assertNotNull(streamKeyVoOption.get().getNonce());
        assertTrue(streamKeyVoOption.get().getExpiryTime() > System.currentTimeMillis());

        verify(persistStream);
    }
}
