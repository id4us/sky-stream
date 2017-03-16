package com.sky.web.utils;

import com.sky.mobile.skytvstream.utils.ErrorResponse;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class HTTPUtils {

    public static String HTTP_AUTHORIZATION_HEADER = "Authorization";
    public static String MAP_KEY_USERNAME = "username";
    public static String MAP_KEY_PASSWORD = "password";

    public static int COOKIE_CONST_IGNORE_MAX_AGE = -1;


    public static String getOriginIp(HttpServletRequest request) {
        String xForwardedIp = request.getHeader("X-Forwarded-For");
        return StringUtils.isBlank(xForwardedIp) ? request
                .getHeader("Remote_Addr") : xForwardedIp;
    }

    public static String getErrorMessage(HttpServletResponse response, int httpCode, String errorCode, String errorMessage){
        response.setStatus(httpCode);
        return new ErrorResponse(errorCode, errorMessage).getFormattedErrorResponse();
    }

    public static String getRequestHeader(HttpServletRequest req, String headerName) {
        if (req == null)
            return null;

        String value = req.getHeader(headerName == null ? "" : headerName);

        if (value != null) {
            String values[] = value.split("\r");
            StringBuffer collated = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                collated.append(("" + values[i]).trim() + " ");
            }
            return collated.toString().trim();
        }

        return value;
    }

    public static String getBasicAuthorizationHeaderFromRequest(HttpServletRequest req) {
        if (req == null)
            return null;

        String value = getRequestHeader(req, HTTP_AUTHORIZATION_HEADER);

        if (value == null || value.trim().length() < 1)
            return null;

        if (value.length() == "basic".length())
            return null;

        int index = value.toLowerCase().indexOf("basic");
        if (index < 0)
            return null;

        return value.substring(index + "basic".length() + 1).trim();
    }

    public static HashMap<String, String> getUsernameAndPasswordMapFromDecodedBasicAuthorizationHeader(String decodedHeader) {

        HashMap<String, String> map = new HashMap<>();
        String[] parts;

        if ((decodedHeader == null)
                || (decodedHeader.trim().length() < 1))
            return null;

        parts = decodedHeader.split(":");

        if (parts == null)
            return null;

        if (parts.length < 2)
            return null;

        map.put(MAP_KEY_USERNAME, parts[0]);
        map.put(MAP_KEY_PASSWORD, parts[1]);

        return map;
    }

    /**
     * Creates a Cookie with a token value and not a quoted-string.
     *
     * Akamai Edge Authorization only supports (non-quoted) token values and
     * will reject a cookie with a quoted value.
     *
     * This means the SDK can not be used to create the Cookie as a javax.servlet.http.Cookie object
     * and can not be set using javax.servlet.http.HttpServletResponse.addCookie().
     *
     * Instead, a Set-Cookie header must be set with a cookie token value that is dynamically constructed from the
     * params.
     *
     * Please see the HTTP 1.1 RFC Section 4.1 http://www.ietf.org/rfc/rfc2109 for a detailed description.
     *
     * http://www.ietf.org/rfc/rfc2068.txt (this specifies a quoted-string and token, both equally valid values)
     *
     * Akamai Edge Auth require a cookie in the following format:
     *
     * "skytvp=expires=1252422291~access=/*~md5=14aa75e6056b4585d0026c81ad3a9032; Version=1; Domain=.mobile-tv.sky.com; Path=/";
     *
     * Note: The name/value pair are quoted and not just the value. This is significant for Akamai.
     *
     * @param res HttpServletResponse that a Set-Cookie header will be set on
     * @param name String name of the cookie
     * @param value token value of the cookie
     * @param comment
     * @param domain
     * @param maxAge The default behavior (when COOKIE_CONST_IGNORE_MAX_AGE is supplied) is to discard the cookie when the useragent exits.
     * @param path
     * @param secure
     * @param version
     */
    public static void addCookieHeader(HttpServletResponse res, String name, String value, String comment, String domain, int maxAge, String path, boolean secure, int version) {

        if (res != null && name != null) {

            String cookieData = name + "=" + value + "; Version=" + version +"; Domain=" + domain + "; Path=" + path;

            if (comment != null) {
                cookieData = cookieData + "; Comment=" + comment;
            }

            if (maxAge != HTTPUtils.COOKIE_CONST_IGNORE_MAX_AGE) {
                cookieData = cookieData + "; Max-Age=" + maxAge;
            }

            if (secure) {
                cookieData = cookieData + "; Secure";
            }


            res.addHeader("Set-Cookie", cookieData);
        }
    }
}
