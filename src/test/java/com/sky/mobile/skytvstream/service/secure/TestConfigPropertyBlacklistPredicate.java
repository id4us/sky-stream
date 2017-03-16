package com.sky.mobile.skytvstream.service.secure;

import com.sky.mobile.skytvstream.testutils.MockEntry;
import org.junit.Test;

import java.util.Map.Entry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestConfigPropertyBlacklistPredicate {

    @Test
    public void test() {
        ConfigPropertyBlackListPredicate predicate = new ConfigPropertyBlackListPredicate();

        Entry<Object, Object> valid = new MockEntry<Object, Object>("a.valid.entry", "value");
        Entry<Object, Object> invalid = new MockEntry<Object, Object>("Xan.invalid.entry", "value");

        assertTrue(predicate.apply(valid));
        assertFalse(predicate.apply(invalid));
    }

}
