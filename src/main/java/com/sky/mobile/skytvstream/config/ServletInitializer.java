package com.sky.mobile.skytvstream.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import com.sky.mobile.ssmtv.oauth.oogway.OauthConfig;


public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(OauthConfig.class, Application.class);
	}

}
