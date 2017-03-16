package com.sky.mobile.skytvstream.controller.internal;

import com.sky.mobile.annotations.InternalOnly;
import com.sky.mobile.skytvstream.service.system.RefreshService;
import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@InternalOnly
@RestController
@RequestMapping("/internal/system")
public class NotifierController {

    @Resource
    private RefreshService service;

    @RequestMapping("/refresh")
    public String refresh() {
        service.refreshData();
        return "OK";
    }
}


