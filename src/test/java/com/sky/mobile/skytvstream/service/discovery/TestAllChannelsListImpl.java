package com.sky.mobile.skytvstream.service.discovery;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.event.Events;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;
import com.sky.mobile.skytvstream.testutils.SimpleTestEventPublisher;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAllChannelsListImpl.TestConfig.class})

public class TestAllChannelsListImpl {

    private static final String TEST_CHANNELS = "src/test/resources/testdiscovery/all-channels.json";
    private static final String TEST_CHANNELSALT = "{alt}";

    @Autowired
    StaticFileService service;

    @Autowired
    private StreamConfig streamConfig;

    @Autowired
    private SimpleTestEventPublisher publisher;

    @Test
    @DirtiesContext
    public void testGetDiscoveryContent() throws IOException {

        String expected = fileFromString(TEST_CHANNELS);
        assertEquals(expected, service.getContent());
    }

    @Test
    @DirtiesContext
    public void testGetDiscoveryContent_reload() throws IOException {

        String firstexpected = fileFromString(TEST_CHANNELS);
        String firstResult = service.getContent();
        assertEquals(firstexpected, firstResult);

        when(streamConfig.getConfiguration(StreamConfig.ALL_CHANNELS_KEY))
                .thenReturn(TEST_CHANNELSALT);

        publisher.publishEvent(Events.newRefreshDataEvent(this));

        String secondexpected = TEST_CHANNELSALT;
        String secondresult = service.getContent();
        assertEquals(secondexpected, secondresult);

    }


    private static String fileFromString(String fname) throws IOException {
        File f = new File(fname);
        return FileUtils.readFileToString(f);
    }

    @Configurable
    public static class TestConfig {

        @Autowired
        ApplicationContext ctx;

        @Bean
        public SimpleTestEventPublisher getPublisher() {
            SimpleTestEventPublisher publisher = new SimpleTestEventPublisher();
            publisher.setApplicationContext(ctx);
            return publisher;
        }

        @Bean
        public StreamConfig getStreamConfig() throws IOException {
            StreamConfig streamConfig = mock(StreamConfig.class);
            when(streamConfig.getConfiguration(StreamConfig.ALL_CHANNELS_KEY))
                    .thenReturn(fileFromString(TEST_CHANNELS));
            return streamConfig;
        }

        @Bean
        StaticFileService getDiscoveryService(StreamConfig streamConfig) throws IOException {
            StaticFileService service = new StaticFileService(streamConfig, StreamConfig.ALL_CHANNELS_KEY);
            return service;
        }
    }
}
