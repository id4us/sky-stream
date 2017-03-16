package com.sky.mobile.skytvstream.service.system;

import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestRefreshService.TestConfig.class})
public class TestRefreshService {

    @Resource
    TestEventListener listener;

    @Resource
    RefreshService service;

    @Test
    @DirtiesContext
    public void testRefresh() {
        assertFalse(listener.wasCalled());
        service.refreshData();
        assertTrue(listener.wasCalled());
    }

    @Configurable
    public static class TestConfig {

        @Bean
        public TestEventListener eventListener() {
            return new TestEventListener();
        }

        @Bean
        public RefreshService getRefreshService() {
            return new RefreshService();
        }

    }

    public static class TestEventListener implements
            ApplicationListener<RefreshDataEvent> {

        public RefreshDataEvent event = null;

        @Override
        public void onApplicationEvent(RefreshDataEvent event) {
            this.event = event;
        }

        public boolean wasCalled() {
            return event != null;
        }

    }

}
