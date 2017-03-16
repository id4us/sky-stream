package com.sky.mobile.skytvstream.service.secure;

import java.util.Map.Entry;

import com.google.common.base.Predicate;

public class ConfigPropertyBlackListPredicate implements Predicate<Entry<Object,Object>> {

	@Override
	public boolean apply(Entry<Object, Object> input) {
		return !input.getKey().toString().startsWith("X");
	}
	
}
