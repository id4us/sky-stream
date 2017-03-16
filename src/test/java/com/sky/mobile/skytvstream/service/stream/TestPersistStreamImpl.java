package com.sky.mobile.skytvstream.service.stream;

import com.sky.mobile.skytvstream.testutils.MockFuture;
import com.sky.mobile.skytvstream.testutils.MockGetFuture;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class TestPersistStreamImpl {

    private static final int CACHETIME = 10;

    private MemcachedClient memcached;
    private PersistStreamImpl persistStream;

    @Before
    public void setUp() throws Exception {
        memcached = createMock(MemcachedClient.class);
        persistStream = new PersistStreamImpl(memcached);
        persistStream.setCacheTimeOut(CACHETIME);
    }

    @Test
    public void testNullPersist() throws PersistQueueException {
        try {
            persistStream.persistStreamInfo(null, null);
            fail("Expected Null Pointer Exception");
        } catch (NullPointerException e) {
            //expected
        }

        try {
            persistStream.persistStreamInfo("123", null);
            fail("Expected Null Pointer Exception");
        } catch (NullPointerException e) {
            //expected
        }

        try {
            persistStream.persistStreamInfo(null, new PersistPayload());
            fail("Expected Null Pointer Exception");
        } catch (NullPointerException e) {
            //expected
        }


    }


    @Test
    public void testStreamPersist_newItem() throws PersistQueueException {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = new PersistPayload();
        expectedPayload.setNonce("1234");
        OperationFuture<Boolean> expectedFuture = createOpFuture();


        expect(memcached.set(expectedStreamId, CACHETIME, expectedPayload)).andReturn(expectedFuture);
        replay(memcached);


        persistStream.persistStreamInfo(expectedStreamId, expectedPayload);

        verify(memcached);

    }

    @Test
    public void testStreamPersist_cacheQueueFail() {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = new PersistPayload();
        expectedPayload.setNonce("1234");


        expect(memcached.set(expectedStreamId, CACHETIME, expectedPayload)).andThrow(new IllegalStateException());
        replay(memcached);

        try {
            persistStream.persistStreamInfo(expectedStreamId, expectedPayload);
            fail("Expected PersistQueueException");
        } catch (PersistQueueException e) {
            assertTrue(e.getMessage().startsWith("ALERT: persistence Queue is full, memcache may not be working!"));
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        verify(memcached);
    }

    @Test
    public void testGetPersisted_noDelay() throws PersistGetTimeoutException, PersistNotFoundException {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = new PersistPayload();
        expectedPayload.setNonce("1234");

        Future<PersistPayload> expectedFuture = MockFuture.createInstantFuture(expectedPayload);

        GetFuture<Object> expectedFutureGet = new MockGetFuture(expectedFuture);
        expect(memcached.asyncGet(expectedStreamId)).andReturn(expectedFutureGet);
        replay(memcached);


        PersistPayload actual = persistStream.getStreamInfo(expectedStreamId);
        assertEquals(expectedPayload, actual);
        verify(memcached);

    }

    @Test
    public void testGetPersisted_oneSecDelay() throws PersistGetTimeoutException, PersistNotFoundException {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = new PersistPayload();
        expectedPayload.setNonce("1234");

        Future<PersistPayload> expectedFuture = MockFuture.createTimedFuture(expectedPayload, 1);

        GetFuture<Object> expectedFutureGet = new MockGetFuture(expectedFuture);
        expect(memcached.asyncGet(expectedStreamId)).andReturn(expectedFutureGet);
        replay(memcached);


        PersistPayload actual = persistStream.getStreamInfo(expectedStreamId);
        assertEquals(expectedPayload, actual);
        verify(memcached);

    }


    @Test
    public void testGetPersisted_timeoutFromMemcache() throws PersistNotFoundException {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = new PersistPayload();
        expectedPayload.setNonce("1234");

        Future<PersistPayload> expectedFuture = MockFuture.createNeverFinishFuture(expectedPayload);

        GetFuture<Object> expectedFutureGet = new MockGetFuture(expectedFuture);
        expect(memcached.asyncGet(expectedStreamId)).andReturn(expectedFutureGet);
        replay(memcached);

        try {
            persistStream.getStreamInfo(expectedStreamId);
            fail("Expected PersistGetTimeoutException");
        } catch (PersistGetTimeoutException e) {
            assertTrue(e.getMessage().startsWith("ALERT: Timeout retrieving item from persistent store."));
        }
        verify(memcached);
    }

    @Test
    public void testGetPersisted_nullFromMemcache() throws PersistGetTimeoutException {
        String expectedStreamId = "987654321";

        PersistPayload expectedPayload = null;

        Future<PersistPayload> expectedFuture = MockFuture.createInstantFuture(expectedPayload);

        GetFuture<Object> expectedFutureGet = new MockGetFuture(expectedFuture);
        expect(memcached.asyncGet(expectedStreamId)).andReturn(expectedFutureGet);
        replay(memcached);


        try {
            persistStream.getStreamInfo(expectedStreamId);
            fail("Expected PersistNotFoundException");
        } catch (PersistNotFoundException e) {
            assertTrue(e.getMessage().startsWith("Null received retrieving streamId from memcache:"));
        }
        verify(memcached);

    }

    private OperationFuture<Boolean> createOpFuture() {
        OperationFuture<Boolean> future = new OperationFuture<Boolean>("test", new CountDownLatch(1), 1000, null) {
            @Override
            public Boolean get(long l, TimeUnit t) {
                return true;
            }
        };
        return future;
    }

}
