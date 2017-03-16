package com.sky.mobile.skytvstream.config;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDbConfig.class,
        TestStreamConfigImpl.TestConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup(value = {"testdata.xml"}, type = DatabaseOperation.REFRESH)
public class TestStreamConfigImpl {

    private JdbcTemplate jdbcTemplate;

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Autowired
    private StreamConfigImpl configImpl;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testDatabaseIsSetUp() {
        assertTrue("Create data sql here", jdbcTemplate.queryForLong("select count(*) from config_stream") >= 1);
    }


    @Test
    public void testGetFile() throws IOException {
        String expected = "1wdsdasf213231w qweqwe qwqwerew";
        assertEquals(expected, configImpl.getConfiguration("test.key1"));
    }

    @Test
    public void testAssertEscaped() throws IOException {
        String actual = configImpl.getConfiguration("test.com.sky.sstv.streaming.products");
        assertFalse(StringUtils.contains(actual, "\\r"));
        assertFalse(StringUtils.contains(actual, "\\n"));
    }


    public static class TestConfig {
       @Bean
        public StreamConfigImpl getStreamConfigImpl(DataSource dataSource) {
            return new StreamConfigImpl(dataSource);
        }
    }


}
