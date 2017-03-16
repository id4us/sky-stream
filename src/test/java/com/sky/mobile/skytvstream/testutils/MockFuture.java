package com.sky.mobile.skytvstream.testutils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockFuture<T extends Object> implements Runnable, Future<T>{
	
	private volatile boolean done = false;
	private volatile int time = 0;
	private T returnedData;

	private MockFuture() {
		
	}
	
	public static <T> MockFuture<T> createInstantFuture (T payload) {
		MockFuture<T> future = new MockFuture<T>();
		future.returnedData = payload;
		future.done = true;
		return future;		
	}

	public static <T> MockFuture<T> createTimedFuture (T payload, int seconds) {
		MockFuture<T> future = new MockFuture<T>();
		future.returnedData = payload;
		future.done = false;
		future.time = seconds;
		new Thread(future).start();
		return future;		
	}	

	public static <T> MockFuture<T> createNeverFinishFuture (T payload) {
		MockFuture<T> future = new MockFuture<T>();
		future.returnedData = payload;
		future.done = false;
		return future;		
	}	
	
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		//TODO: Implement if needed.
		return false;
	}

	@Override
	public boolean isCancelled() {
		//TODO: Implement if needed.
		return false;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return _get(Long.MAX_VALUE);
		} catch (TimeoutException e) {
			throw new InterruptedException();
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		
		long endTime = System.currentTimeMillis() + (TimeUnit.MILLISECONDS.convert(timeout, unit));
		return _get(endTime);
		
	}

	private T _get(long endTime) throws TimeoutException {
		while(!done) {
			if (System.currentTimeMillis() > endTime) {
				throw new TimeoutException();
			}
		}
		return returnedData;
	}

	@Override
	public void run() {
		long endTime = System.currentTimeMillis() + (time * 1000);
		while(System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing for now.
			}
		}
		done = true;
	}

}
