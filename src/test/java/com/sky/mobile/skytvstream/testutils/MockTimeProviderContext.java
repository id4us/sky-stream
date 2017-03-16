package com.sky.mobile.skytvstream.testutils;

import com.sky.web.utils.AbstractTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockTimeProviderContext {

    @Bean
    MockTimeProvider getMockTimeProvider() {
        return new MockTimeProvider();
    }

    public static class MockTimeProvider extends AbstractTimeProvider {

        private long mockTime = 0L;

        @Override
        public long getTimeMillis() {
            return mockTime;
        }

        public void setMockTime(long time) {
            mockTime = time;
        }
    }
}
