package com.sky.mobile.skytvstream.event;

import org.springframework.context.ApplicationEvent;

public class RefreshDataEvent extends ApplicationEvent{

	private static final long serialVersionUID = 2072407426218291216L;

	public RefreshDataEvent(Object source) {
		super(source);
	}

}
