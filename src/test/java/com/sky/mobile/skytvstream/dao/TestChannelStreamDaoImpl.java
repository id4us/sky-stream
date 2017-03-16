package com.sky.mobile.skytvstream.dao;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.ChannelStreamVo;
import com.sky.mobile.skytvstream.event.Events;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class TestChannelStreamDaoImpl {
    private static final String TEST_STREAMLIST = "src/test/resources/testdiscovery/channel_streams.json";
    private static final String TEST_STREAMLISTALT = "src/test/resources/testdiscovery/channel_streams_alt.json";

    private ChannelStreamDaoImpl instance;
    private StreamConfig streamConfig;

    @Before
    public void setup() throws Exception {
        streamConfig = createMock(StreamConfig.class);
        instance = new ChannelStreamDaoImpl(streamConfig);

        File f = new File(TEST_STREAMLIST);
        String json = FileUtils.readFileToString(f);

        expect(streamConfig.getConfiguration(StreamConfig.CHANNEL_STREAMS))
                .andReturn(json);
        replay(streamConfig);
        instance.afterPropertiesSet();
    }

    @After
    public void verifyMocks() {
        verify(streamConfig);
    }

    @Test
    public void testGetChanelStreamByChannelId() {

        Optional<ChannelStreamVo> optionalResult = instance
                .getChanelStreamByChannelId("1301");
        assertTrue(optionalResult.isPresent());
        ChannelStreamVo result = optionalResult.get();
        assertEquals("1301", result.getChannelId());
        assertEquals("Sky Sports 1", result.getName());
        assertEquals("/key/*", result.getKeyPath());
        assertEquals(
                "http://poc-content.mobile-tv.sky.com/content/ss1/ss1.m3u8",
                result.getPlaylist());
        assertEquals("/content/ss1/*!", result.getPlaylistPath());

        optionalResult = instance.getChanelStreamByChannelId("1322");
        assertTrue(optionalResult.isPresent());
        result = optionalResult.get();
        assertEquals("1322", result.getChannelId());
        assertEquals("Sky Sports 4", result.getName());
        assertEquals("/key/*", result.getKeyPath());
        assertEquals(
                "http://poc-content.mobile-tv.sky.com/content/ss4/ss4.m3u8",
                result.getPlaylist());
        assertEquals("/content/ss4/*!", result.getPlaylistPath());

    }

    @Test
    public void testRefresh() throws IOException {

        Optional<ChannelStreamVo> optionalResult = instance
                .getChanelStreamByChannelId("1301");
        assertTrue(optionalResult.isPresent());
        ChannelStreamVo result = optionalResult.get();
        assertEquals("1301", result.getChannelId());
        assertEquals("Sky Sports 1", result.getName());
        assertEquals("/key/*", result.getKeyPath());
        assertEquals(
                "http://poc-content.mobile-tv.sky.com/content/ss1/ss1.m3u8",
                result.getPlaylist());
        assertEquals("/content/ss1/*!", result.getPlaylistPath());

        verify(streamConfig);
        reset(streamConfig);
        File f = new File(TEST_STREAMLISTALT);
        String json = FileUtils.readFileToString(f);
        expect(streamConfig.getConfiguration(StreamConfig.CHANNEL_STREAMS))
                .andReturn(json);
        replay(streamConfig);

        instance.onApplicationEvent(Events.newRefreshDataEvent(this));

        optionalResult = instance.getChanelStreamByChannelId("1301");
        assertTrue(optionalResult.isPresent());
        result = optionalResult.get();
        assertEquals("1301", result.getChannelId());
        assertEquals("Sky Sports 1 ALT", result.getName());
        assertEquals("/key/*", result.getKeyPath());
        assertEquals(
                "http://poc-content.mobile-tv.sky.com/content/ss1a/ss1a.m3u8",
                result.getPlaylist());
        assertEquals("/content/ss1a/*!", result.getPlaylistPath());

        optionalResult = instance.getChanelStreamByChannelId("1322");
        assertFalse(optionalResult.isPresent());
    }

}
