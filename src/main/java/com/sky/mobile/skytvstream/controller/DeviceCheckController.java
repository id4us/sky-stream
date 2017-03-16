package com.sky.mobile.skytvstream.controller;

import com.sky.mobile.annotations.CacheHeaders;
import com.sky.mobile.annotations.HeadersRequired;
import com.sky.mobile.annotations.PageCacheStrategy;
import com.sky.mobile.skytvstream.domain.DeviceVo;
import com.sky.mobile.skytvstream.service.device.DeviceService;
import com.sky.mobile.skytvstream.utils.StreamingHeaders;
import com.sky.web.utils.HTTPUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
public class DeviceCheckController {

    @Resource
    private DeviceService deviceService;

    @Resource(name = "currentRequest")
    private Map<StreamingHeaders, String> currentRequest;

    @CacheHeaders(PageCacheStrategy.NONE)
    @HeadersRequired({StreamingHeaders.VERSION, StreamingHeaders.MODEL_ID, StreamingHeaders.CLIENT_ID})
    @RequestMapping(value = "/supported/device", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object isSupportDevice(final HttpServletResponse response) throws IOException {

        final DeviceVo deviceVo = deviceService.getDeviceVo(currentRequest.get(StreamingHeaders.MODEL_ID));

        if (!deviceService.isSupportDevice(deviceVo)) {
            return HTTPUtils.getErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, "9004",
                    "Device is not supported.");
        }

        return "{\"supported\":\"true\"}";
    }
}
