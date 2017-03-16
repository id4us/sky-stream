package com.sky.mobile.skytvstream.service.secure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AkamaiCookie {
	
	public static final String KEY_COOKIE_NAME = "skytvk";
	public static final String PLAYLIST_COOKIE_NAME = "skytvp";
	
    private String ip;
    private String URL;
    private String salt;
    private long window;
    private long time;
    private long expires;
    private String token;
    private String mac;
    
	public AkamaiCookie() {
		throw new IllegalArgumentException("No parameter constructor is not supported");
	}
	
    public AkamaiCookie(String ipIn, long timeIn, long duration, String accessUrl, String saltIn, boolean validateAccessUrl, boolean calculateExpiry)
    {
        StringBuffer stringbuffer = new StringBuffer();
        StringBuffer stringbuffer1 = new StringBuffer();
        if(validateAccessUrl)
        {
            String as[] = accessUrl.split("!");
            String s3 = new String("");
            for(int j = 0; j < as.length; j++)
            {
                String s5 = new String("");
                String as1[] = as[j].split("\\?");
                for(int k = 0; k < as1.length; k++)
                    if(k == 0)
                    {
                        String s6 = as1[k].replaceAll("//+", "/");
                        s5 = s5.concat(s6);
                    } else
                    {
                        String s7 = as1[k].replaceAll("#.*", "");
                        s5 = s5.concat("?");
                        s5 = s5.concat(s7);
                    }

                if(j != 0)
                    s3 = s3.concat("!");
                s3 = s3.concat(s5);
            }

            accessUrl = s3;
        } 
        
        if (ip!=null) {
        	ip = new String(ipIn);
        }
        salt = new String(saltIn);
        URL = new String(accessUrl);
        time = timeIn;
        window = duration;
        
        if(calculateExpiry) {
        	expires = time + window;
        } else {
        	expires = time;
        }
        
        MessageDigest messagedigest;
        try
        {
            messagedigest = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException nosuchalgorithmexception)
        {
            throw new IllegalArgumentException((new StringBuilder()).append("Can't get MD5 instance ").append(nosuchalgorithmexception).toString());
        }
        if(ip != null && ip.length() != 0)
            messagedigest.update(ip.getBytes());
        if(expires != 0L)
            messagedigest.update(Long.toString(expires).getBytes());
        messagedigest.update(URL.getBytes());
        messagedigest.update(salt.getBytes());
        byte abyte0[] = messagedigest.digest();
        for(int i = 0; i < abyte0.length; i++)
        {
            String s4 = Integer.toHexString(abyte0[i] & 0xff);
            if(s4.length() == 1)
                stringbuffer1.append('0');
            stringbuffer1.append(s4);
        }

        if(ip != null && ip.length() != 0 && !ip.equals("0.0.0.0"))
        {
            stringbuffer.append("ip=");
            stringbuffer.append(ip);
            stringbuffer.append("~");
        }
        if(expires != 0L)
        {
            stringbuffer.append("expires=");
            stringbuffer.append(expires);
            stringbuffer.append("~");
        }
        if(URL != null && URL.length() != 0)
        {
            stringbuffer.append("access=");
            stringbuffer.append(URL);
            stringbuffer.append("~");
        }
        stringbuffer.append("md5=");
        stringbuffer.append(stringbuffer1);
        token = stringbuffer.toString();
        mac = stringbuffer1.toString();
    }

	public AkamaiCookie(String ipIn, long timeIn, long duration, String accessUrl, String saltIn)
    {
        this(ipIn, timeIn, duration, accessUrl, saltIn, false, true);
    }
    
    public AkamaiCookie(String ipIn, long timeIn, String accessUrl, String saltIn)
    {
        this(ipIn, timeIn, 120L, accessUrl, saltIn, false, false);
    }    

    public String getToken()
    {
        return token;
    }

    public String getIP()
    {
        return ip;
    }

    public long getWindow()
    {
        return window;
    }

    public long getTime()
    {
        return time;
    }

    public long getExpires()
    {
        return expires;
    }

    public String getSalt()
    {
        return salt;
    }

    public String getAccess()
    {
        return URL;
    }

    public String getURL()
    {
        return URL;
    }
    
    public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
	
}
