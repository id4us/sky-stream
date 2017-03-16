package com.sky.mobile.skytvstream.domain;

public enum ErrorReturnCodes {
	NO_SUBS(402, 9001);

	
	private final int errCode;
	private final int httpCode;

	ErrorReturnCodes(int httpCode, int errCode) {
		this.httpCode = httpCode;
		this.errCode = errCode;
	}

	public int getErrCode() {
		return errCode;
	}

	public int getHttpCode() {
		return httpCode;
	}
}
