package com.sky.mobile.skytvstream.controller;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.domain.ExchangeTokenCommand;
import com.sky.mobile.skytvstream.service.secure.TokenExchangeService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthServiceException;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthenticationException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeServiceException;
import com.sky.web.utils.HTTPUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
public class TokenExchangeController {

    @Resource
    private TokenExchangeService tokenExchangeService;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @CacheHeaders(value = PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.CLIENT_ID})
    @RequestMapping(value = "/token", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Object exchangeToken(
            @RequestBody ExchangeTokenCommand command,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException, GeneralSecurityException {
        try {
            return tokenExchangeService.exchangeCode(command.getToken(), currentRequest.get(StreamingHeaders.CLIENT_ID), HTTPUtils.getOriginIp(request));
        } catch (TokenExchangeException | TokenExchangeServiceException | AuthServiceException | AuthenticationException t) {
            return HTTPUtils.getErrorMessage( response, HttpServletResponse.SC_FORBIDDEN, "9300",
                    "Invalid access code or Oogway is in error");
        }
    }

    public void setTokenExchangeService(TokenExchangeService tokenExchangeService) {
        this.tokenExchangeService = tokenExchangeService;
    }

    public void setCurrentRequest(Map<StreamingHeaders, String> currentRequest) {
        this.currentRequest = currentRequest;
    }
}

