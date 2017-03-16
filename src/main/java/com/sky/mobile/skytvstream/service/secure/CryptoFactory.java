package com.sky.mobile.skytvstream.service.secure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


@Service
public class CryptoFactory {

    private static final Log log = LogFactory.getLog(CryptoFactory.class);

    /**
     * Generates a cryptographically-safe 128 bit random number. It should not produce
     * numbers in a predictable sequence.
     * The API should use a hardware random number generator, that although could
     * theoretically generate a repeated number, it is very unlikely. The use of
     * this method should consider if this is acceptable.
     * <p/>
     * Base 64 encodes the generated value so it may be passed in web requests and
     * responses.
     *
     * @return hardware generated 128 random number
     * @throws Exception
     */
    public byte[] getNonce() {

        byte[] nonce = new byte[16];
        Random rand;
        try {
            rand = SecureRandom.getInstance("SHA1PRNG");
            rand.nextBytes(nonce);
        } catch (NoSuchAlgorithmException na) {
            String msg = "ALERT: Could not generate NONCE value. Algorithm (SHA1PRNG) is not supported";
            log.error(msg, na);
            throw new IllegalStateException(msg, na);
        }

        // the generated nonce values contain characters that
        // can not be passed in the web response. Therefore safely
        // encode the bytes and make the 'web safe'
        return Base64.encodeBase64(nonce);
    }

    public int getRandomNumber() {
        Random rand = new Random();
        return rand.nextInt(9000000) + 1000000;
    }


}
