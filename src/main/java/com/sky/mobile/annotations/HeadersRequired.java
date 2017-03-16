package com.sky.mobile.annotations;

import com.sky.mobile.skytvstream.utils.StreamingHeaders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HeadersRequired {
	StreamingHeaders[] value();
}
