package com.sky.mobile.skytvstream.service.secure;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;
import com.sky.mobile.skytvstream.service.stream.PersistGetTimeoutException;
import com.sky.mobile.skytvstream.service.stream.PersistNotFoundException;
import com.sky.mobile.skytvstream.service.stream.PersistQueueException;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.*;


public class TestAlgorithmicStreamKeyServiceImpl {

    private StreamKeyService streamKeyService;
    private MockTimeProviderContext.MockTimeProvider timeProvider;
    private CryptoFactory cryptoFactory;
    private long TIME = 1435768153000l;
    private long TIME_TO_LIVE = 2000l;

    @Before
    public void setUp() {
        timeProvider = new MockTimeProviderContext.MockTimeProvider();
        cryptoFactory = createMock(CryptoFactory.class);
        timeProvider.setMockTime(TIME);
        streamKeyService = new AlgorithmicStreamKeyServiceImpl(timeProvider, cryptoFactory, "serverSeed", TIME_TO_LIVE);
    }

    @Test
    public void successfulGenerateKey() throws PersistQueueException {
        expect(cryptoFactory.getNonce()).andReturn("IMANONCE".getBytes());

        replay(cryptoFactory);

        StreamKeyVo streamKeyVo = new StreamKeyVo("1001", "auser", "adevice", "aModel");
        Optional<String> key = streamKeyService.generateKey(streamKeyVo);

        assertTrue(key.isPresent());
        assertEquals("NO5zt9suiedK4aXiir1duauJCwTU7NModwecnVFvDA4wpDoYv8KZjPlRWZ/GM+qAChMJvW3h8/JNTtoMXWUX/A==", key.get());
        verify(cryptoFactory);
    }

    @Test
    public void illegalStateExceptionWhenGenerateKey() {

        cryptoFactory.getNonce();
        expectLastCall().andThrow(new IllegalStateException()).anyTimes();

        replay(cryptoFactory);

        StreamKeyVo streamKeyVo = new StreamKeyVo("xx", "sss", "dfasd", "www");
        Optional<String> keyOption = streamKeyService.generateKey(streamKeyVo);
        assertFalse(keyOption.isPresent());

        verify(cryptoFactory);
    }

    @Test
    public void exceptionWhenGetStreamKeyVo() throws PersistNotFoundException, PersistGetTimeoutException {
        String key = "youcantparsethismate";
        Optional<StreamKeyVo> streamKeyVoOption = streamKeyService.getStreamKeyVo(key);
        assertFalse(streamKeyVoOption.isPresent());
    }

    @Test
    public void successfulGetStreamKeyVo() throws PersistNotFoundException, PersistGetTimeoutException {
        String key = "NO5zt9suiedK4aXiir1duauJCwTU7NModwecnVFvDA4wpDoYv8KZjPlRWZ/GM+qAChMJvW3h8/JNTtoMXWUX/A==";

        Optional<StreamKeyVo> streamKeyVoOption = streamKeyService.getStreamKeyVo(key);

        assertTrue(streamKeyVoOption.isPresent());
        assertEquals("1001", streamKeyVoOption.get().getChannelId());
        assertEquals("adevice", streamKeyVoOption.get().getDeviceId());
        assertEquals("auser", streamKeyVoOption.get().getProfileId());
        assertEquals("aModel", streamKeyVoOption.get().getModel());
        assertEquals("IMANONCE", streamKeyVoOption.get().getNonce());
        assertEquals(1435768155000l, streamKeyVoOption.get().getExpiryTime());
    }
}
