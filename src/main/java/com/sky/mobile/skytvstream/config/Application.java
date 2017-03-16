package com.sky.mobile.skytvstream.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.sky.web.utils.Country;
import com.sky.web.utils.CountryImpl;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.sky.mobile.skytvstream.interceptor.CacheHeaderInterceptor;
import com.sky.mobile.skytvstream.interceptor.HeadersInterceptor;
import com.sky.mobile.skytvstream.interceptor.InternalInterceptor;
import com.sky.mobile.skytvstream.service.templates.StaticFileService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.ssmtv.oauth.interceptor.OauthInterceptor;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import com.sky.web.utils.SystemTimeProviderImpl;
import com.sky.web.utils.TimeProvider;

@Configuration
@ComponentScan(basePackages = "com.sky.mobile.skytvstream")
@EnableAutoConfiguration
@EnableWebMvc
public class Application  extends WebMvcConfigurerAdapter {

	@Bean
	public CacheHeaderInterceptor cacheHeaderInterceptor() {
		return new CacheHeaderInterceptor();
	}

	@Bean
	public InternalInterceptor internalInterceptor() {
		return new InternalInterceptor();
	}

	@Bean
	public OauthInterceptor oauthInterceptor() {
		return new OauthInterceptor();
	}

	@Bean
	public HeadersInterceptor headersInterceptor() {
		return new HeadersInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(cacheHeaderInterceptor());
		registry.addInterceptor(internalInterceptor());
		registry.addInterceptor(headersInterceptor());
		registry.addInterceptor(oauthInterceptor());
	}

	@Value("${memcache.server.nodes}")
	private String memcachedHosts;

	@Bean(name = "timeProvider")
	TimeProvider getTimeProvider() {
		return new SystemTimeProviderImpl();
	}

	@Bean(name = "currentAuthUser")
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AuthenticatedPerson getOauthPerson() {
		return new AuthenticatedPerson();
	}

	@Bean(name = "currentRequest")
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public Map<StreamingHeaders,String> getCurrentRequest() {
		return new HashMap<>();
	}

	@Bean(name = "countryCode")
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public Country getCountry() {
		return new CountryImpl(getCurrentRequest().get(StreamingHeaders.COUNTRY_ID));
	}


	@Bean(name = "discoveryService")
	public StaticFileService getDiscoveryService(StreamConfig config) throws IOException {
		StaticFileService discoveryService = new StaticFileService(config,
				StreamConfig.DISCOVERY_KEY);
		return discoveryService;
	}

	@Bean(name = "allChannelsService")
	public StaticFileService getAllChannelsService(StreamConfig config) throws IOException {
		StaticFileService allChannelsService = new StaticFileService(config,
				StreamConfig.ALL_CHANNELS_KEY);
		return allChannelsService;
	}

	@Bean(name = "allChannelsServiceRoi")
	public StaticFileService getAllChannelsServiceRoi(StreamConfig config) throws IOException {
		StaticFileService allChannelsServiceRoi = new StaticFileService(config,
				StreamConfig.ALL_CHANNELS_KEY_ROI);
		return allChannelsServiceRoi;
	}

    @Bean(name = "packageList")
    public StaticFileService getPackageList(StreamConfig config) throws IOException {
        StaticFileService packageService = new StaticFileService(config,
                StreamConfig.PACKAGE_LIST);
        return packageService;
    }

    @Bean(name = "packageListRoi")
    public StaticFileService getPackageListRoi(StreamConfig config) throws IOException {
        StaticFileService packageService = new StaticFileService(config,
                StreamConfig.PACKAGE_LIST_ROI);
        return packageService;
    }

	@Bean
	public MemcachedClient getMemcacheInstace() throws IOException {
		return new MemcachedClient(AddrUtil.getAddresses(memcachedHosts));
    }
	
	public static Properties configProperties() throws IOException {
		String externalConfig = System.getProperty("file.environment.conf");
		Resource resources = new FileSystemResource(externalConfig);
		Properties props = new Properties();
		props.load(resources.getInputStream());
		return props;
	}

	@Bean
	public DataSource dataSource() {
		JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
		DataSource dataSource = dataSourceLookup
				.getDataSource("java:comp/env/jdbc/sstv_stream");
		return dataSource;
	}
	
	@Bean
	public Properties version() throws IOException {
		Resource resources = new ClassPathResource("version.properties");
		Properties props = new Properties();
		props.load(resources.getInputStream());
		return props;		
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
			throws IOException {
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		pspc.setProperties(configProperties());
		pspc.setIgnoreUnresolvablePlaceholders(true);
		return pspc;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
