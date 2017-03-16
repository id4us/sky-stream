package com.sky.mobile.skytvstream.service.secure;

public class AkamaiToken {

	public static final String SEPARATOR = "~";
	public static final String URL_SEPARATOR = "!";
	public static final String MAC_IDENTIFIER = "md5=";
	public static final String IP_IDENTIFIER = "ip=";
	public static final String EXPIRES_IDENTIFIER = "expires=";
	public static final String ACCESS_IDENTIFIER = "access=";
	
	// the complete set of values including the MAC
	private String token;
	
	// the value of the MAC (not including the mac name)
	private String macValue;
	// the open values of the token, excluding the mac name, value pair
	private String entropy; 
	
	private String ip;
	private String expires;
	private String url;
	
	public AkamaiToken(String tokenIn) {
		
		this.token = tokenIn;
		this.macValue = findMAC();
		this.ip = findIP();
		this.expires = findExpires();
		this.url = findURL();
		this.entropy = findEntropy();
	}
	
	public String getMac() {
		return macValue;
	}

	public void setMacValue(String macValue) {
		this.macValue = macValue;
	}

	public String getEntropy() {
		return entropy;
	}

	public void setEntropy(String entropy) {
		this.entropy = entropy;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getExpires() {
		long retVal = 0L;
		
		if (expires != null){
			retVal = Long.valueOf(expires).longValue();
		}
		return retVal;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
    private String findMAC() {
    	String retVal = null;
    	
    	if (token.contains(AkamaiToken.MAC_IDENTIFIER)) {
    		retVal = token.substring(token.indexOf(AkamaiToken.MAC_IDENTIFIER) + AkamaiToken.MAC_IDENTIFIER.length());
    	}
    	
    	return retVal;
    }
    
    private String findIP() {
    	String retVal = null;
    	
    	if (token.contains(AkamaiToken.IP_IDENTIFIER)) {
    		retVal = token.substring(token.indexOf(AkamaiToken.IP_IDENTIFIER) + AkamaiToken.IP_IDENTIFIER.length());
    		retVal = retVal.substring(0, retVal.indexOf(SEPARATOR));
    	}
    	
    	return retVal;    	
    }
    
    private String findExpires() {
    	String retVal = null;
    	
    	if (token.contains(AkamaiToken.EXPIRES_IDENTIFIER)) {
    		retVal = token.substring(token.indexOf(AkamaiToken.EXPIRES_IDENTIFIER) + AkamaiToken.EXPIRES_IDENTIFIER.length());
    		retVal = retVal.substring(0, retVal.indexOf(SEPARATOR));
    	}
    	
    	return retVal;    	
    }
    
    private String findURL() {
    	String retVal = null;
    	
    	if (token.contains(AkamaiToken.ACCESS_IDENTIFIER)) {
    		retVal = token.substring(token.indexOf(AkamaiToken.ACCESS_IDENTIFIER) + AkamaiToken.EXPIRES_IDENTIFIER.length() - 1);
    		retVal = retVal.substring(0, retVal.indexOf(SEPARATOR));
    	}
    	
    	return retVal;    	
    }    
    
    private String findEntropy() {
    	String retVal = null;
    	
    	if (token.contains(AkamaiToken.MAC_IDENTIFIER)) {
    		retVal = token.substring(0, token.indexOf(AkamaiToken.MAC_IDENTIFIER));
    	} else {
    		retVal = token;
    	}
    	
    	return retVal;    	
    }
    
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( getClass().getSimpleName() );
		buffer.append( "[" );
		buffer.append( "ip" );
		buffer.append( getIp() );
		buffer.append( ", url=" );
		buffer.append( getUrl() );
		buffer.append( ", expires=\"" );
		buffer.append( getExpires() );
		buffer.append( "\", mac=" );
		buffer.append( getMac() );
		buffer.append( "]" );
		
		return buffer.toString();
	}    
}
