package com.sky.mobile.skytvstream.service.stream;

public interface PersistStream {

	void persistStreamInfo(String streamId, PersistPayload payload) throws PersistQueueException;
	
	PersistPayload getStreamInfo(String streamId) throws PersistGetTimeoutException, PersistNotFoundException;

	void removeStreamInfo(String streamId);
	
}

