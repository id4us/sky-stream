package com.sky.mobile.skytvstream.service.versions;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.event.Events;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestVersionServiceImpl {

    private static final String TEST_ALLOWED_VERSIONS = "v0_8,v0_9,v1_0, v1_1";
    private static final String TEST_SLATED_VERSIONS = "v0_8,v0_9";
    private VersionServiceImpl instance;
    private StreamConfig streamConfig;

    @Before
    public void setup() throws Exception {
        streamConfig = createMock(StreamConfig.class);
        expect(streamConfig.getConfiguration(StreamConfig.ALLOWED_VERSIONS_KEY))
                .andReturn(TEST_ALLOWED_VERSIONS);

        expect(streamConfig.getConfiguration(StreamConfig.SLATED_VERSIONS_KEY))
                .andReturn(TEST_SLATED_VERSIONS);

        replay(streamConfig);

        instance = new VersionServiceImpl(streamConfig);
        instance.afterPropertiesSet();
        verify(streamConfig);
    }

    @Test
    public void testAllowedVersion() throws IOException {
        assertTrue(instance.isAllowedVersion("v0_8"));
        assertTrue(instance.isAllowedVersion("v0_9"));
        assertTrue(instance.isAllowedVersion("v1_0"));
        assertTrue(instance.isAllowedVersion("v1_1"));
        assertFalse(instance.isAllowedVersion("v0_7"));
        assertFalse(instance.isAllowedVersion("v0_6"));
        assertFalse(instance.isAllowedVersion("v1_7"));
        assertFalse(instance.isAllowedVersion("v2_7"));
        assertFalse(instance.isAllowedVersion(""));
    }

    @Test
    public void testSlatedVersion() throws IOException {
        assertTrue(instance.isSlatedVersion("v0_8"));
        assertTrue(instance.isSlatedVersion("v0_9"));
        assertFalse(instance.isSlatedVersion("v1_0"));
        assertFalse(instance.isSlatedVersion("v1_1"));
        assertFalse(instance.isSlatedVersion("v0_0"));
        assertFalse(instance.isSlatedVersion("v0_7"));
        assertFalse(instance.isSlatedVersion(""));
    }

    @Test
    public void testRefresh() throws IOException {
        assertTrue(instance.isAllowedVersion("v1_1"));
        assertFalse(instance.isAllowedVersion("v1_2"));

        assertTrue(instance.isSlatedVersion("v0_9"));
        assertFalse(instance.isSlatedVersion("v1_0"));


        String newAllowedList = "v1_1, v1_2";
        String newSlateList = "v0_9, v1_0";
        reset(streamConfig);
        expect(streamConfig.getConfiguration(StreamConfig.ALLOWED_VERSIONS_KEY))
                .andReturn(newAllowedList);

        expect(streamConfig.getConfiguration(StreamConfig.SLATED_VERSIONS_KEY))
                .andReturn(newSlateList);
        replay(streamConfig);


        instance.onApplicationEvent(Events.newRefreshDataEvent(this));

        assertTrue(instance.isAllowedVersion("v1_1"));
        assertTrue(instance.isAllowedVersion("v1_2"));

        assertTrue(instance.isSlatedVersion("v0_9"));
        assertTrue(instance.isSlatedVersion("v1_0"));
    }
}
