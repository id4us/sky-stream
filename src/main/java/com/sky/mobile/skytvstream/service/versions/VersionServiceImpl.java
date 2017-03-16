package com.sky.mobile.skytvstream.service.versions;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class VersionServiceImpl implements VersionService, InitializingBean,
        ApplicationListener<RefreshDataEvent> {

    private static final Logger LOG = LoggerFactory
            .getLogger(VersionServiceImpl.class);

    private StreamConfig streamConfig;

    private final Set<String> allowedVersions = new HashSet<>();
    private final Set<String> slatedVersions = new HashSet<>();

    @Autowired
    public VersionServiceImpl(StreamConfig streamConfig) {
        this.streamConfig = streamConfig;
    }

    @Override
    public boolean isAllowedVersion(String version) {
        return allowedVersions != null? allowedVersions.contains(version.toLowerCase()): false;
    }

    @Override
    public boolean isSlatedVersion(String version) {
        return slatedVersions != null? slatedVersions.contains(version.toLowerCase()): false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadData();
    }

    @Override
    public void onApplicationEvent(RefreshDataEvent event) {
        try {
            loadData();
        } catch (IOException e) {
            LOG.error("Unable to load allowed versions", e);
        }

    }

    private void loadData() throws IOException {
        loadValues(StreamConfig.ALLOWED_VERSIONS_KEY, allowedVersions);
        loadValues(StreamConfig.SLATED_VERSIONS_KEY, slatedVersions);
    }

    private void loadValues(String key, Set<String> dataset) throws IOException {
        final String item = StringUtils.trimToNull(
                streamConfig.getConfiguration(key));
        if (item == null) {
            LOG.error("Empty or unknown data for key : " + key);
        } else {
            final String[] values = item.split(",");
            dataset.clear();
            int i = 0;
            for (String s : values) {
                String value = StringUtils.trimToEmpty(s);
                if (StringUtils.isNotBlank(s)) {
                    dataset.add(value.toLowerCase());
                    i++;
                }
            }
            LOG.info("Loaded " + i + " items for Key " + key + " from value \'" + item + '\'');
        }

    }

}
