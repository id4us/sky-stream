package com.sky.mobile.skytvstream.testutils;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.internal.GetFuture;

public class MockGetFuture<T> extends GetFuture<T> {

	private Future<T> mockFuture;

	public MockGetFuture(CountDownLatch l, long opTimeout, String key,
			ExecutorService service) {
		super(l, opTimeout, key, service);
	}
	
	public MockGetFuture(Future<T> mockFuture) {
		this(null ,0, null, null);
		this.mockFuture = mockFuture;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return mockFuture.get();
	}
	
	@Override
	public T get(long duration, TimeUnit units) throws InterruptedException, ExecutionException, TimeoutException {
		return mockFuture.get(duration, units);
	}
	
	@Override
	public boolean isDone() {
		return mockFuture.isDone();
	}
	
	
}