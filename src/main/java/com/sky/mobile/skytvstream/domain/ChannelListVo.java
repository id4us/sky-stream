package com.sky.mobile.skytvstream.domain;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class ChannelListVo {

    private List<String> channelList = Lists.newArrayList();

    public List<String> getChannelList() {
        return channelList;
    }

	public void setChannelList(List<String> channelList) {
		this.channelList = channelList;
	}

    public static ChannelListVo from(Collection<String> channels) {
        ChannelListVo channelListVo = new ChannelListVo();
        channelListVo.getChannelList().addAll(channels);
        return channelListVo;
    }

}
