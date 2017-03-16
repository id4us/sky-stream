package com.sky.mobile.skytvstream.interceptor;

import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.skytvstream.service.versions.VersionService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.ssmtv.oauth.oogway.OauthService;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.sky.mobile.skytvstream.utils.StreamingHeaders.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestHeadersInterceptor.TestConfig.class})
public class TestHeadersInterceptor {

    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AuthenticatedPerson mockPerson;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @Autowired
    private OauthService mockService;

    private MockMvc mockMvc;

    @Autowired
    private VersionService versionService;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    @DirtiesContext
    public void testGetHeaders() throws Exception {
        String expectedVersion = "v1_0";
        String expectedProvider = "GOO";
        String expectedModelId = "HTC1";
        String expectedClientId = "12345";
        this.mockMvc
                .perform(
                        get("/testAllHeaders")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(VERSION.toString(), expectedVersion)
                                .header(SUB_PROVIDER.toString(),
                                        expectedProvider)
                                .header(MODEL_ID.toString(), expectedModelId)
                                .header(CLIENT_ID.toString(), expectedClientId))

                .andExpect(status().isOk())
                .andExpect(
                        header().string(HTTP_HEADER_NAME_CONTENT_TYPE,
                                is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(content().string("testNoAnnotation"));

        assertNull(mockPerson.getEncryptedOauthToken());
        assertEquals(expectedVersion, currentRequest.get(VERSION));
        assertEquals(expectedProvider, currentRequest
                .get(SUB_PROVIDER));
        assertEquals(expectedModelId, currentRequest.get(MODEL_ID));
        assertEquals(expectedClientId, currentRequest.get(CLIENT_ID));

        // assertTrue(LOG.isEmpty());
    }

    @Test
    @DirtiesContext
    public void testGetHeaders_missingModelId() throws Exception {
        String expectedVersion = "v_10";
        this.mockMvc.perform(
                get("/testpartialHeaders")
                        .header(HTTP_HEADER_NAME_ACCEPT,
                                HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                        .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                        .header(VERSION.toString(), expectedVersion))

                .andExpect(status().isBadRequest());

    }

    @Test
    @DirtiesContext
    public void testGetHeaders_mandatory() throws Exception {
        String expectedVersion = "v1_0";
        String expectedProvider = "GOO";
        String expectedModelId = "HTC1";
        String expectedClientId = "12345";
        when(versionService.isAllowedVersion(expectedVersion)).thenReturn(true);
        this.mockMvc
                .perform(
                        get("/testHeaders")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(VERSION.toString(), expectedVersion)
                                .header(SUB_PROVIDER.toString(),
                                        expectedProvider)
                                .header(MODEL_ID.toString(), expectedModelId)
                                .header(CLIENT_ID.toString(), expectedClientId))

                .andExpect(status().isOk())
                .andExpect(
                        header().string(HTTP_HEADER_NAME_CONTENT_TYPE,
                                is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(content().string("testAnnotation"));

        assertNull(mockPerson.getEncryptedOauthToken());
        assertEquals(expectedVersion, currentRequest.get(VERSION));
        assertEquals(expectedProvider, currentRequest
                .get(SUB_PROVIDER));
        assertEquals(expectedModelId, currentRequest.get(MODEL_ID));
        assertEquals(expectedClientId, currentRequest.get(CLIENT_ID));

        // assertTrue(LOG.isEmpty());
    }

    @Test
    @DirtiesContext
    public void testUnsupportedVersion_incorrectVersion() throws Exception {
        String expectedVersion = "v1_1";
        when(versionService.isAllowedVersion(expectedVersion))
                .thenReturn(false);
        MvcResult resultSet = this.mockMvc
                .perform(
                        get("/testversion")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(VERSION.toString(), expectedVersion))
                .andExpect(status().isPreconditionFailed()).andReturn();
        String content = resultSet.getResponse().getErrorMessage();
        assertEquals("{\"code\":\"9420\",\"message\":\"Unsupported version\"}", content);
    }

    @Test
    @DirtiesContext
    public void testUnsupportedVersion_happy() throws Exception {

        String expectedVersion = "v1_0";
        when(versionService.isAllowedVersion(expectedVersion)).thenReturn(true);

        this.mockMvc
                .perform(
                        get("/testversion")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(VERSION.toString(), expectedVersion))

                .andExpect(status().isOk())
                .andExpect(content().string("testVersion"));

        assertNull(mockPerson.getEncryptedOauthToken());
        assertEquals(expectedVersion, currentRequest.get(VERSION));
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfig extends WebMvcConfigurerAdapter {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(getHeadersInterceptor()).addPathPatterns(
                    "/*");
        }

        @Bean
        public OauthService getOauthService() {
            return mock(OauthService.class);
        }

        @Bean
        public HeadersInterceptor getHeadersInterceptor() {
            return new HeadersInterceptor();
        }

        @Bean(name = "currentOauthUser")
        public AuthenticatedPerson getOauthPerson() {
            return new AuthenticatedPerson();
        }

        @Bean(name = "currentRequest")
        public Map<StreamingHeaders, String> getCurrentRequest() {
            HashMap<StreamingHeaders, String> request = new HashMap<>();
            return request;
        }


        @Bean
        public VersionService getVersionService() {
            return mock(VersionService.class);
        }

        @Controller
        public static class TestController {
            @RequestMapping(value = "/testAllHeaders", method = RequestMethod.GET)
            public
            @ResponseBody
            String test1() {
                return "testNoAnnotation";
            }

            @RequestMapping(value = "/testpartialHeaders", method = RequestMethod.GET)
            @HeadersRequired({MODEL_ID})
            public
            @ResponseBody
            String test2() {
                return "testNoAnnotation";
            }

            @RequestMapping(value = "/testversion", method = RequestMethod.GET)
            @HeadersRequired({VERSION})
            public
            @ResponseBody
            String testVersion() {
                return "testVersion";
            }

            @HeadersRequired({MODEL_ID, VERSION, CLIENT_ID, SUB_PROVIDER})
            @RequestMapping(value = "/testHeaders", method = RequestMethod.GET)
            public
            @ResponseBody
            String test3() {
                return "testAnnotation";
            }

        }

    }

}
