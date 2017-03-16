package com.sky.mobile.skytvstream.service.templates;

import com.google.common.base.Preconditions;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.io.IOException;

public class StaticFileService implements ContentProducer,
        ApplicationListener<RefreshDataEvent> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(StaticFileService.class);

    private final String key;
    private String content;
    private final StreamConfig streamConfig;

    public StaticFileService(StreamConfig streamConfig, String allChannelsKey)
            throws IOException {
        key = allChannelsKey;
        this.streamConfig = streamConfig;
        loadContent();
    }

    @Override
    public String getContent() {
        Preconditions.checkNotNull(content);
        return content;
    }

    private void loadContent() throws IOException {
        String result = streamConfig.getConfiguration(key);
        Preconditions.checkNotNull(result,
                "Unable to load static data from key: " + key);
        content = result;
    }

    @Override
    public void onApplicationEvent(RefreshDataEvent event) {
        try {
            loadContent();
        } catch (IOException e) {
            LOG.error("Ioexception trying to refresh static data " + key, e);
        }
    }

}
