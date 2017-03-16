package com.sky.mobile.skytvstream.testutils;

import java.util.Map.Entry;

public class MockEntry<K,V> implements Entry<K,V>{

	private final K key;
	private V value;

	public MockEntry(K key, V value) {
		this.key = key;
		this.value = value;
	} 
	
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		return value;
	}
	
}