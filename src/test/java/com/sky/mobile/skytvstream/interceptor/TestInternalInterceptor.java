package com.sky.mobile.skytvstream.interceptor;

import com.sky.mobile.annotations.InternalOnly;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
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
@ContextConfiguration(classes = {TestInternalInterceptor.OauthInterceptorTestConfig.class})
public class TestInternalInterceptor {

    private static final String HTTP_HEADER_NAME_ACCEPT = "Accept";
    private static final String HTTP_HEADER_NAME_AUTHORISATION = "Authorization";
    private static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE = "application/json;charset=utf-8";
    private static final String HHTP_HEADER_X_FORWARED = "X-Forwarded-For";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    @DirtiesContext
    public void testNoAnnotation() throws Exception {
        this.mockMvc
                .perform(
                        get("/testNoAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isOk())
                .andExpect(
                        header().string(HTTP_HEADER_NAME_CONTENT_TYPE,
                                is(HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)))
                .andExpect(content().string("testNoAnnotation"));

    }

    @Test
    @DirtiesContext
    public void testAnnotationNoHeader() throws Exception {
        this.mockMvc
                .perform(
                        get("/testAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE))

                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DirtiesContext
    public void testAnnotationHeaderIncorrectNumberOFParams() throws Exception {
        this.mockMvc
                .perform(
                        get("/testAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTHORISATION,
                                        "Invalid"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DirtiesContext
    public void testAnnotationHeaderNoBasic() throws Exception {
        this.mockMvc
                .perform(
                        get("/testAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTHORISATION,
                                        "Invalid 123563456abc"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DirtiesContext
    public void testAnnotationAuthFailedAgainstOogway() throws Exception {
        String originIp = "123.0.0.1";
        String base64UserPassword = "ZXhwZWN0ZWR1c2VyOnRlc3QxMjM0";

        this.mockMvc
                .perform(
                        get("/testAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTHORISATION,
                                        "Basic " + base64UserPassword)
                                .header(HHTP_HEADER_X_FORWARED, originIp))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    public void testAnnotationHeaderAuthSuccessful() throws Exception {
        String originIp = "123.0.0.1";
        String base64UserPassword = "Y29uZmlnYWRtaW46YWRtMW5jbDFlbnQ=";

        this.mockMvc
                .perform(
                        get("/testAnnotation")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTHORISATION,
                                        "Basic " + base64UserPassword)
                                .header(HHTP_HEADER_X_FORWARED, originIp))
                .andExpect(status().isOk())
                .andExpect(content().string("testAnnotation"));
    }

    @Test
    @DirtiesContext
    public void testAnnotationHeaderAuthDisableOnProd() throws Exception {
        String originIp = "123.0.0.1";
        String base64UserPassword = "Y29uZmlnYWRtaW46YWRtMW5jbDFlbnQ=";
        System.setProperty("env.name", "prod");
        this.mockMvc
                .perform(
                        get("/testAnnotationNoProd")
                                .header(HTTP_HEADER_NAME_ACCEPT,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_CONTENT_TYPE,
                                        HTTP_CONTENT_TYPE_RESPONSE_HEADER_VALUE)
                                .header(HTTP_HEADER_NAME_AUTHORISATION,
                                        "Basic " + base64UserPassword)
                                .header(HHTP_HEADER_X_FORWARED, originIp))
                .andExpect(status().isNotFound());
    }


    @Configuration
    @EnableWebMvc
    public static class OauthInterceptorTestConfig extends
            WebMvcConfigurerAdapter {


        @Bean
        public MockEnvironment getEnvironment() {
            return new MockEnvironment();
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(getInternalInterceptor())
                    .addPathPatterns("/*");
        }

        ;

        @Bean
        public InternalInterceptor getInternalInterceptor() {
            return new InternalInterceptor();

        }


        @Controller
        public static class TestController {
            @RequestMapping(value = "/testNoAnnotation", method = RequestMethod.GET)
            public
            @ResponseBody
            String test1() {
                return "testNoAnnotation";
            }

            @RequestMapping(value = "/testAnnotation", method = RequestMethod.GET)
            @InternalOnly
            public
            @ResponseBody
            String test2() {
                return "testAnnotation";
            }

            @RequestMapping(value = "/testAnnotationNoProd", method = RequestMethod.GET)
            @InternalOnly(preventOnProd = true)
            public
            @ResponseBody
            String test3() {
                return "testAnnotation";
            }

        }

    }

}
