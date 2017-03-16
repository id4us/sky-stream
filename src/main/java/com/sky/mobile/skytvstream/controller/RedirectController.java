package com.sky.mobile.skytvstream.controller;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.PageCacheStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

@Controller
public class RedirectController{
	private static final Logger LOG = LoggerFactory.getLogger(RedirectController.class);
	
	@Value("${stream.playlist.normal.suffix}")
	private String normalSuffix;

	@Value("${stream.playlist.degraded.suffix}")
	private String degradedPlaylistSuffix;
	

	private final String[] degradedPlatformList;
	
	@Autowired
	public RedirectController(@Value("${stream.playlist.degraded.platforms}") String degradedPlatforms) {
		degradedPlatformList = parseDegradedPlatforms(degradedPlatforms);
	}

	@CacheHeaders(PageCacheStrategy.NONE)
    @RequestMapping(value = "/redirect/content", method = RequestMethod.GET)
	public void redirectForDegrade(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String contentUrl = request.getParameter("content");
		String platformHeader = request.getHeader("User-Agent");
		boolean degradable = isPlatformDegradable(platformHeader);
		String destination = degradable ? makeDegradableUrl(contentUrl) : contentUrl;
		response.sendRedirect(destination);		
	}
	
	private String makeDegradableUrl(String contentUrl) {
		return StringUtils.replace(contentUrl, normalSuffix, degradedPlaylistSuffix);
	}

	private String[] parseDegradedPlatforms(final String degradedPlatforms) {
		final Iterator<String> splitter = Splitter.on(',')
				.trimResults()
				.omitEmptyStrings()
				.split(degradedPlatforms)
				.iterator();
		if (splitter.hasNext()) {
			String[] result = Lists.newArrayList(splitter).toArray(new String[]{});
			return result;
		} else {
			return new String[]{};
		}
	}	
	
	public boolean isPlatformDegradable(String fromHeader){
        LOG.debug("header:{}", fromHeader);

		for(String degradedPlatform : degradedPlatformList){
            LOG.debug("degradedPlatform:{}", degradedPlatform);
			if(StringUtils.containsIgnoreCase(fromHeader, degradedPlatform)){
				return true;
			}
		}
		return false;
	}
}
