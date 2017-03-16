package com.sky.mobile.skytvstream.utils;

public class ErrorResponse {

    private String code="undefined";
    private String message="undefined";

    public ErrorResponse(String code , String message){
        this.code = code;
        this.message = message;
    }

    public String getFormattedErrorResponse(){
        StringBuilder strb = new StringBuilder("{\"code\":\"");
        strb.append(code);
        strb.append("\",\"message\":\"");
        strb.append(message);
        strb.append("\"}");
        return strb.toString();
    }

}
