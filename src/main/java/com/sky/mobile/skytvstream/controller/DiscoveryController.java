package com.sky.mobile.skytvstream.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.web.utils.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;


@Controller
public class DiscoveryController {

    private Properties versionProperties;
    private StaticFileService discoveryService;
    private StreamConfig streamConfig;
    private TimeProvider timeProvider;

    private final static String ANDROID_STATUS_TEMPLATE_CONFIGURATION_KEY = "com.sky.sstv.service.status.android.template";
    private final static String IOS_STATUS_TEMPLATE_CONFIGURATION_KEY = "com.sky.sstv.service.status.ios.template";
    private final static String STATUS_MESSAGE_CONFIGURATION_KEY = "com.sky.sstv.service.status.message";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy HH:mm", Locale.ENGLISH);
    private final String buildProperties;
    private final String androidServiceStatusTemplate;
    private final String iosServiceStatusTemplate;

    private static final Logger LOG = LoggerFactory
            .getLogger(DiscoveryController.class);

    @Autowired
    public DiscoveryController(
            @Qualifier("version") Properties versionProperties,
            @Qualifier("discoveryService") StaticFileService discoveryService,
            StreamConfig streamConfig,
            TimeProvider timeProvider) throws IOException {
        this.discoveryService = discoveryService;
        this.timeProvider = timeProvider;
        this.versionProperties = versionProperties;
        this.streamConfig = streamConfig;
        buildProperties = getDisplayableBuildProperties();
        androidServiceStatusTemplate = streamConfig.getConfiguration(ANDROID_STATUS_TEMPLATE_CONFIGURATION_KEY);
        iosServiceStatusTemplate = streamConfig.getConfiguration(IOS_STATUS_TEMPLATE_CONFIGURATION_KEY);
    }

  	@CacheHeaders(value=PageCacheStrategy.CACHED, cacheMinuites=120)
	@RequestMapping(value="", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody String getDiscovery() {
		return discoveryService.getContent();
	}


    @CacheHeaders(value=PageCacheStrategy.CACHED, cacheMinuites=120)
    @RequestMapping(value="/discovery", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
    public @ResponseBody String getDiscovery2() {

        LOG.info("Test String for logging");
        return discoveryService.getContent();
    }

    @CacheHeaders(value=PageCacheStrategy.CACHED, cacheMinuites=3)
    @RequestMapping(value = "/android/service/status", method = RequestMethod.GET, produces="text/html")
    public @ResponseBody String getAndroidServiceStatus() throws IOException {
        return  getStatusContent(androidServiceStatusTemplate);
    }

    @CacheHeaders(value=PageCacheStrategy.CACHED, cacheMinuites=3)
    @RequestMapping(value = "/ios/service/status", method = RequestMethod.GET, produces="text/html")
    public @ResponseBody String getIOSServiceStatus() throws IOException {
        return  getStatusContent(iosServiceStatusTemplate);
    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object ping() {
        return buildProperties;
    }

    private String getDisplayableBuildProperties() {
        return "{\"project.name\":\"" + versionProperties.getProperty("project.name") + "\"\n ," +
                "\"jenkins.build.number\":\" " + versionProperties.getProperty("jenkins.build.number") + "\"\n ," +
                "\"jenkins.build.id\":\"" + versionProperties.getProperty("jenkins.build.id") + "\"\n ," +
                "\"jenkins.build.date\":\" " + versionProperties.getProperty("jenkins.build.date") + "\"\n ," +
                "\"svn.revision\":\"" + versionProperties.getProperty("svn.revision")+"\"}";
    }

    private String getStatusContent(String template) throws IOException {
        String now = dateFormat.format(timeProvider.getDate());
        return template.replace("{date}", now)
                .replace("{status}", streamConfig.getConfiguration(STATUS_MESSAGE_CONFIGURATION_KEY));
    }
}

