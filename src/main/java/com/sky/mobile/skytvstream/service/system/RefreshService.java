package com.sky.mobile.skytvstream.service.system;

import com.sky.mobile.skytvstream.event.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "cis-sstv-streaming:name=configManager", description = "Config Manager")
public class RefreshService implements ApplicationEventPublisherAware {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshService.class);

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @ManagedOperation
    public void refreshData() {
        LOG.info("Refreshing configuration data: starting...");
        publisher.publishEvent(Events.newRefreshDataEvent(this));
        LOG.info("Refreshing configuration data: complete");
    }

}
