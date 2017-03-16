package com.sky.mobile.skytvstream.testutils;

import java.lang.reflect.Field;

public final class BeanSetter {

    public static Field getField(final Object bean, final String fieldName) throws SecurityException,
            NoSuchFieldException {
        final Class<?> c = bean.getClass();
        final Field f = c.getDeclaredField(fieldName);
        f.setAccessible(true); // solution
        return f;
    }

    public static Field getFieldForClass(final Class<?> clazz, final String fieldName) throws SecurityException,
            NoSuchFieldException {
        final Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true); // solution
        return f;
    }
}
