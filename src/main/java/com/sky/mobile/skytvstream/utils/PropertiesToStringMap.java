package com.sky.mobile.skytvstream.utils;

import com.google.common.collect.Maps;
import com.sky.mobile.skytvstream.service.secure.ConfigPropertyBlackListPredicate;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesToStringMap {
    private static final ConfigPropertyBlackListPredicate PREDICATE = new ConfigPropertyBlackListPredicate();

    public static Map<String, String> toMap(Properties properties) {
        Map<String, String> stringMap = Maps.newTreeMap();

        for (Entry<Object, Object> e : properties.entrySet()) {
            if (PREDICATE.apply(e)) {
                stringMap.put(e.getKey().toString(), e.getValue().toString());
            }
        }
        return stringMap;
    }

}
