package com.sky.web.utils;

import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

public class AnnotationHelper {
    private AnnotationHelper() {
    }

    public static <K extends Annotation> K getAnnotationFromHandler(final HandlerMethod handler, final Class<K> klass) {

        final Class<?> targetClass = handler.getBeanType();

        final K annotation = handler
                .getMethodAnnotation(klass) != null ? handler
                .getMethodAnnotation(klass) : (targetClass
                .isAnnotationPresent(klass) ? targetClass
                .getAnnotation(klass) : null);

        return annotation;
    }

    public static <K extends Annotation> K getAnnotationFromObject(final Object handler, final Class<K> klass) {
        return handler instanceof HandlerMethod ? getAnnotationFromHandler((HandlerMethod) handler, klass)
                : handler.getClass()
                .isAnnotationPresent(klass) ? handler.getClass()
                .getAnnotation(klass) : null;
    }

}
