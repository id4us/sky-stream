package com.sky.mobile.skytvstream.controller.internal;


import com.sky.mobile.annotations.InternalOnly;
import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@InternalOnly
@RestController
@RequestMapping("/system")
public class TokenController {

    @Autowired
    private OauthTokenTranslator oauthTokenTranslator;

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getToken(final @RequestHeader("x-oauth") String authorizationHeader,
                           final @RequestHeader("x-client-id") String clientIdHeader) throws GeneralSecurityException, UnsupportedEncodingException {
        return "{\"stream_token\": \"" + oauthTokenTranslator.oauthEncrypt(authorizationHeader, clientIdHeader) + "\"}";
    }
}
