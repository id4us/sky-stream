package com.sky.mobile.skytvstream.interceptor;

import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sky.mobile.annotations.InternalOnly;
import com.sky.web.utils.AnnotationHelper;
import com.sky.web.utils.HTTPUtils;

public class InternalInterceptor extends HandlerInterceptorAdapter {
	private static final Logger LOG = LoggerFactory
			.getLogger(InternalInterceptor.class);

	
	@Resource
	private Environment environment;
	

	@Override
	public boolean preHandle(final HttpServletRequest request,
			final HttpServletResponse response, final Object object)
			throws IOException, ServletException {

		InternalOnly annotation = AnnotationHelper.getAnnotationFromObject(
				object, InternalOnly.class);

		if (annotation == null) {
			return true;
		}

		if (annotation.preventOnProd() && "prod".equalsIgnoreCase(environment.getProperty("env.name", ""))) {
			LOG.error("ALERT: Attempt to view internal pages on production"
					+ ". Origin: "
					+ getOriginIp(request));
			sendError(response);
			return false;			
		}
		
		
		HashMap<String, String> userCredentialsMap = AuthHelper
				.getCredentialsFromHeader(request);
		if (userCredentialsMap == null) {
			LOG.error("ALERT: Attempt to view Internal pages with no Authentication");
			sendError(response);
			return false;
		}
		String username = userCredentialsMap.get(HTTPUtils.MAP_KEY_USERNAME);
		String password = userCredentialsMap.get(HTTPUtils.MAP_KEY_PASSWORD);

		if (StringUtils.equals(username, "configadmin")
				&& StringUtils.equals(password, "adm1ncl1ent")) {
			return true;
		}
		LOG.error("ALERT: Attempt to view Internal pages with incorrectAuthentication: Username = "
				+ username
				+ ", Password = "
				+ password
				+ ". Origin: "
				+ getOriginIp(request));
		sendError(response);
		return false;
	}

	private void sendError(HttpServletResponse response) throws IOException {
		response.sendError(404, "Not Found");
	}

	private String getOriginIp(HttpServletRequest request) {
		String xForwardedIp = request.getHeader("X-Forwarded-For");
		return StringUtils.isBlank(xForwardedIp) ? request
				.getHeader("Remote_Addr") : xForwardedIp;
	}	
}
