package com.sky.mobile.skytvstream.domain;

public class ChannelVo {

	private String channelid;
	private String channelName;

	public ChannelVo() {
		super();
	}
	
	public ChannelVo(String channelid, String channelName) {
		this.channelid = channelid;
		this.channelName = channelName;
	}

	public String getChannelid() {
		return channelid;
	}

	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

}
