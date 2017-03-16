package com.sky.mobile.skytvstream.interceptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.web.utils.AnnotationHelper;
import com.sky.web.utils.TimeProvider;

public class CacheHeaderInterceptor extends HandlerInterceptorAdapter {

    private static final String NO_CACHE_CONTROL = "max-age=0, no-cache, no-store";
    private static final String HTTP_PRAGMA = "Pragma";
    private static final String HTTP_EXPIRES = "Expires";
    private static final String HTTP_CACHE_CONTROL = "Cache-Control";
    private static final String HTTP_DATE = "Date";
    private static final String PUBLIC_CACHE_FORMAT = "public, max-age=%d, s-maxage=%d";
    private static final String PRIVATE_CACHE_FORMAT = "private, max-age=%d";

    public static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

    @Autowired
    private TimeProvider timeProvider;

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler) throws IOException, ServletException {

        CacheHeaders annotation = AnnotationHelper.getAnnotationFromObject(handler, CacheHeaders.class);

        if (annotation == null) {
            return true;
        }

        switch (annotation.value()) {
            case NONE:
                doNoCache(response);
                break;

            case CACHED:
                doCached(annotation.cacheMinuites(), response);
                break;

            case PRIVATE:
                doPrivate(annotation.cacheMinuites(), response);
                break;

        }
        response.addHeader(HTTP_DATE, DATE_FMT.print(timeProvider.getDateTime().withZone(DateTimeZone.UTC)));
        return true;
    }

    private void doPrivate(int cacheMinuites, HttpServletResponse response) {
        long cacheSeconds = cacheMinuites * 60L;
        String cacheControlValue = String.format(PRIVATE_CACHE_FORMAT, cacheSeconds);
        response.addHeader(HTTP_CACHE_CONTROL, cacheControlValue);

        addExpiryTimeHeader(cacheMinuites, response);
    }

    private void doCached(int cacheMinuites, HttpServletResponse response) {
        long cacheSeconds = cacheMinuites * 60L;
        String cacheControlValue = String.format(PUBLIC_CACHE_FORMAT, cacheSeconds, cacheSeconds);
        response.addHeader(HTTP_CACHE_CONTROL, cacheControlValue);

        addExpiryTimeHeader(cacheMinuites, response);
    }

    private void doNoCache(HttpServletResponse response) {
        response.addHeader(HTTP_CACHE_CONTROL, NO_CACHE_CONTROL);

        response.addHeader(HTTP_PRAGMA, "no-cache");
        response.addHeader(HTTP_EXPIRES, DATE_FMT.print(timeProvider.getDateTime().withZone(DateTimeZone.UTC)));
    }

    private void addExpiryTimeHeader(int cacheMinuites,
                                     HttpServletResponse response) {
        DateTime expiresTime = timeProvider.getDateTime().withZone(DateTimeZone.UTC).plusMinutes(cacheMinuites);
        response.addHeader(HTTP_EXPIRES, DATE_FMT.print(expiresTime));
    }

}
