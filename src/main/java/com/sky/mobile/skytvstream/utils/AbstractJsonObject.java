package com.sky.mobile.skytvstream.utils;

import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Map;

/**
 * Abstract class to extend for creating Domain Objects to be marshelled to and
 * from JSON via Jackson Adds a map for unknown or undetected properties.
 */
public abstract class AbstractJsonObject {

    @JsonIgnore
    private final Map<String, Object> otherFields = Maps.newHashMap();

    @JsonAnySetter
    public void handleUnknown(final String key, final Object value) {
        otherFields.put(key, value);
    }

    @JsonIgnore
    public Map<String, Object> getOtherFields() {
        return otherFields;
    }
}
