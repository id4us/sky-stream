package com.sky.mobile.skytvstream.interceptor;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;


import org.apache.tomcat.util.codec.binary.Base64;

import com.sky.web.utils.HTTPUtils;

public class AuthHelper {

	public static HashMap<String, String> getCredentialsFromHeader(
			HttpServletRequest request) {
		if (request == null)
			return null;
		
		String authorizationHeader = HTTPUtils.getBasicAuthorizationHeaderFromRequest(request);
    	
    	return HTTPUtils.getUsernameAndPasswordMapFromDecodedBasicAuthorizationHeader(base64DecodeString( authorizationHeader ));	
	}
	protected static String base64DecodeString(String toBeDecoded) {
		return (toBeDecoded == null ? null : new String(Base64.decodeBase64(toBeDecoded)));
	}
}
