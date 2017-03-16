package com.sky.mobile.skytvstream.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestErrorResponse {
    @Test
    public void testCorrectMessage() {
        ErrorResponse er = new ErrorResponse("code","message");
        assertEquals("{\"code\":\"code\",\"message\":\"message\"}", er.getFormattedErrorResponse());
    }
}
