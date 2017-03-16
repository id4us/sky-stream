package com.sky.mobile.skytvstream.service.stream;

import com.google.common.base.Preconditions;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class PersistStreamImpl implements PersistStream {

    private static final Logger LOG = LoggerFactory.getLogger(PersistStreamImpl.class);

    private static final long GET_TIMEOUT = 2000;

    private int cacheTimeOut;

    private MemcachedClient memcachedClient;

    @Autowired
    public PersistStreamImpl(MemcachedClient memcacheClient) {
        this.memcachedClient = memcacheClient;
    }

    @Override
    public void persistStreamInfo(String streamId, PersistPayload payload)
            throws PersistQueueException {
        Preconditions.checkNotNull(streamId);
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(memcachedClient);
        try {
            OperationFuture<Boolean> future = memcachedClient.set(streamId, cacheTimeOut, payload);
            future.get(GET_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            //TODO: Really need to think of a way of detecting a failed memcache BEFORE the queue is full!!
            // ie, maybe sample some futures async, and if some are returning with errors, open gates?
            // Also maybe handle it better, such as flush, etc?
            String msg = "ALERT: persistence Queue is full, memcache may not be working!";
            LOG.error(msg, e);
            throw new PersistQueueException(msg, e);
        } catch (TimeoutException e) {
            String msg = "ALERT: Unable to persist item in a timely manner. Is memcache working/running hot?";
            LOG.error(msg, e);
            throw new PersistQueueException(msg, e);
        } catch (Exception e) {
            String msg = "ALERT: Unable to persist item. Is memcache working/running hot?";
            LOG.error(msg, e);
            throw new PersistQueueException(msg, e);
        }
    }

    @Override
    public void removeStreamInfo(String streamId) {
        Preconditions.checkNotNull(streamId);
        memcachedClient.delete(streamId);
    }

    @Override
    public PersistPayload getStreamInfo(String streamId)
            throws PersistGetTimeoutException, PersistNotFoundException {
        Preconditions.checkNotNull(memcachedClient);
        Object payload;
        try {
            GetFuture<Object> futureOp = memcachedClient.asyncGet(streamId);
            payload = futureOp.get(GET_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            String msg = "ALERT: Timeout retrieving item from persistent store. " + GET_TIMEOUT;
            LOG.error(msg, e);
            throw new PersistGetTimeoutException(msg, e);
        } catch (Exception e) {
            String msg = "Unknown error retrieving stream from persistence store: " + streamId;
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        if (payload == null) {
            String msg = "Null received retrieving streamId from memcache: " + streamId;
            LOG.warn(msg);
            throw new PersistNotFoundException(msg);
        }
        return (PersistPayload) payload;
    }


    @Value("${stream.memcache.eviction.timeout}")
    public void setCacheTimeOut(int cacheTimeOut) {
        this.cacheTimeOut = cacheTimeOut;
        LOG.debug("set persist duration to: " + cacheTimeOut);
    }

}
