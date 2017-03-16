package com.sky.mobile.skytvstream.interceptor;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext;
import com.sky.mobile.skytvstream.testutils.MockTimeProviderContext.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {MockTimeProviderContext.class,
        TestCacheHeaderInterceptor.TestConfig.class})
public class TestCacheHeaderInterceptor {

    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";
    private static final String HTTP_CACHE_CONTROL = "Cache-Control";

    private static final DateTime TESTDATE = new DateTime(2010, 11, 23, 13, 00, DateTimeZone.UTC);
    private static final String TEST_DATE_STRING = "Tue, 23 Nov 2010 13:00:00 +00:00";
    private static final String HTTP_PRAGMA = "pragma";
    private static final String HTTP_EXPIRES = "Expires";
    private static final String HTTP_DATE = "Date";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockTimeProvider timeProvider;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        timeProvider.setMockTime(TESTDATE.getMillis());
    }

    @Ignore
    @Test
    public void noAnnotationTest() throws Exception {
        this.mockMvc.perform(get("/testNoAnnotation")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(header().string(HTTP_CACHE_CONTROL, is((String) null)))
                .andExpect(header().string(HTTP_PRAGMA, is((String) null)))
                .andExpect(header().string(HTTP_EXPIRES, is((String) null)))
                .andExpect(header().string(HTTP_DATE, is((String) null)))
                .andExpect(content().string("testNoAnnotation"));
    }


    @Ignore
    @Test
    public void defaultCacheAnnotationTest() throws Exception {
        String expectedExpires = "Tue, 23 Nov 2010 13:01:00 +00:00";
        String expectedcacheControl = "public, max-age=60, s-maxage=60";

        this.mockMvc.perform(get("/testdefaultannotation")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(header().string(HTTP_PRAGMA, is((String) null)))
                .andExpect(header().string(HTTP_CACHE_CONTROL, is(expectedcacheControl)))
                .andExpect(header().string(HTTP_EXPIRES, is(expectedExpires)))
                .andExpect(header().string(HTTP_DATE, is(TEST_DATE_STRING)))
                .andExpect(content().string("testdefaultannotation"));
    }

    @Ignore
    @Test
    public void cache20MinsAnnotationTest() throws Exception {
        String expectedExpires = "Tue, 23 Nov 2010 13:20:00 +00:00";
        String expectedcacheControl = "public, max-age=1200, s-maxage=1200";

        this.mockMvc.perform(get("/test20minscache")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(header().string(HTTP_PRAGMA, is((String) null)))
                .andExpect(header().string(HTTP_CACHE_CONTROL, is(expectedcacheControl)))
                .andExpect(header().string(HTTP_EXPIRES, is(expectedExpires)))
                .andExpect(header().string(HTTP_DATE, is(TEST_DATE_STRING)))
                .andExpect(content().string("test20minscache"));
    }

    @Ignore
    @Test
    public void private2minCacheTest() throws Exception {
        String expectedExpires = "Tue, 23 Nov 2010 13:02:00 +00:00";
        String expectedcacheControl = "private, max-age=120";

        this.mockMvc.perform(get("/private2mincache")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(header().string(HTTP_PRAGMA, is((String) null)))
                .andExpect(header().string(HTTP_CACHE_CONTROL, is(expectedcacheControl)))
                .andExpect(header().string(HTTP_EXPIRES, is(expectedExpires)))
                .andExpect(header().string(HTTP_DATE, is(TEST_DATE_STRING)))
                .andExpect(content().string("private2mincache"));
    }


    @Ignore
    @Test
    public void noCacheTest() throws Exception {
        String expectedExpires = "Tue, 23 Nov 2010 13:00:00 +00:00";
        String expectedcacheControl = "max-age=0, no-cache, no-store";
        String expectedPragma = "no-cache";

        this.mockMvc.perform(get("/nocache")
                .header(HTTP_HEADER_NAME_ACCEPT, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                .header(HTTP_HEADER_NAME_CONTENT_TYPE, HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(header().string(HTTP_HEADER_NAME_CONTENT_TYPE, is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(header().string(HTTP_PRAGMA, is(expectedPragma)))
                .andExpect(header().string(HTTP_CACHE_CONTROL, is(expectedcacheControl)))
                .andExpect(header().string(HTTP_EXPIRES, is(expectedExpires)))
                .andExpect(header().string(HTTP_DATE, is(TEST_DATE_STRING)))
                .andExpect(content().string("nocache"));
    }


    @Configuration
    @EnableWebMvc
    public static class TestConfig extends WebMvcConfigurerAdapter {

        @Bean
        public CacheHeaderInterceptor getCacheHeaderInterceptor() {
            return new CacheHeaderInterceptor();
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(getCacheHeaderInterceptor()).addPathPatterns("/*");
        }

        @Controller
        public static class TestController {
            @RequestMapping(value = "/testNoAnnotation", method = RequestMethod.GET)
            public
            @ResponseBody
            String test1() {
                return "testNoAnnotation";
            }

            @RequestMapping(value = "/testdefaultannotation", method = RequestMethod.GET)
            @CacheHeaders
            public
            @ResponseBody
            String test2() {
                return "testdefaultannotation";
            }

            @RequestMapping(value = "/test20minscache", method = RequestMethod.GET)
            @CacheHeaders(cacheMinuites = 20)
            public
            @ResponseBody
            String test3() {
                return "test20minscache";
            }

            @RequestMapping(value = "/private2mincache", method = RequestMethod.GET)
            @CacheHeaders(value = PageCacheStrategy.PRIVATE, cacheMinuites = 2)
            public
            @ResponseBody
            String test4() {
                return "private2mincache";
            }

            @RequestMapping(value = "/nocache", method = RequestMethod.GET)
            @CacheHeaders(value = PageCacheStrategy.NONE)
            public
            @ResponseBody
            String test5() {
                return "nocache";
            }
        }

    }

}
