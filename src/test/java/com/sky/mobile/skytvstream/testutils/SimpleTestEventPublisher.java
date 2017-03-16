package com.sky.mobile.skytvstream.testutils;

import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

public class SimpleTestEventPublisher implements ApplicationEventPublisher, ApplicationContextAware {

    private ApplicationContext ctx;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void publishEvent(ApplicationEvent event) {
        for (Entry<String, ApplicationListener> e : ctx.getBeansOfType(ApplicationListener.class).entrySet()) {
            e.getValue().onApplicationEvent(event);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.ctx = applicationContext;
    }

}
