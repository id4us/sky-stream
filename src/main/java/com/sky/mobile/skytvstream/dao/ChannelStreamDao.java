package com.sky.mobile.skytvstream.dao;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.ChannelStreamVo;

public interface ChannelStreamDao {
	
	Optional<ChannelStreamVo> getChanelStreamByChannelId(String channelId);

}
