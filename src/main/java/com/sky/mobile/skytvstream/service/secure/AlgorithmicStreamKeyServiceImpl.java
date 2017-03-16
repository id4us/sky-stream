package com.sky.mobile.skytvstream.service.secure;


import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;
import com.sky.web.utils.TimeProvider;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

@Service
@Qualifier("algorithmicStreamKeyService")
public class AlgorithmicStreamKeyServiceImpl implements StreamKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(AlgorithmicStreamKeyServiceImpl.class);

    private static final String DELIMITER = "@#$#";
    private TimeProvider timeProvider;
    private CryptoFactory cryptoFactory;
    private String serverSideSeed;
    private long timeToLive;

    private static final byte[] SALT = {
            (byte) 0xd0, (byte) 0x9d, (byte) 0x11, (byte) 0x98, (byte) 0xa8, (byte) 0xe0, (byte) 0xd0, (byte) 0xd8
    };

    @Autowired
    public AlgorithmicStreamKeyServiceImpl(
            TimeProvider timeProvider,
            CryptoFactory cryptoFactory,
            @Value("${Xalgorithmic.stream.key.salt}") String serverSideSeed,
            @Value("${algorithmic.stream.key.eviction.time.interval}") long timeToLive) {
        this.serverSideSeed = serverSideSeed;
        this.timeToLive = timeToLive;
        this.timeProvider = timeProvider;
        this.cryptoFactory = cryptoFactory;
    }

    @Override
    public Optional<String> generateKey(final StreamKeyVo streamKeyVo) {
        String key = null;
        try {
            String nonce = new String(cryptoFactory.getNonce());
            streamKeyVo.setNonce(nonce);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(timeProvider.getTimeMillis() + timeToLive);
            stringBuffer.append(DELIMITER);
            stringBuffer.append(streamKeyVo.getDeviceId());
            stringBuffer.append(DELIMITER);
            stringBuffer.append(streamKeyVo.getProfileId());
            stringBuffer.append(DELIMITER);
            stringBuffer.append(streamKeyVo.getModel());
            stringBuffer.append(DELIMITER);
            stringBuffer.append(streamKeyVo.getChannelId());
            stringBuffer.append(DELIMITER);
            stringBuffer.append(streamKeyVo.getNonce());
            key = encrypt(stringBuffer.toString());
        } catch (Exception e) {
            LOG.error("unable to generate stream key", e);
        }

        return Optional.fromNullable(key);
    }

    @Override
    public Optional<StreamKeyVo> getStreamKeyVo(final String streamKey) {
        StreamKeyVo streamKeyVo;
        try {
            String decryptedStreamKey = decrypt(streamKey);
            String[] streamValues = decryptedStreamKey.split(Pattern.quote(DELIMITER));
            streamKeyVo = new StreamKeyVo(streamValues[4], streamValues[2], streamValues[1], streamValues[3]);
            streamKeyVo.setExpiryTime(Long.parseLong(streamValues[0]));
            streamKeyVo.setNonce(streamValues[5]);
        } catch (Exception e) {
            streamKeyVo = null;
            LOG.error("unable to decrypt stream key", e);
        }
        return Optional.fromNullable(streamKeyVo);
    }

    private String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(serverSideSeed.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return Base64.encodeBase64String(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    private String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(serverSideSeed.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(Base64.decodeBase64(property)), "UTF-8");
    }

}
