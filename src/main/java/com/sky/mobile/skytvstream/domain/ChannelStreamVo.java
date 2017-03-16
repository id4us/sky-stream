package com.sky.mobile.skytvstream.domain;

import com.sky.mobile.skytvstream.utils.AbstractJsonObject;

public class ChannelStreamVo extends AbstractJsonObject {
	private String channelId;
	private String name;
	private String keyPath;
	private String playlist;
	private String playlistPath;

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

	public String getPlaylist() {
		return playlist;
	}

	public void setPlaylist(String playlist) {
		this.playlist = playlist;
	}

	public String getPlaylistPath() {
		return playlistPath;
	}

	public void setPlaylistPath(String playlistPath) {
		this.playlistPath = playlistPath;
	}

}
