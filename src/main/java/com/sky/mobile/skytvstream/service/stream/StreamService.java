package com.sky.mobile.skytvstream.service.stream;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.ChannelStreamVo;
import com.sky.mobile.skytvstream.domain.DeviceVo;
import com.sky.mobile.skytvstream.domain.StreamResponseObjectVo;
import com.sky.mobile.skytvstream.domain.StreamVerificationVo;

import javax.servlet.http.HttpServletResponse;

public interface StreamService {

	StreamResponseObjectVo createStream(String channelId, String profileId, String deviceId, String model);
	
	boolean isValidStreamObject(StreamVerificationVo streamVerificationVo);
	
	Optional<ChannelStreamVo> getChannelStreamingUrls(String channelId);
	
	void buildStreamAndCookies(StreamResponseObjectVo streamObject,
			ChannelStreamVo channelStreamVO, HttpServletResponse response, String deviceId, DeviceVo deviceVo);
}
