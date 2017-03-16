package com.sky.mobile.skytvstream.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.web.utils.TimeProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.sky.mobile.skytvstream.service.templates.StaticFileService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestDiscoveryController.TestConfig.class, MockTimeProviderContext.class})
public class TestDiscoveryController {
	
    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_COUNTRY = "x-country";
    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";
    private static final String EXPECTED = "{sample json}";
    private static final String EXPECTED_STATUS = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta name=\"viewport\" content=\"initial-scale=1.0\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>Sky SportsTV - Service Status</title></head><body><div id=\"greycasing\"><h2>Sky Sports TV - Service Status</h2><div id=\"mainContent\" ><p><strong>Wednesday 20 May 2015 18:56</strong></p><p>Service Status: Normal</p></div></div></body></html>";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy HH:mm", Locale.ENGLISH);


	@Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    
    @Resource(name="discoveryService")
    private StaticFileService discoveryService;

	@Resource(name="streamConfig")
	private StreamConfig streamConfig;

	@Autowired
	private MockTimeProviderContext.MockTimeProvider timeProvider;

	@Before
	public void setUp() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		Date date = dateFormat.parse("Wednesday 20 May 2015 18:56");
		timeProvider.setMockTime(date.getTime());
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void testGetDiscovery() throws Exception {
		when(discoveryService.getContent()).thenReturn(EXPECTED);

        this.mockMvc.perform(get("/")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(content().string(EXPECTED));
	}

	@Test
	public void testAndroidGetStatus() throws Exception {
		this.mockMvc.perform(get("/android/service/status"))
				.andExpect(status().isOk())
				.andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is("text/html")))
				.andExpect(content().string("android date:Wednesday 20 May 2015 18:56, status:Normal"));
	}

	@Test
	public void testIosGetStatus() throws Exception {
		this.mockMvc.perform(get("/ios/service/status"))
				.andExpect(status().isOk())
				.andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is("text/html")))
				.andExpect(content().string("ios date:Wednesday 20 May 2015 18:56, status:Normal"));
	}

	@Configuration
	@EnableWebMvc
	public static class TestConfig extends WebMvcConfigurerAdapter {
		
		@Bean(name="version")
		public Properties mavenProperties() {
			return new Properties();
		}

		@Bean
		public DiscoveryController getController(
				@Qualifier("version") Properties versionProperties,
				@Qualifier("discoveryService") StaticFileService discoveryService,
				StreamConfig streamConfig,
				TimeProvider timeProvider
		) throws IOException {

			when(streamConfig.getConfiguration("com.sky.sstv.service.status.message")).thenReturn("Normal");
			when(streamConfig.getConfiguration("com.sky.sstv.service.status.android.template")).thenReturn("android date:{date}, status:{status}");
			when(streamConfig.getConfiguration("com.sky.sstv.service.status.ios.template")).thenReturn("ios date:{date}, status:{status}");

			return new DiscoveryController(
					versionProperties,
					discoveryService,
					streamConfig,
					timeProvider);
		}

		@Bean(name="discoveryService")
		public StaticFileService getDiscoveryService() {
			return mock(StaticFileService.class);
		}

		@Bean(name="streamConfig")
		public StreamConfig getStreamConfig() {
			return mock(StreamConfig.class);
		}
	}
}
