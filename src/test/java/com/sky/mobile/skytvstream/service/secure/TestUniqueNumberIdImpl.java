package com.sky.mobile.skytvstream.service.secure;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestUniqueNumberIdImpl {

    private UniqueNumberGenerator uniqueId;

    @Before
    public void setUp() throws Exception {
        uniqueId = new UniqueNumberGeneratorImpl();
    }

    @Test
    public void test() {
        String a = uniqueId.getId();
        String b = uniqueId.getId();
        assertTrue(new Long(a).longValue() < new Long(b).longValue());
        assertTrue((new Long(a).longValue() + 1L) == (new Long(b).longValue()));
    }

}
