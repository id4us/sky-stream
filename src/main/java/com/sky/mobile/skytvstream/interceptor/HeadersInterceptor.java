package com.sky.mobile.skytvstream.interceptor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.skytvstream.service.versions.VersionService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import com.sky.web.utils.AnnotationHelper;

public class HeadersInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(HeadersInterceptor.class);

	@Autowired
	private AuthenticatedPerson person;

	@Autowired
	private VersionService versionService;
	
	@Resource(name="currentRequest")
	private Map<StreamingHeaders, String> headers;

	@Override
	public boolean preHandle(final HttpServletRequest request,
			final HttpServletResponse response, final Object object)
			throws IOException, ServletException {

		Enumeration<String> headerNames = request.getHeaderNames();


		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			StreamingHeaders name = StreamingHeaders.fromHeaderName(headerName);
			String value;
			if ((name != null)
					&& StringUtils.isNotBlank(value = request.getHeader(name
							.toString()))) {
				headers.put(name, value);
			}
		}

		HandlerMethod handler = (HandlerMethod) object;
		HeadersRequired requiredHeaders = AnnotationHelper
				.getAnnotationFromHandler(handler, HeadersRequired.class);

		if (requiredHeaders != null) {
			for (StreamingHeaders e : requiredHeaders.value()) {
				if (e.equals(StreamingHeaders.VERSION)) {
					final String userVersion = headers
							.get(StreamingHeaders.VERSION);
					if (!versionService.isAllowedVersion(userVersion)) {
						String msg = "Unsupported version " + userVersion;
						LOG.info(msg);
						response.sendError(HttpStatus.PRECONDITION_FAILED.value(),
                                "{\"code\":\"9420\",\"message\":\"Unsupported version\"}");
						return false;
					}
				} else if (!headers.keySet().contains(e)) {
					String msg = "9400: Missing Headers: " + e.toString();
					LOG.info(msg);
					response.sendError(HttpStatus.BAD_REQUEST.value(),
                            "{\"code\":\"9400\",\"message\":\"Missing Headers\"}");
					return false;
				}
			}
		}

		return true;
	}

}
