package com.sky.mobile.skytvstream.service.secure;

import com.sky.mobile.skytvstream.domain.ExchangeTokenResponse;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthServiceException;
import com.sky.mobile.ssmtv.oauth.exceptions.AuthenticationException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeException;
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeServiceException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface TokenExchangeService {

    ExchangeTokenResponse exchangeCode(String code, String clientId, String originIp) throws TokenExchangeException, TokenExchangeServiceException, AuthenticationException, AuthServiceException, GeneralSecurityException, UnsupportedEncodingException;

}
