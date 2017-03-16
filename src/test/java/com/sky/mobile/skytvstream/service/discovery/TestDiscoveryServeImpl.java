package com.sky.mobile.skytvstream.service.discovery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDiscoveryServeImpl.TestConfig.class})

public class TestDiscoveryServeImpl {

	private static final String TEST_DISCOVERY = "src/test/resources/testdiscovery/discovery.json";

	@Autowired
	StaticFileService service;
	
	@Test
	public void testGetDiscoveryContent() throws IOException {
		
		String expected = fileFromString(TEST_DISCOVERY);
		assertEquals(expected, service.getContent());
	}

	private static String fileFromString(String fname) throws IOException {
		File f = new File(fname);
		return FileUtils.readFileToString(f);
	}
	
	@Configurable
	public static class TestConfig{
				
		@Bean
		public StreamConfig getStreamConfig() throws IOException {
			StreamConfig streamConfig =  mock(StreamConfig.class);
			when(streamConfig.getConfiguration(StreamConfig.DISCOVERY_KEY))
			  .thenReturn(fileFromString(TEST_DISCOVERY));
			return streamConfig;
		}
		
		@Bean
		public StaticFileService getStaticFileService(StreamConfig streamConfig) throws IOException {
			 StaticFileService service = new StaticFileService(streamConfig, StreamConfig.DISCOVERY_KEY);
			 return service;
		}
	}
}
