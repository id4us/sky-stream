package com.sky.mobile.skytvstream.utils;

public enum StreamingHeaders {

    VERSION("x-version"),
    SUB_PROVIDER("x-subscription-provider"),
    CLIENT_ID("x-client-id"),
    MODEL_ID("x-model-identifier"),
    COUNTRY_ID("x-country");


    private String header;

    StreamingHeaders(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }

    public static StreamingHeaders fromHeaderName(String headerName) {
        for (StreamingHeaders h : StreamingHeaders.values()) {
            if (h.toString().equalsIgnoreCase(headerName)) {
                return h;
            }
        }
        return null;
    }
}
