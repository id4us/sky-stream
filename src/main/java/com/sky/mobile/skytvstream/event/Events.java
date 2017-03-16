package com.sky.mobile.skytvstream.event;

public final class Events {
	private Events() {
	}

	public static RefreshDataEvent newRefreshDataEvent(Object obj) {
		return new RefreshDataEvent(obj);
	}
}
