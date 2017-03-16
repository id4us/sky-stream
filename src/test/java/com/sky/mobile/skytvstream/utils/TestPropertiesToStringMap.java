package com.sky.mobile.skytvstream.utils;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPropertiesToStringMap {

    @Test
    public void test() {
        Properties p = new Properties();

        p.put("hello", "there");
        p.setProperty("a.key", "a.value");
        p.put(new TestObject("an.object.key"),
                new TestObject("an.object.value"));

        Map<String, String> result = PropertiesToStringMap.toMap(p);

        assertEquals(3, result.size());
        assertTrue(result.keySet().containsAll(
                Sets.newHashSet("hello", "a.key", "an.object.key")));
        assertEquals("there", result.get("hello"));
        assertEquals("an.object.value", result.get("an.object.key"));
        assertEquals("a.value", result.get("a.key"));
    }

    private class TestObject {
        private final String value;

        public TestObject(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
