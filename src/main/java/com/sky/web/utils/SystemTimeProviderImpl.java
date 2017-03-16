package com.sky.web.utils;

public final class SystemTimeProviderImpl extends AbstractTimeProvider {

	@Override
	public long getTimeMillis() {
		return System.currentTimeMillis();
	}

}
